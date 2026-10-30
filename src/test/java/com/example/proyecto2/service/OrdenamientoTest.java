package com.example.proyecto2.service;

import com.example.proyecto2.exception.SolicitudInvalidaException;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrdenamientoTest {

    private static final Set<String> PERMITIDOS = Set.of("nombre", "precio");

    @Test
    void aceptaCamposPermitidos() {
        Pageable pageable = PageRequest.of(
                0, 10, Sort.by("nombre").and(Sort.by("precio").descending())
        );

        assertDoesNotThrow(() -> Ordenamiento.validar(pageable, PERMITIDOS));
    }

    @Test
    void aceptaUnaPaginaSinOrden() {
        Pageable pageable = PageRequest.of(0, 10);

        assertDoesNotThrow(() -> Ordenamiento.validar(pageable, PERMITIDOS));
    }

    @Test
    void rechazaUnCampoQueNoEstaPermitido() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("campoInventado"));

        SolicitudInvalidaException error = assertThrows(
                SolicitudInvalidaException.class,
                () -> Ordenamiento.validar(pageable, PERMITIDOS)
        );

        assertTrue(error.getMessage().contains("campoInventado"));
    }
}
