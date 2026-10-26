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

            // 4. Crear el detalle y guardar el precio actual
            DetalleOrden detalle = new DetalleOrden();
            detalle.setProducto(producto);
            detalle.setCantidad(cantidad);
            detalle.setPrecioUnitario(producto.getPrecio());
            detalle.calcularSubtotal();

            // 5. Asociar el detalle con la orden
            orden.agregarDetalle(detalle);

            // 6. Calcular el total de la orden
            total = total.add(detalle.getSubtotal());
        }

        // 7. Establecer el total
        orden.setTotal(total);

        // 8. Guardar la orden y sus detalles
        Orden ordenGuardada = ordenRepository.save(orden);

        // 9. Devolver la respuesta
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

        // Comprobar y descontar las existencias de cada producto
        for (DetalleOrden detalle : orden.getDetalles()) {

            Producto producto = productoRepository
                    .findById(detalle.getProducto().getId())
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "No existe el producto con ID: "
                                            + detalle.getProducto().getId()
                            )
                    );

            if (producto.getStock() < detalle.getCantidad()) {
                throw new IllegalStateException(
                        "Existencias insuficientes para el producto: "
                                + producto.getNombre()
                );
            }

            producto.setStock(
                    producto.getStock() - detalle.getCantidad()
            );

            productoRepository.save(producto);
        }

        // Cambiar el estado solo después de procesar los productos
        orden.setEstado(EstadoOrden.CONFIRMED);

        Orden ordenActualizada = ordenRepository.save(orden);

        return convertirRespuesta(ordenActualizada);
    }


//CANCELAR
    @Transactional
    public OrdenResponse cancelar(Long id) {

        Orden orden = ordenRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No existe la orden con ID: " + id
                        )
                );

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

        // Solo devolver existencias si la orden estaba confirmada
        if (orden.getEstado() == EstadoOrden.CONFIRMED) {

            for (DetalleOrden detalle : orden.getDetalles()) {

                Producto producto = productoRepository
                        .findById(detalle.getProducto().getId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "No existe el producto con ID: "
                                                + detalle.getProducto().getId()
                                )
                        );

                producto.setStock(
                        producto.getStock() + detalle.getCantidad()
                );

                productoRepository.save(producto);
            }
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
