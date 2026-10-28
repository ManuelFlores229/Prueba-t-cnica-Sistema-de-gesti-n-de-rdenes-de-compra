
package com.example.proyecto2.Controller;

import com.example.proyecto2.dto.cliente.ClienteRequest;
import com.example.proyecto2.dto.cliente.ClienteResponse;
import com.example.proyecto2.service.ClienteService;
import com.example.proyecto2.service.OrdenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(ClienteController.class)
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClienteService clienteService;

    @MockitoBean
    private OrdenService ordenService;

    @Test
    void debeRegistrarClienteValido() throws Exception {
        ClienteResponse respuesta = new ClienteResponse();
        respuesta.setId(1L);
        respuesta.setNombre("Manuel Flores");
        respuesta.setEmail("manuel@example.com");
        respuesta.setTelefono("2451001046");

        when(clienteService.crear(any(ClienteRequest.class)))
                .thenReturn(respuesta);

        mockMvc.perform(post("/api/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Manuel Flores",
                                  "email": "manuel@example.com",
                                  "telefono": "2451001046"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Manuel Flores"))
                .andExpect(jsonPath("$.email").value("manuel@example.com"));
    }

    @Test
    void debeRechazarCorreoInvalido() throws Exception {
        mockMvc.perform(post("/api/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Manuel Flores",
                                  "email": "correo-invalido",
                                  "telefono": "2451001046"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}

