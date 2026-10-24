package com.example.proyecto2.mapper;

import com.example.proyecto2.dto.cliente.ClienteRequest;
import com.example.proyecto2.dto.cliente.ClienteResponse;
import com.example.proyecto2.entidades.Cliente;
import org.springframework.stereotype.Component;

@Component
public class ClienteMapper {

    public Cliente toEntity(ClienteRequest request) {
        Cliente cliente = new Cliente();
        cliente.setNombre(request.getNombre());
        cliente.setEmail(request.getEmail());
        cliente.setTelefono(request.getTelefono());
        return cliente;
    }

    public ClienteResponse toResponse(Cliente cliente) {
        ClienteResponse response = new ClienteResponse();
        response.setId(cliente.getId());
        response.setNombre(cliente.getNombre());
        response.setEmail(cliente.getEmail());
        response.setTelefono(cliente.getTelefono());
        response.setFechaRegistro(cliente.getFechaRegistro());
        response.setEstado(cliente.getEstado());
        return response;
    }
}
