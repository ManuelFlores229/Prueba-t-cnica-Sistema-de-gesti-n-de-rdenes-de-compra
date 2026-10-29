
package com.example.proyecto2.Controller;

import com.example.proyecto2.exception.RecursoNoEncontradoException;
import com.example.proyecto2.service.OrdenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.proyecto2.dto.orden.OrdenResponse;

import static org.mockito.Mockito.verify;


import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;


@WebMvcTest(OrdenController.class)
class OrdenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrdenService ordenService;

    @Test
    void debeResponder404CuandoLaOrdenNoExiste() throws Exception {
        when(ordenService.buscarPorId(999L))
                .thenThrow(new RecursoNoEncontradoException(
                        "No existe la orden con ID: 999"
                ));

        mockMvc.perform(get("/api/v1/ordenes/999"))
                .andExpect(status().isNotFound());
    }



    @Test
    void debeResponder200AlConsultarUnaOrdenExistente() throws Exception {
        OrdenResponse respuesta = new OrdenResponse();
        respuesta.setId(5L);
        respuesta.setClienteId(1L);

        when(ordenService.buscarPorId(5L))
                .thenReturn(respuesta);

        mockMvc.perform(get("/api/v1/ordenes/5"))
                .andExpect(status().isOk());

        verify(ordenService).buscarPorId(5L);
    }
    @Test
    void debeResponder400CuandoLaOrdenNoTieneCliente() throws Exception {
        mockMvc.perform(post("/api/v1/ordenes")
                        .contentType("application/json")
                        .content("""
                            {
                                "detalles": [
                                    {
                                        "productoId": 1,
                                        "cantidad": 2
                                    }
                                ]
                            }
                            """))
                .andExpect(status().isBadRequest());
    }

}

