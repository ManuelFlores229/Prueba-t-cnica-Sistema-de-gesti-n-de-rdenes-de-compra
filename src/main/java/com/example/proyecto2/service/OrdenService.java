package com.example.proyecto2.service;

import com.example.proyecto2.dto.orden.DetalleOrdenRequest;
import com.example.proyecto2.dto.orden.DetalleOrdenResponse;
import com.example.proyecto2.dto.orden.OrdenRequest;
import com.example.proyecto2.dto.orden.OrdenResponse;
import com.example.proyecto2.entidades.Cliente;
import com.example.proyecto2.entidades.DetalleOrden;
import com.example.proyecto2.entidades.Estados.EstadoOrden;
import com.example.proyecto2.entidades.Orden;
import com.example.proyecto2.entidades.Producto;
import com.example.proyecto2.repository.ClienteRepository;
import com.example.proyecto2.repository.OrdenRepository;
import com.example.proyecto2.repository.ProductoRepository;
import com.example.proyecto2.entidades.Estados.EstadoOrden;

import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrdenService {

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

        return ordenRepository.findAll(pageable)
                .map(this::convertirRespuesta);
    }

    @Transactional(readOnly = true)
    public Page<OrdenResponse> listarPorEstado(
            EstadoOrden estado,
            Pageable pageable
    ) {
        return ordenRepository.findByEstado(estado, pageable)
                .map(this::convertirRespuesta);
    }

    @Transactional(readOnly = true)
    public Page<OrdenResponse> listarPorRangoFechas(
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin,
            Pageable pageable
    ) {
        if (fechaInicio.isAfter(fechaFin)) {
            throw new IllegalArgumentException(
                    "La fecha de inicio no puede ser posterior a la fecha final"
            );
        }

        return ordenRepository.findByFechaCreacionBetween(
                        fechaInicio,
                        fechaFin,
                        pageable
                )
                .map(this::convertirRespuesta);
    }



    @Transactional(readOnly = true)
    public Page<OrdenResponse> listarPorCliente(Long clienteId, Pageable pageable) {

        return ordenRepository.findByClienteId(clienteId, pageable)
                .map(this::convertirRespuesta);
    }

    @Transactional
    public OrdenResponse crear(OrdenRequest request) {

        // 1. Buscar al cliente
        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No existe el cliente indicado"
                        )
                );

        // 2. Crear la orden
        Orden orden = new Orden();
        orden.setCliente(cliente);
        orden.setEstado(EstadoOrden.CREATED);

        BigDecimal total = BigDecimal.ZERO;

        // 3. Procesar los productos solicitados
        for (DetalleOrdenRequest detalleRequest : request.getDetalles()) {

            Producto producto = productoRepository
                    .findById(detalleRequest.getProductoId())
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "No existe el producto con ID: "
                                            + detalleRequest.getProductoId()
                            )
                    );

            Integer cantidad = detalleRequest.getCantidad();

            // 4. Validar las existencias
            if (producto.getStock() < cantidad) {
                throw new IllegalStateException(
                        "Existencias insuficientes para el producto: "
                                + producto.getNombre()
                );
            }

            // 5. Crear el detalle y calcular su subtotal
            DetalleOrden detalle = new DetalleOrden();
            detalle.setProducto(producto);
            detalle.setCantidad(cantidad);
            detalle.setPrecioUnitario(producto.getPrecio());
            detalle.calcularSubtotal();

            // 6. Asociar el detalle con la orden
            orden.agregarDetalle(detalle);

            // 7. Sumar el subtotal al total
            total = total.add(detalle.getSubtotal());

            // 8. Descontar las existencias
            producto.setStock(producto.getStock() - cantidad);
            productoRepository.save(producto);
        }

        // 9. Establecer el total de la orden
        orden.setTotal(total);

        // 10. Guardar la orden y sus detalles
        Orden ordenGuardada = ordenRepository.save(orden);

        // 11. Devolver la respuesta
        return convertirRespuesta(ordenGuardada);
    }

    @Transactional(readOnly = true)
    public OrdenResponse buscarPorId(Long id) {

        Orden orden = ordenRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No existe la orden con ID: " + id
                        )
                );

        return convertirRespuesta(orden);
    }

//CONFIRMAR
    @Transactional
    public OrdenResponse confirmar(Long id) {
        Orden orden = ordenRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No existe la orden con ID: " + id
                        )
                );

        if (orden.getEstado() != EstadoOrden.CREATED) {
            throw new IllegalStateException(
                    "Solo se pueden confirmar órdenes con estado CREATED"
            );
        }

        orden.setEstado(EstadoOrden.CONFIRMED);

        Orden ordenActualizada = ordenRepository.save(orden);

        return convertirRespuesta(ordenActualizada);
    }

    //CANCELAR



    @Transactional
    public OrdenResponse cancelar(Long id) {
        Orden orden = ordenRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe la orden con ID: " + id
                ));

        if (orden.getEstado() == EstadoOrden.CANCELLED) {
            throw new IllegalStateException(
                    "La orden ya se encuentra cancelada"
            );
        }

        if (orden.getEstado() != EstadoOrden.CREATED
                && orden.getEstado() != EstadoOrden.CONFIRMED) {
            throw new IllegalStateException(
                    "Solo se pueden cancelar órdenes CREATED o CONFIRMED"
            );
        }

        for (DetalleOrden detalle : orden.getDetalles()) {
            Producto producto = detalle.getProducto();
            producto.setStock(producto.getStock() + detalle.getCantidad());
            productoRepository.save(producto);
        }

        orden.setEstado(EstadoOrden.CANCELLED);

        Orden ordenActualizada = ordenRepository.save(orden);

        return convertirRespuesta(ordenActualizada);
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

            DetalleOrdenResponse detalleResponse =
                    new DetalleOrdenResponse();

            detalleResponse.setProductoId(detalle.getProducto().getId());
            detalleResponse.setNombreProducto(
                    detalle.getProducto().getNombre()
            );
            detalleResponse.setCantidad(detalle.getCantidad());
            detalleResponse.setPrecioUnitario(
                    detalle.getPrecioUnitario()
            );
            detalleResponse.setSubtotal(detalle.getSubtotal());

            detallesResponse.add(detalleResponse);
        }

        response.setDetalles(detallesResponse);

        return response;
    }




}
