package com.example.proyecto2.service;

import com.example.proyecto2.dto.cliente.ClienteRequest;
import com.example.proyecto2.dto.cliente.ClienteResponse;
import com.example.proyecto2.entidades.Cliente;
import com.example.proyecto2.exception.ConflictoNegocioException;
import com.example.proyecto2.exception.RecursoNoEncontradoException;
import com.example.proyecto2.mapper.ClienteMapper;
import com.example.proyecto2.repository.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    private ClienteService clienteService;

    @BeforeEach
    void preparar() {
        MockitoAnnotations.openMocks(this);
        clienteService = new ClienteService(clienteRepository, new ClienteMapper());
    }

    private ClienteRequest solicitud(String email) {
        ClienteRequest request = new ClienteRequest();
        request.setNombre("Ana Lopez");
        request.setEmail(email);
        request.setTelefono("5551234567");
        return request;
    }

    @Test
    void rechazaUnCorreoYaRegistrado() {
        when(clienteRepository.existsByEmailIgnoreCase("ana@test.com"))
                .thenReturn(true);

        assertThrows(
                ConflictoNegocioException.class,
                () -> clienteService.crear(solicitud("ana@test.com"))
        );

        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    void guardaUnClienteConCorreoNuevo() {
        when(clienteRepository.existsByEmailIgnoreCase("ana@test.com"))
                .thenReturn(false);
        when(clienteRepository.save(any(Cliente.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        ClienteResponse respuesta = clienteService.crear(solicitud("ana@test.com"));

        assertEquals("ana@test.com", respuesta.getEmail());
        verify(clienteRepository).save(any(Cliente.class));
    }

    @Test
    void lanzaNoEncontradoSiElClienteNoExiste() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        RecursoNoEncontradoException error = assertThrows(
                RecursoNoEncontradoException.class,
                () -> clienteService.buscarPorId(99L)
        );

        assertTrue(error.getMessage().contains("99"));
    }
}