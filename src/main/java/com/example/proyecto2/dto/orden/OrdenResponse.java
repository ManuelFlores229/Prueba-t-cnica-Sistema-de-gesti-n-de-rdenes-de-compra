package com.example.proyecto2.dto.orden;

import com.example.proyecto2.entidades.Estados.EstadoOrden;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrdenResponse {

    private Long id;
    private Long clienteId;
    private LocalDateTime fechaCreacion;
    private BigDecimal total;
    private EstadoOrden estado;
    private List<DetalleOrdenResponse> detalles;

    public OrdenResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public EstadoOrden getEstado() {
        return estado;
    }

    public void setEstado(EstadoOrden estado) {
        this.estado = estado;
    }

    public List<DetalleOrdenResponse> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleOrdenResponse> detalles) {
        this.detalles = detalles;
    }
}