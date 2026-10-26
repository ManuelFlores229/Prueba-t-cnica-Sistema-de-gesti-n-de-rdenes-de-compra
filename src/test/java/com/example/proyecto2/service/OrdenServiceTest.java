package com.example.proyecto2.service;

import com.example.proyecto2.entidades.DetalleOrden;
import com.example.proyecto2.entidades.Estados.EstadoOrden;
import com.example.proyecto2.repository.ClienteRepository;
import com.example.proyecto2.repository.OrdenRepository;
import com.example.proyecto2.repository.ProductoRepository;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


import com.example.proyecto2.repository.OrdenRepository;
import com.example.proyecto2.repository.ClienteRepository;
import com.example.proyecto2.repository.ProductoRepository;

import com.example.proyecto2.dto.orden.DetalleOrdenRequest;
import com.example.proyecto2.dto.orden.OrdenRequest;
import com.example.proyecto2.entidades.Cliente;
import com.example.proyecto2.entidades.Orden;
import com.example.proyecto2.entidades.Producto;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrdenServiceTest {

    @Test
    void primeraPrueba() {
        assertTrue(true);
    }

    @Mock
    private OrdenRepository ordenRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private OrdenService ordenService;

    @BeforeEach
    void preparar() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void debeCalcularCorrectamenteElTotalDeLaOrden() {
        // Arrange: preparar los datos simulados
        Cliente cliente = new Cliente();
        cliente.setId(1L);

        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Laptop");
        producto.setPrecio(new BigDecimal("12500.00"));
        producto.setStock(10);

        when(clienteRepository.findById(1L))
                .thenReturn(java.util.Optional.of(cliente));

        when(productoRepository.findById(1L))
                .thenReturn(java.util.Optional.of(producto));

        OrdenRequest request = new OrdenRequest();
        request.setClienteId(1L);

        when(ordenRepository.save(any(Orden.class)))
                .thenAnswer(invocation -> {
                    Orden orden = invocation.getArgument(0);
                    orden.setId(1L);
                    return orden;
                });

        DetalleOrdenRequest detalle = new DetalleOrdenRequest();
        detalle.setProductoId(1L);
        detalle.setCantidad(3);

        request.setDetalles(List.of(detalle));

        // Act: ejecutar el servicio
        var respuesta = ordenService.crear(request);

        // Assert: verificar el resultado
        assertEquals(
                new BigDecimal("37500.00"),
                respuesta.getTotal()
        );

        assertEquals("Laptop", respuesta.getDetalles().get(0).getNombreProducto());
        assertEquals(3, respuesta.getDetalles().get(0).getCantidad());
    }


    @Test
    void debeCrearOrdenAunqueNoHayaStockSuficienteHastaConfirmarla() {
        Cliente cliente = new Cliente();
        cliente.setId(1L);

        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Laptop");
        producto.setPrecio(new BigDecimal("12500.00"));
        producto.setStock(2);

        when(clienteRepository.findById(1L))
                .thenReturn(java.util.Optional.of(cliente));

        when(productoRepository.findById(1L))
                .thenReturn(java.util.Optional.of(producto));

        when(ordenRepository.save(any(Orden.class)))
                .thenAnswer(invocation -> {
                    Orden orden = invocation.getArgument(0);
                    orden.setId(1L);
                    return orden;
                });

        OrdenRequest request = new OrdenRequest();
        request.setClienteId(1L);

        DetalleOrdenRequest detalle = new DetalleOrdenRequest();
        detalle.setProductoId(1L);
        detalle.setCantidad(3);

        request.setDetalles(List.of(detalle));

        var respuesta = ordenService.crear(request);

        assertEquals(EstadoOrden.CREATED, respuesta.getEstado());
        assertEquals(new BigDecimal("37500.00"), respuesta.getTotal());
        assertEquals(2, producto.getStock());

        verify(productoRepository, never()).save(any(Producto.class));
    }




    @Test
    void debeCancelarUnaOrdenCreadaSinModificarStock() {
        Cliente cliente = new Cliente();
        cliente.setId(1L);

        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Laptop");
        producto.setPrecio(new BigDecimal("12500.00"));
        producto.setStock(7);

        Orden orden = new Orden();
        orden.setId(1L);
        orden.setCliente(cliente);
        orden.setEstado(EstadoOrden.CREATED);
        orden.setTotal(new BigDecimal("37500.00"));

        DetalleOrden detalle = new DetalleOrden();
        detalle.setProducto(producto);
        detalle.setCantidad(3);
        detalle.setPrecioUnitario(new BigDecimal("12500.00"));
        detalle.calcularSubtotal();

        orden.agregarDetalle(detalle);

        when(ordenRepository.findById(1L))
                .thenReturn(java.util.Optional.of(orden));

        when(ordenRepository.save(any(Orden.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var respuesta = ordenService.cancelar(1L);

        assertEquals(EstadoOrden.CANCELLED, respuesta.getEstado());
        assertEquals(7, producto.getStock());

        verify(productoRepository, never()).save(any(Producto.class));
        verify(ordenRepository).save(orden);
    }


    @Test
    void noDebeCancelarUnaOrdenDosVeces() {
        Orden orden = new Orden();
        orden.setId(1L);
        orden.setEstado(EstadoOrden.CANCELLED);

        when(ordenRepository.findById(1L))
                .thenReturn(java.util.Optional.of(orden));

        IllegalStateException excepcion = assertThrows(
                IllegalStateException.class,
                () -> ordenService.cancelar(1L)
        );

        assertEquals(
                "La orden ya se encuentra cancelada",
                excepcion.getMessage()
        );

        verify(productoRepository, never()).save(any(Producto.class));
    }



    @Test
    void debeConfirmarUnaOrdenCreada() {
        Cliente cliente = new Cliente();
        cliente.setId(1L);

        Orden orden = new Orden();
        orden.setId(1L);
        orden.setCliente(cliente);
        orden.setEstado(EstadoOrden.CREATED);

        when(ordenRepository.findById(1L))
                .thenReturn(java.util.Optional.of(orden));

        when(ordenRepository.save(any(Orden.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var respuesta = ordenService.confirmar(1L);

        assertEquals(EstadoOrden.CONFIRMED, respuesta.getEstado());

        verify(ordenRepository).save(orden);
    }


    @Test
    void debeDescontarStockAlConfirmarUnaOrden() {
        Cliente cliente = new Cliente();
        cliente.setId(1L);

        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Laptop");
        producto.setPrecio(new BigDecimal("12500.00"));
        producto.setStock(10);

        Orden orden = new Orden();
        orden.setId(1L);
        orden.setCliente(cliente);
        orden.setEstado(EstadoOrden.CREATED);
        orden.setTotal(new BigDecimal("37500.00"));

        DetalleOrden detalle = new DetalleOrden();
        detalle.setProducto(producto);
        detalle.setCantidad(3);
        detalle.setPrecioUnitario(new BigDecimal("12500.00"));
        detalle.calcularSubtotal();
        orden.agregarDetalle(detalle);

        when(ordenRepository.findById(1L))
                .thenReturn(java.util.Optional.of(orden));

        when(productoRepository.findById(1L))
                .thenReturn(java.util.Optional.of(producto));

        when(productoRepository.save(any(Producto.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(ordenRepository.save(any(Orden.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var respuesta = ordenService.confirmar(1L);

        assertEquals(7, producto.getStock());
        assertEquals(EstadoOrden.CONFIRMED, respuesta.getEstado());

        verify(productoRepository).save(producto);
        verify(ordenRepository).save(orden);
    }


    @Test
    void noDebeConfirmarUnaOrdenSiNoHayStockSuficiente() {
        Cliente cliente = new Cliente();
        cliente.setId(1L);

        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Laptop");
        producto.setPrecio(new BigDecimal("12500.00"));
        producto.setStock(2);

        Orden orden = new Orden();
        orden.setId(1L);
        orden.setCliente(cliente);
        orden.setEstado(EstadoOrden.CREATED);
        orden.setTotal(new BigDecimal("37500.00"));

        DetalleOrden detalle = new DetalleOrden();
        detalle.setProducto(producto);
        detalle.setCantidad(3);
        detalle.setPrecioUnitario(new BigDecimal("12500.00"));
        detalle.calcularSubtotal();
        orden.agregarDetalle(detalle);

        when(ordenRepository.findById(1L))
                .thenReturn(java.util.Optional.of(orden));

        when(productoRepository.findById(1L))
                .thenReturn(java.util.Optional.of(producto));

        IllegalStateException excepcion = assertThrows(
                IllegalStateException.class,
                () -> ordenService.confirmar(1L)
        );

        assertEquals(
                "Existencias insuficientes para el producto: Laptop",
                excepcion.getMessage()
        );

        assertEquals(2, producto.getStock());
        assertEquals(EstadoOrden.CREATED, orden.getEstado());

        verify(productoRepository, never()).save(any(Producto.class));
        verify(ordenRepository, never()).save(any(Orden.class));
    }


    @Test
    void noDebeConfirmarUnaOrdenCancelada() {
        Orden orden = new Orden();
        orden.setId(1L);
        orden.setEstado(EstadoOrden.CANCELLED);

        when(ordenRepository.findById(1L))
                .thenReturn(java.util.Optional.of(orden));

        IllegalStateException excepcion = assertThrows(
                IllegalStateException.class,
                () -> ordenService.confirmar(1L)
        );

        assertEquals(
                "Solo se pueden confirmar órdenes con estado CREATED",
                excepcion.getMessage()
        );

        verify(ordenRepository, never()).save(any(Orden.class));
    }

    @Test
    void debeLanzarExcepcionSiLaOrdenNoExiste() {
        when(ordenRepository.findById(999L))
                .thenReturn(java.util.Optional.empty());

        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> ordenService.buscarPorId(999L)
        );

        assertEquals(
                "No existe la orden con ID: 999",
                excepcion.getMessage()
        );
    }

    @Test
    void noDebeCrearOrdenSiElClienteNoExiste() {
        OrdenRequest request = new OrdenRequest();
        request.setClienteId(999L);

        when(clienteRepository.findById(999L))
                .thenReturn(java.util.Optional.empty());

        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> ordenService.crear(request)
        );

        assertEquals(
                "No existe el cliente indicado",
                excepcion.getMessage()
        );

        verify(productoRepository, never()).save(any(Producto.class));
        verify(ordenRepository, never()).save(any(Orden.class));
    }

    @Test
    void noDebeCrearOrdenSiElProductoNoExiste() {
        Cliente cliente = new Cliente();
        cliente.setId(1L);

        OrdenRequest request = new OrdenRequest();
        request.setClienteId(1L);

        DetalleOrdenRequest detalleRequest = new DetalleOrdenRequest();
        detalleRequest.setProductoId(999L);
        detalleRequest.setCantidad(2);

        request.setDetalles(java.util.List.of(detalleRequest));

        when(clienteRepository.findById(1L))
                .thenReturn(java.util.Optional.of(cliente));

        when(productoRepository.findById(999L))
                .thenReturn(java.util.Optional.empty());

        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> ordenService.crear(request)
        );

        assertEquals(
                "No existe el producto con ID: 999",
                excepcion.getMessage()
        );

        verify(ordenRepository, never()).save(any(Orden.class));
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    void debeCancelarUnaOrdenConfirmadaYRestaurarStock() {
        Cliente cliente = new Cliente();
        cliente.setId(1L);

        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Laptop");
        producto.setPrecio(new java.math.BigDecimal("12500.00"));
        producto.setStock(7);

        Orden orden = new Orden();
        orden.setId(1L);
        orden.setCliente(cliente);
        orden.setEstado(EstadoOrden.CONFIRMED);
        orden.setTotal(new java.math.BigDecimal("37500.00"));

        DetalleOrden detalle = new DetalleOrden();
        detalle.setProducto(producto);
        detalle.setCantidad(3);
        detalle.setPrecioUnitario(new java.math.BigDecimal("12500.00"));
        detalle.calcularSubtotal();

        orden.agregarDetalle(detalle);

        when(ordenRepository.findById(1L))
                .thenReturn(java.util.Optional.of(orden));

        when(productoRepository.findById(1L))
                .thenReturn(java.util.Optional.of(producto));

        when(productoRepository.save(any(Producto.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(ordenRepository.save(any(Orden.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var respuesta = ordenService.cancelar(1L);

        assertEquals(EstadoOrden.CANCELLED, respuesta.getEstado());
        assertEquals(10, producto.getStock());

        verify(productoRepository).save(producto);
        verify(ordenRepository).save(orden);
    }


    @Test
    void debeBuscarUnaOrdenPorId() {
        Cliente cliente = new Cliente();
        cliente.setId(1L);

        Orden orden = new Orden();
        orden.setId(1L);
        orden.setCliente(cliente);
        orden.setEstado(EstadoOrden.CREATED);
        orden.setTotal(new java.math.BigDecimal("5000.00"));

        when(ordenRepository.findById(1L))
                .thenReturn(java.util.Optional.of(orden));

        var respuesta = ordenService.buscarPorId(1L);

        assertEquals(1L, respuesta.getId());
        assertEquals(1L, respuesta.getClienteId());
        assertEquals(EstadoOrden.CREATED, respuesta.getEstado());
        assertEquals(
                new java.math.BigDecimal("5000.00"),
                respuesta.getTotal()
        );

        verify(ordenRepository).findById(1L);
    }

    @Test
    void debeListarOrdenesConPaginacion() {
        Cliente cliente = new Cliente();
        cliente.setId(1L);

        Orden orden = new Orden();
        orden.setId(1L);
        orden.setCliente(cliente);
        orden.setEstado(EstadoOrden.CREATED);
        orden.setTotal(new java.math.BigDecimal("5000.00"));

        org.springframework.data.domain.Page<Orden> paginaOrdenes =
                new org.springframework.data.domain.PageImpl<>(
                        java.util.List.of(orden),
                        org.springframework.data.domain.PageRequest.of(0, 5),
                        1
                );

        when(ordenRepository.findAll(
                org.mockito.ArgumentMatchers.any(
                        org.springframework.data.domain.Pageable.class
                )
        )).thenReturn(paginaOrdenes);

        var respuesta = ordenService.listar(
                org.springframework.data.domain.PageRequest.of(0, 5)
        );

        assertEquals(1, respuesta.getTotalElements());
        assertEquals(1, respuesta.getContent().size());
        assertEquals(1L, respuesta.getContent().get(0).getId());
        assertEquals(EstadoOrden.CREATED,
                respuesta.getContent().get(0).getEstado());

        verify(ordenRepository).findAll(
                org.mockito.ArgumentMatchers.any(
                        org.springframework.data.domain.Pageable.class
                )
        );
    }

    @Test
    void noDebeConfirmarUnaOrdenQueNoExiste() {
        when(ordenRepository.findById(999L))
                .thenReturn(java.util.Optional.empty());

        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> ordenService.confirmar(999L)
        );

        assertEquals(
                "No existe la orden con ID: 999",
                excepcion.getMessage()
        );

        verify(ordenRepository, never()).save(any(Orden.class));
        verify(productoRepository, never()).save(any(Producto.class));
    }


}