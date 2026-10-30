package com.example.proyecto2.service;

import com.example.proyecto2.dto.producto.ProductoFiltro;
import com.example.proyecto2.dto.producto.ProductoRequest;
import com.example.proyecto2.dto.producto.ProductoResponse;
import com.example.proyecto2.entidades.Estados.EstadoRegistro;
import com.example.proyecto2.entidades.Producto;
import com.example.proyecto2.exception.ConflictoNegocioException;
import com.example.proyecto2.exception.RecursoNoEncontradoException;
import com.example.proyecto2.exception.SolicitudInvalidaException;
import com.example.proyecto2.mapper.ProductoMapper;
import com.example.proyecto2.repository.ProductoRepository;
import com.example.proyecto2.repository.ProductoSpecs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.Set;

@Service
public class ProductoService {
    private static final Set<String> CAMPOS_ORDENABLES = Set.of(
            "id", "nombre", "categoria", "precio", "stock", "fechaCreacion", "estado"
    );

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
            throw new ConflictoNegocioException(
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
    public Page<ProductoResponse> listar(
            ProductoFiltro filtro,
            Pageable pageable
    ) {
        Ordenamiento.validar(pageable, CAMPOS_ORDENABLES);
        validarRangoPrecios(filtro.getPrecioMin(), filtro.getPrecioMax());

        return productoRepository
                .findAll(construirFiltros(filtro), pageable)
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
            throw new ConflictoNegocioException(
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
            throw new SolicitudInvalidaException(
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
                        new RecursoNoEncontradoException(
                                "No existe un producto con el ID " + id
                        )
                );
    }

    private void validarProducto(ProductoRequest request) {
        if (request.getPrecio() == null
                || request.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            throw new SolicitudInvalidaException(
                    "El precio debe ser mayor que cero"
            );
        }

        if (request.getStock() == null || request.getStock() < 0) {
            throw new SolicitudInvalidaException(
                    "El stock no puede ser negativo"
            );
        }
    }
    private Specification<Producto> construirFiltros(ProductoFiltro filtro) {
        EstadoRegistro estado = Optional.ofNullable(filtro.getEstado())
                .orElse(EstadoRegistro.ACTIVO);

        return Stream.of(
                        Optional.ofNullable(filtro.getNombre())
                                .filter(texto -> !texto.isBlank())
                                .map(ProductoSpecs::nombreContiene),
                        Optional.ofNullable(filtro.getCategoria())
                                .filter(texto -> !texto.isBlank())
                                .map(ProductoSpecs::conCategoria),
                        Optional.ofNullable(filtro.getPrecioMin())
                                .map(ProductoSpecs::precioDesde),
                        Optional.ofNullable(filtro.getPrecioMax())
                                .map(ProductoSpecs::precioHasta),
                        Optional.ofNullable(filtro.getConStock())
                                .filter(Boolean::booleanValue)
                                .map(pedido -> ProductoSpecs.conStock()),
                        Optional.of(ProductoSpecs.conEstado(estado))
                )
                .flatMap(Optional::stream)
                .reduce(Specification::and)
                .orElseThrow();
    }

    private void validarRangoPrecios(BigDecimal minimo, BigDecimal maximo) {
        if (minimo != null && minimo.compareTo(BigDecimal.ZERO) < 0) {
            throw new SolicitudInvalidaException(
                    "El precio mínimo no puede ser negativo"
            );
        }

        if (minimo != null && maximo != null && minimo.compareTo(maximo) > 0) {
            throw new SolicitudInvalidaException(
                    "El rango de precios no es válido"
            );
        }
    }
}