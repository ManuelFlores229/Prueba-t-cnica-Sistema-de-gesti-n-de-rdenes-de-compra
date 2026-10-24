package com.example.proyecto2.mapper;

import com.example.proyecto2.dto.producto.ProductoRequest;
import com.example.proyecto2.dto.producto.ProductoResponse;
import com.example.proyecto2.entidades.Producto;
import org.springframework.stereotype.Component;

@Component
public class ProductoMapper {

    public Producto toEntity(ProductoRequest request) {
        Producto producto = new Producto();
        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setCategoria(request.getCategoria());
        producto.setPrecio(request.getPrecio());
        producto.setStock(request.getStock());
        return producto;
    }

    public ProductoResponse toResponse(Producto producto) {
        ProductoResponse response = new ProductoResponse();
        response.setId(producto.getId());
        response.setNombre(producto.getNombre());
        response.setDescripcion(producto.getDescripcion());
        response.setCategoria(producto.getCategoria());
        response.setPrecio(producto.getPrecio());
        response.setStock(producto.getStock());
        response.setFechaCreacion(producto.getFechaCreacion());
        response.setEstado(producto.getEstado());
        return response;
    }
}
