package com.example.proyecto2.exception;

public class ConflictoNegocioException extends RuntimeException {

    public ConflictoNegocioException(String mensaje) {
        super(mensaje);
    }
}