package com.example.proyecto2.service;

import com.example.proyecto2.exception.SolicitudInvalidaException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;
import java.util.stream.Collectors;

public final class Ordenamiento {

    private Ordenamiento() {
    }

    public static void validar(Pageable pageable, Set<String> permitidos) {
        pageable.getSort().stream()
                .map(Sort.Order::getProperty)
                .filter(propiedad -> !permitidos.contains(propiedad))
                .findFirst()
                .ifPresent(propiedad -> {
                    throw new SolicitudInvalidaException(
                            "No se puede ordenar por '" + propiedad
                                    + "'. Campos permitidos: "
                                    + permitidos.stream()
                                    .sorted()
                                    .collect(Collectors.joining(", "))
                    );
                });
    }
}