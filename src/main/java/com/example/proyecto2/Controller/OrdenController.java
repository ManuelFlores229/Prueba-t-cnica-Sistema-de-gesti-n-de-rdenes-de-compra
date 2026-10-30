package com.example.proyecto2.Controller;

import com.example.proyecto2.dto.orden.OrdenRequest;
import com.example.proyecto2.dto.orden.OrdenResponse;
import com.example.proyecto2.entidades.Estados.EstadoOrden;
import com.example.proyecto2.service.OrdenService;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/ordenes")
public class OrdenController {

    private final OrdenService ordenService;

    public OrdenController(OrdenService ordenService) {
        this.ordenService = ordenService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrdenResponse crear(
            @Valid @RequestBody OrdenRequest request
    ) {
        return ordenService.crear(request);
    }

    @GetMapping
    public Page<OrdenResponse> listar(
            @ParameterObject
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ordenService.listar(pageable);
    }

    @GetMapping("/{id}")
    public OrdenResponse buscarPorId(@PathVariable Long id) {
        return ordenService.buscarPorId(id);
    }

    @PatchMapping("/{id}/confirmar")
    public OrdenResponse confirmar(@PathVariable Long id) {
        return ordenService.confirmar(id);
    }

    @PatchMapping("/{id}/cancelar")
    public OrdenResponse cancelar(@PathVariable Long id) {
        return ordenService.cancelar(id);
    }

    @GetMapping("/estado")
    public Page<OrdenResponse> listarPorEstado(
            @RequestParam EstadoOrden estado,
            @ParameterObject
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ordenService.listarPorEstado(estado, pageable);
    }

    @GetMapping("/fechas")
    public Page<OrdenResponse> listarPorRangoFechas(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime fechaInicio,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime fechaFin,

            @ParameterObject
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ordenService.listarPorRangoFechas(
                fechaInicio,
                fechaFin,
                pageable
        );
    }
}