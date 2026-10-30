package com.example.proyecto2.dto.producto;

import com.example.proyecto2.entidades.Estados.EstadoRegistro;

import java.math.BigDecimal;

public class ProductoFiltro {

    private String nombre;
    private String categoria;
    private Boolean conStock;
    private BigDecimal precioMin;
    private BigDecimal precioMax;
    private EstadoRegistro estado;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public Boolean getConStock() {
        return conStock;
    }

    public void setConStock(Boolean conStock) {
        this.conStock = conStock;
    }

    public BigDecimal getPrecioMin() {
        return precioMin;
    }

    public void setPrecioMin(BigDecimal precioMin) {
        this.precioMin = precioMin;
    }

    public BigDecimal getPrecioMax() {
        return precioMax;
    }

    public void setPrecioMax(BigDecimal precioMax) {
        this.precioMax = precioMax;
    }

    public EstadoRegistro getEstado() {
        return estado;
    }

    public void setEstado(EstadoRegistro estado) {
        this.estado = estado;
    }
}
