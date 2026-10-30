package com.example.proyecto2.Controller;

import com.example.proyecto2.dto.producto.ProductoRequest;
import com.example.proyecto2.dto.producto.ProductoResponse;
import com.example.proyecto2.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.example.proyecto2.dto.producto.ProductoFiltro;
import org.springdoc.core.annotations.ParameterObject;

@RestController
@RequestMapping("/api/v1/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductoResponse crear(
            @Valid @RequestBody ProductoRequest request
    ) {
        return productoService.crear(request);
    }

    @GetMapping("/{id}")
    public ProductoResponse buscarPorId(@PathVariable Long id) {
        return productoService.buscarPorId(id);
    }

    @GetMapping
    public Page<ProductoResponse> listar(
            @ParameterObject ProductoFiltro filtro,
            @ParameterObject
            @PageableDefault(size = 10, sort = "nombre")
            Pageable pageable
    ) {
        return productoService.listar(filtro, pageable);
    }

    @PutMapping("/{id}")
    public ProductoResponse actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ProductoRequest request
    ) {
        return productoService.actualizar(id, request);
    }

    @PatchMapping("/{id}/stock")
    public ProductoResponse actualizarStock(
            @PathVariable Long id,
            @RequestParam Integer cantidad
    ) {
        return productoService.actualizarStock(id, cantidad);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable Long id) {
        productoService.desactivar(id);
    }
}