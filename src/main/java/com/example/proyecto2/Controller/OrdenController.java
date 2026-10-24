
package com.example.proyecto2.Controller;

import com.example.proyecto2.dto.orden.OrdenRequest;
import com.example.proyecto2.dto.orden.OrdenResponse;
import com.example.proyecto2.service.OrdenService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

@RestController
@RequestMapping("/api/v1/ordenes")
public class OrdenController {

    private final OrdenService ordenService;

    public OrdenController(OrdenService ordenService) {
        this.ordenService = ordenService;
    }

    // Registrar una nueva orden
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrdenResponse crear(
            @Valid @RequestBody OrdenRequest request
    ) {
        return ordenService.crear(request);
    }
    // Listar órdenes con paginación
    @GetMapping
    public Page<OrdenResponse> listar(
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ordenService.listar(pageable);
    }

    // Consultar una orden por su ID
    @GetMapping("/{id}")
    public OrdenResponse buscarPorId(@PathVariable Long id) {
        return ordenService.buscarPorId(id);
    }


    //CONFIRMAR
    @PatchMapping("/{id}/confirmar")
    public OrdenResponse confirmar(@PathVariable Long id) {
        return ordenService.confirmar(id);
    }

    //CANCELAR

    @PatchMapping("/{id}/cancelar")
    public OrdenResponse cancelar(@PathVariable Long id) {
        return ordenService.cancelar(id);
    }


}


