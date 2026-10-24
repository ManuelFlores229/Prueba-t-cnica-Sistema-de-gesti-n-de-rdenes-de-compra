package com.example.proyecto2.Controller;

import com.example.proyecto2.dto.cliente.ClienteRequest;
import com.example.proyecto2.dto.cliente.ClienteResponse;
import com.example.proyecto2.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
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
            @PageableDefault(size = 10, sort = "nombre") Pageable pageable
    ) {
        return clienteService.listar(pageable);
    }

    @GetMapping("/buscar")
    public Page<ClienteResponse> buscarPorNombre(
            @RequestParam String nombre,
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

    @PatchMapping("/{id}/desactivar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable Long id) {
        clienteService.desactivar(id);
    }
}
