package com.example.proyecto2.dto.orden;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

public class OrdenRequest {

    @NotNull(message = "El cliente es obligatorio")
    private Long clienteId;

    @NotEmpty(message = "La orden debe contener al menos un producto")
    @Valid
    private List<DetalleOrdenRequest> detalles = new ArrayList<>();

    public OrdenRequest() {
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public List<DetalleOrdenRequest> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleOrdenRequest> detalles) {
        this.detalles = detalles;
    }
}