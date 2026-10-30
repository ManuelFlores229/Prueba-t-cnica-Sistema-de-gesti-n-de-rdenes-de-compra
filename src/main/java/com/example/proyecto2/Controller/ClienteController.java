package com.example.proyecto2.Controller;

import com.example.proyecto2.dto.cliente.ClienteRequest;
import com.example.proyecto2.dto.cliente.ClienteResponse;
import com.example.proyecto2.service.ClienteService;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.example.proyecto2.dto.orden.OrdenResponse;
import com.example.proyecto2.service.OrdenService;

@RestController
@RequestMapping("/api/v1/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    private final OrdenService ordenService;

    public ClienteController(ClienteService clienteService, OrdenService ordenService) {
        this.clienteService = clienteService;
        this.ordenService = ordenService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteResponse crear(
            @Valid @RequestBody ClienteRequest request
    ) {
        return clienteService.crear(request);
    }

    @GetMapping("/{id}")
    public ClienteResponse buscarPorId(@PathVariable Long id) {
        return clienteService.buscarPorId(id);
    }

    @GetMapping
    public Page<ClienteResponse> listar(
            @ParameterObject
            @PageableDefault(size = 10, sort = "nombre") Pageable pageable
    ) {
        return clienteService.listar(pageable);
    }

    @GetMapping("/buscar")
    public Page<ClienteResponse> buscarPorNombre(
            @RequestParam String nombre,
            @ParameterObject
            @PageableDefault(size = 10, sort = "nombre") Pageable pageable
    ) {
        return clienteService.buscarPorNombre(nombre, pageable);
    }

    @PutMapping("/{id}")
    public ClienteResponse actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ClienteRequest request
    ) {
        return clienteService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable Long id) {
        clienteService.desactivar(id);
    }

    @GetMapping("/{id}/ordenes")
    public Page<OrdenResponse> listarOrdenesPorCliente(
            @PathVariable Long id,
            @ParameterObject
            @PageableDefault(size = 10, sort = "fechaCreacion")
            Pageable pageable
    ) {
        return ordenService.listarPorCliente(id, pageable);
    }
}