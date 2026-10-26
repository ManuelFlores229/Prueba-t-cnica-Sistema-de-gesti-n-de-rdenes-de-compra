package com.example.proyecto2.service;

import com.example.proyecto2.dto.producto.ProductoRequest;
import com.example.proyecto2.dto.producto.ProductoResponse;
import com.example.proyecto2.entidades.Estados.EstadoRegistro;
import com.example.proyecto2.entidades.Producto;
import com.example.proyecto2.mapper.ProductoMapper;
import com.example.proyecto2.repository.ProductoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final ProductoMapper productoMapper;

    public ProductoService(
            ProductoRepository productoRepository,
            ProductoMapper productoMapper
    ) {
        this.productoRepository = productoRepository;
        this.productoMapper = productoMapper;
    }

    @Transactional
    public ProductoResponse crear(ProductoRequest request) {
        validarProducto(request);

        if (productoRepository
                .existsByNombreIgnoreCaseAndCategoriaIgnoreCase(
                        request.getNombre(),
                        request.getCategoria()
                )) {
            throw new IllegalArgumentException(
                    "Ya existe un producto con ese nombre y categoría"
            );
        }

        Producto producto = productoMapper.toEntity(request);
        Producto productoGuardado = productoRepository.save(producto);

        return productoMapper.toResponse(productoGuardado);
    }

    @Transactional(readOnly = true)
    public ProductoResponse buscarPorId(Long id) {
        return productoMapper.toResponse(obtenerProducto(id));
    }

    @Transactional(readOnly = true)
    public Page<ProductoResponse> listar(Pageable pageable) {
        return productoRepository.findAll(pageable)
                .map(productoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProductoResponse> buscarPorNombre(
            String nombre,
            Pageable pageable
    ) {
        return productoRepository
                .findByNombreContainingIgnoreCase(nombre, pageable)
                .map(productoMapper::toResponse);
    }
    @Transactional(readOnly = true)
    public Page<ProductoResponse> buscarPorRangoPrecio(
            BigDecimal precioMinimo,
            BigDecimal precioMaximo,
            Pageable pageable
    ) {
        if (precioMinimo == null || precioMaximo == null) {
            throw new IllegalArgumentException(
                    "El precio mínimo y máximo son obligatorios"
            );
        }

        if (precioMinimo.compareTo(BigDecimal.ZERO) < 0
                || precioMaximo.compareTo(precioMinimo) < 0) {
            throw new IllegalArgumentException(
                    "El rango de precios no es válido"
            );
        }

        return productoRepository.findByPrecioBetween(
                        precioMinimo,
                        precioMaximo,
                        pageable
                )
                .map(productoMapper::toResponse);
    }

    @Transactional
    public ProductoResponse actualizar(
            Long id,
            ProductoRequest request
    ) {
        validarProducto(request);

        Producto producto = obtenerProducto(id);

        boolean nombreCategoriaEnUso =
                productoRepository
                        .existsByNombreIgnoreCaseAndCategoriaIgnoreCase(
                                request.getNombre(),
                                request.getCategoria()
                        )
                        && (
                        !producto.getNombre()
                                .equalsIgnoreCase(request.getNombre())
                                || !producto.getCategoria()
                                .equalsIgnoreCase(request.getCategoria())
                );

        if (nombreCategoriaEnUso) {
            throw new IllegalArgumentException(
                    "Ya existe otro producto con ese nombre y categoría"
            );
        }

        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setCategoria(request.getCategoria());
        producto.setPrecio(request.getPrecio());
        producto.setStock(request.getStock());

        Producto actualizado = productoRepository.save(producto);

        return productoMapper.toResponse(actualizado);
    }

    @Transactional
    public ProductoResponse actualizarStock(Long id, Integer nuevoStock) {
        if (nuevoStock == null || nuevoStock < 0) {
            throw new IllegalArgumentException(
                    "El stock no puede ser negativo"
            );
        }

        Producto producto = obtenerProducto(id);
        producto.setStock(nuevoStock);

        Producto actualizado = productoRepository.save(producto);

        return productoMapper.toResponse(actualizado);
    }

    @Transactional
    public void desactivar(Long id) {
        Producto producto = obtenerProducto(id);
        producto.setEstado(EstadoRegistro.INACTIVO);
        productoRepository.save(producto);
    }

    private Producto obtenerProducto(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No existe un producto con el ID " + id
                        )
                );
    }

    private void validarProducto(ProductoRequest request) {
        if (request.getPrecio() == null
                || request.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "El precio debe ser mayor que cero"
            );
        }

        if (request.getStock() == null || request.getStock() < 0) {
            throw new IllegalArgumentException(
                    "El stock no puede ser negativo"
            );
        }
    }
}