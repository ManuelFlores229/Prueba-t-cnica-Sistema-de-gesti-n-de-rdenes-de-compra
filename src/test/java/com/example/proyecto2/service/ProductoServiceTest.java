package com.example.proyecto2.service;

import com.example.proyecto2.dto.producto.ProductoFiltro;
import com.example.proyecto2.dto.producto.ProductoRequest;
import com.example.proyecto2.dto.producto.ProductoResponse;
import com.example.proyecto2.entidades.Producto;
import com.example.proyecto2.exception.ConflictoNegocioException;
import com.example.proyecto2.exception.SolicitudInvalidaException;
import com.example.proyecto2.mapper.ProductoMapper;
import com.example.proyecto2.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    private ProductoService productoService;

    @BeforeEach
    void preparar() {
        MockitoAnnotations.openMocks(this);
        productoService = new ProductoService(
                productoRepository, new ProductoMapper()
        );
    }

    private ProductoRequest solicitud(String precio, int stock) {
        ProductoRequest request = new ProductoRequest();
        request.setNombre("Monitor");
        request.setCategoria("Computo");
        request.setPrecio(new BigDecimal(precio));
        request.setStock(stock);
        return request;
    }

    @Test
    void rechazaUnProductoConPrecioNegativo() {
        SolicitudInvalidaException error = assertThrows(
                SolicitudInvalidaException.class,
                () -> productoService.crear(solicitud("-10.00", 5))
        );

        assertEquals("El precio debe ser mayor que cero", error.getMessage());
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    void rechazaUnProductoDuplicado() {
        when(productoRepository
                .existsByNombreIgnoreCaseAndCategoriaIgnoreCase("Monitor", "Computo"))
                .thenReturn(true);

        assertThrows(
                ConflictoNegocioException.class,
                () -> productoService.crear(solicitud("100.00", 5))
        );

        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    void rechazaUnRangoDePreciosInvertido() {
        ProductoFiltro filtro = new ProductoFiltro();
        filtro.setPrecioMin(new BigDecimal("500"));
        filtro.setPrecioMax(new BigDecimal("100"));

        assertThrows(
                SolicitudInvalidaException.class,
                () -> productoService.listar(filtro, PageRequest.of(0, 10))
        );

        verify(productoRepository, never()).findAll(
                ArgumentMatchers.<Specification<Producto>>any(),
                any(Pageable.class)
        );
    }

    @Test
    void rechazaOrdenarPorUnCampoDesconocido() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("campoInventado"));

        assertThrows(
                SolicitudInvalidaException.class,
                () -> productoService.listar(new ProductoFiltro(), pageable)
        );
    }

    @Test
    void devuelveLaPaginaConLosProductosMapeados() {
        Producto producto = new Producto();
        producto.setNombre("Monitor");
        producto.setCategoria("Computo");
        producto.setPrecio(new BigDecimal("100.00"));
        producto.setStock(5);

        Pageable pageable = PageRequest.of(0, 10);
        when(productoRepository.findAll(
                ArgumentMatchers.<Specification<Producto>>any(), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(producto), pageable, 1));

        Page<ProductoResponse> pagina =
                productoService.listar(new ProductoFiltro(), pageable);

        assertEquals(1, pagina.getTotalElements());
        assertEquals("Monitor", pagina.getContent().get(0).getNombre());
    }
}