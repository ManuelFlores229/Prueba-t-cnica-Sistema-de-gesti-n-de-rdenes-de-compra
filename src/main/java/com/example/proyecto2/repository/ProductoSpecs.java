package com.example.proyecto2.repository;

import com.example.proyecto2.entidades.Estados.EstadoRegistro;
import com.example.proyecto2.entidades.Producto;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public final class ProductoSpecs {

    private ProductoSpecs() {
    }

    public static Specification<Producto> nombreContiene(String texto) {
        String patron = "%" + texto.trim().toLowerCase() + "%";
        return (producto, consulta, cb) ->
                cb.like(cb.lower(producto.get("nombre")), patron);
    }

    public static Specification<Producto> conCategoria(String categoria) {
        String buscada = categoria.trim().toLowerCase();
        return (producto, consulta, cb) ->
                cb.equal(cb.lower(producto.get("categoria")), buscada);
    }

    public static Specification<Producto> precioDesde(BigDecimal minimo) {
        return (producto, consulta, cb) ->
                cb.greaterThanOrEqualTo(producto.<BigDecimal>get("precio"), minimo);
    }

    public static Specification<Producto> precioHasta(BigDecimal maximo) {
        return (producto, consulta, cb) ->
                cb.lessThanOrEqualTo(producto.<BigDecimal>get("precio"), maximo);
    }

    public static Specification<Producto> conStock() {
        return (producto, consulta, cb) ->
                cb.greaterThan(producto.<Integer>get("stock"), 0);
    }

    public static Specification<Producto> conEstado(EstadoRegistro estado) {
        return (producto, consulta, cb) ->
                cb.equal(producto.get("estado"), estado);
    }
}