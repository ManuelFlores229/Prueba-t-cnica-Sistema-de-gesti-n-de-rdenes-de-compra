package com.example.proyecto2.service;

import com.example.proyecto2.dto.orden.DetalleOrdenRequest;
import com.example.proyecto2.dto.orden.DetalleOrdenResponse;
import com.example.proyecto2.dto.orden.OrdenRequest;
import com.example.proyecto2.dto.orden.OrdenResponse;
import com.example.proyecto2.entidades.Cliente;
import com.example.proyecto2.entidades.DetalleOrden;
import com.example.proyecto2.entidades.Estados.EstadoOrden;
import com.example.proyecto2.entidades.Estados.EstadoRegistro;
import com.example.proyecto2.entidades.Orden;
import com.example.proyecto2.entidades.Producto;
import com.example.proyecto2.exception.ConflictoNegocioException;
import com.example.proyecto2.exception.RecursoNoEncontradoException;
import com.example.proyecto2.exception.SolicitudInvalidaException;
import com.example.proyecto2.repository.ClienteRepository;
import com.example.proyecto2.repository.OrdenRepository;
import com.example.proyecto2.repository.ProductoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class OrdenService {
    private static final Set<String> CAMPOS_ORDENABLES = Set.of(
            "id", "total", "estado", "fechaCreacion"
    );

    private final OrdenRepository ordenRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;

    public OrdenService(
            OrdenRepository ordenRepository,
            ClienteRepository clienteRepository,
            ProductoRepository productoRepository
    ) {
        this.ordenRepository = ordenRepository;
        this.clienteRepository = clienteRepository;
        this.productoRepository = productoRepository;
    }

    @Transactional(readOnly = true)
    public Page<OrdenResponse> listar(Pageable pageable) {
        Ordenamiento.validar(pageable, CAMPOS_ORDENABLES);

        return ordenRepository.findAll(pageable)
                .map(this::convertirRespuesta);
    }

    @Transactional(readOnly = true)
    public Page<OrdenResponse> listarPorEstado(
            EstadoOrden estado,
            Pageable pageable
    ) {
        Ordenamiento.validar(pageable, CAMPOS_ORDENABLES);

        return ordenRepository.findByEstado(estado, pageable)
                .map(this::convertirRespuesta);
    }

    @Transactional(readOnly = true)
    public Page<OrdenResponse> listarPorRangoFechas(
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin,
            Pageable pageable
    ) {
        Ordenamiento.validar(pageable, CAMPOS_ORDENABLES);

        if (fechaInicio.isAfter(fechaFin)) {
            throw new SolicitudInvalidaException(
                    "La fecha de inicio no puede ser posterior a la fecha final"
            );
        }

        return ordenRepository
                .findByFechaCreacionBetween(fechaInicio, fechaFin, pageable)
                .map(this::convertirRespuesta);
    }

    @Transactional(readOnly = true)
    public Page<OrdenResponse> listarPorCliente(Long clienteId, Pageable pageable) {
        Ordenamiento.validar(pageable, CAMPOS_ORDENABLES);

        return ordenRepository.findByClienteId(clienteId, pageable)
                .map(this::convertirRespuesta);
    }


    @Transactional(readOnly = true)
    public OrdenResponse buscarPorId(Long id) {
        return convertirRespuesta(obtenerOrden(id));
    }

    @Transactional
    public OrdenResponse crear(OrdenRequest request) {
        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe el cliente indicado"));

        if (cliente.getEstado() != EstadoRegistro.ACTIVO) {
            throw new ConflictoNegocioException(
                    "No se pueden crear órdenes para clientes inactivos"
            );
        }

        Orden orden = new Orden();
        orden.setCliente(cliente);
        orden.setEstado(EstadoOrden.CREATED);

        for (DetalleOrdenRequest item : request.getDetalles()) {
            Producto producto = obtenerProducto(item.getProductoId());

            if (producto.getEstado() != EstadoRegistro.ACTIVO) {
                throw new ConflictoNegocioException(
                        "No se pueden agregar productos inactivos a una orden"
                );
            }

            DetalleOrden detalle = new DetalleOrden();
            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecio());
            detalle.calcularSubtotal();
            orden.agregarDetalle(detalle);
        }

        BigDecimal total = orden.getDetalles().stream()
                .map(DetalleOrden::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        orden.setTotal(total);

        return convertirRespuesta(ordenRepository.save(orden));
    }

    @Transactional
    public OrdenResponse confirmar(Long id) {
        Orden orden = obtenerOrden(id);

        if (orden.getEstado() != EstadoOrden.CREATED) {
            throw new ConflictoNegocioException(
                    "Solo se pueden confirmar órdenes con estado CREATED"
            );
        }

        for (DetalleOrden detalle : orden.getDetalles()) {
            Producto producto = obtenerProducto(detalle.getProducto().getId());

            if (producto.getStock() < detalle.getCantidad()) {
                throw new ConflictoNegocioException(
                        "Existencias insuficientes para el producto: "
                                + producto.getNombre()
                );
            }

            producto.setStock(producto.getStock() - detalle.getCantidad());
            productoRepository.save(producto);
        }

        orden.setEstado(EstadoOrden.CONFIRMED);

        return convertirRespuesta(ordenRepository.save(orden));
    }

    @Transactional
    public OrdenResponse cancelar(Long id) {
        Orden orden = obtenerOrden(id);

        if (orden.getEstado() == EstadoOrden.CANCELLED) {
            throw new ConflictoNegocioException(
                    "La orden ya se encuentra cancelada"
            );
        }

        if (orden.getEstado() == EstadoOrden.CONFIRMED) {
            for (DetalleOrden detalle : orden.getDetalles()) {
                Producto producto =
                        obtenerProducto(detalle.getProducto().getId());

                producto.setStock(producto.getStock() + detalle.getCantidad());
                productoRepository.save(producto);
            }
        }

        orden.setEstado(EstadoOrden.CANCELLED);

        return convertirRespuesta(ordenRepository.save(orden));
    }

    private Orden obtenerOrden(Long id) {
        return ordenRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe la orden con ID: " + id));
    }

    private Producto obtenerProducto(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe el producto con ID: " + id));
    }

    private OrdenResponse convertirRespuesta(Orden orden) {
        OrdenResponse response = new OrdenResponse();
        response.setId(orden.getId());
        response.setClienteId(orden.getCliente().getId());
        response.setFechaCreacion(orden.getFechaCreacion());
        response.setTotal(orden.getTotal());
        response.setEstado(orden.getEstado());

        List<DetalleOrdenResponse> detallesResponse = new ArrayList<>();
        for (DetalleOrden detalle : orden.getDetalles()) {
            DetalleOrdenResponse detalleResponse = new DetalleOrdenResponse();
            detalleResponse.setProductoId(detalle.getProducto().getId());
            detalleResponse.setNombreProducto(detalle.getProducto().getNombre());
            detalleResponse.setCantidad(detalle.getCantidad());
            detalleResponse.setPrecioUnitario(detalle.getPrecioUnitario());
            detalleResponse.setSubtotal(detalle.getSubtotal());
            detallesResponse.add(detalleResponse);
        }

        response.setDetalles(detallesResponse);
        return response;
    }
}