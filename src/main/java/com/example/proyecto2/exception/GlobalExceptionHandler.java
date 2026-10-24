
package com.example.proyecto2.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail manejarArgumentoInvalido(
            IllegalArgumentException ex) {

        ProblemDetail problema = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );

        problema.setTitle("Recurso no encontrado");

        return problema;
    }

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail manejarEstadoInvalido(
            IllegalStateException ex) {

        ProblemDetail problema = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                ex.getMessage()
        );

        problema.setTitle("Operación no permitida");

        return problema;
    }


    @ExceptionHandler(
            org.springframework.web.bind.MethodArgumentNotValidException.class
    )
    public ProblemDetail manejarErroresValidacion(
            org.springframework.web.bind.MethodArgumentNotValidException ex) {

        java.util.List<String> errores =
                ex.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(error -> error.getField()
                                + ": " + error.getDefaultMessage())
                        .distinct()
                        .toList();

        ProblemDetail problema = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "La solicitud contiene datos inválidos"
        );

        problema.setTitle("Error de validación");
        problema.setProperty("errores", errores);

        return problema;
    }

}
