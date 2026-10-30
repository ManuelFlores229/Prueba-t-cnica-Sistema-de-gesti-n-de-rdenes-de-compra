package com.example.proyecto2.integracion;

import com.example.proyecto2.entidades.Cliente;
import com.example.proyecto2.entidades.Producto;
import com.example.proyecto2.repository.ClienteRepository;
import com.example.proyecto2.repository.ProductoRepository;
import com.jayway.jsonpath.JsonPath;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiIntegracionTest {

    private static final MediaType JSON = MediaType.APPLICATION_JSON;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private Cliente crearCliente() {
        Cliente cliente = new Cliente();
        cliente.setNombre("Cliente integracion");
        cliente.setEmail("api_" + System.nanoTime() + "@test.com");
        cliente.setTelefono("5551234567");
        return clienteRepository.saveAndFlush(cliente);
    }

    private Producto crearProducto(
            String nombre, String categoria, String precio, int stock
    ) {
        Producto producto = new Producto();
        producto.setNombre(nombre + " " + System.nanoTime());
        producto.setCategoria(categoria);
        producto.setPrecio(new BigDecimal(precio));
        producto.setStock(stock);
        return productoRepository.saveAndFlush(producto);
    }

    private Long crearOrdenPorApi(Long clienteId, Long productoId, int cantidad)
            throws Exception {
        String cuerpo = """
                {
                  "clienteId": %d,
                  "detalles": [{"productoId": %d, "cantidad": %d}]
                }
                """.formatted(clienteId, productoId, cantidad);

        String respuesta = mockMvc.perform(post("/api/v1/ordenes")
                        .contentType(JSON)
                        .content(cuerpo))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return ((Number) JsonPath.read(respuesta, "$.id")).longValue();
    }

    private int stockActual(Long productoId) {
        entityManager.flush();
        entityManager.clear();
        return productoRepository.findById(productoId).orElseThrow().getStock();
    }

    @Test
    void rechazaUnCorreoDuplicadoConLaBaseReal() throws Exception {
        String cuerpo = """
                {
                  "nombre": "Ana Lopez",
                  "email": "dup_%d@test.com",
                  "telefono": "5551234567"
                }
                """.formatted(System.nanoTime());

        mockMvc.perform(post("/api/v1/clientes").contentType(JSON).content(cuerpo))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/clientes").contentType(JSON).content(cuerpo))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Operación no permitida"));
    }

    @Test
    void respondeNoEncontradoSiElProductoNoExiste() throws Exception {
        mockMvc.perform(get("/api/v1/productos/999999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void rechazaUnProductoConPrecioNegativo() throws Exception {
        mockMvc.perform(post("/api/v1/productos")
                        .contentType(JSON)
                        .content("""
                                {
                                  "nombre": "Monitor",
                                  "categoria": "Computo",
                                  "precio": -10,
                                  "stock": 5
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rechazaOrdenarPorUnCampoInexistente() throws Exception {
        mockMvc.perform(get("/api/v1/productos").param("sort", "campoInventado"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void filtraProductosPorCategoriaYExistencias() throws Exception {
        String categoria = "Cat " + System.nanoTime();
        Producto conStock = crearProducto("Con stock", categoria, "100.00", 5);
        crearProducto("Sin stock", categoria, "100.00", 0);

        mockMvc.perform(get("/api/v1/productos").param("categoria", categoria))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));

        mockMvc.perform(get("/api/v1/productos")
                        .param("categoria", categoria)
                        .param("conStock", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].nombre")
                        .value(conStock.getNombre()));
    }

    @Test
    void ocultaLosProductosDesactivadosDelCatalogo() throws Exception {
        String categoria = "Cat " + System.nanoTime();
        Producto producto = crearProducto("Descontinuado", categoria, "50.00", 3);

        mockMvc.perform(delete("/api/v1/productos/{id}", producto.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/productos").param("categoria", categoria))
                .andExpect(jsonPath("$.content.length()").value(0));

        mockMvc.perform(get("/api/v1/productos")
                        .param("categoria", categoria)
                        .param("estado", "INACTIVO"))
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void calculaElTotalConVariosProductos() throws Exception {
        Cliente cliente = crearCliente();
        Producto teclado = crearProducto("Teclado", "Integracion", "150.00", 10);
        Producto mouse = crearProducto("Mouse", "Integracion", "200.00", 10);

        String cuerpo = """
                {
                  "clienteId": %d,
                  "detalles": [
                    {"productoId": %d, "cantidad": 2},
                    {"productoId": %d, "cantidad": 1}
                  ]
                }
                """.formatted(cliente.getId(), teclado.getId(), mouse.getId());

        mockMvc.perform(post("/api/v1/ordenes").contentType(JSON).content(cuerpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("CREATED"))
                .andExpect(jsonPath("$.detalles.length()").value(2))
                .andExpect(jsonPath("$.total").value(500.0));
    }

    @Test
    void confirmaYCancelaRestituyendoElInventario() throws Exception {
        Cliente cliente = crearCliente();
        Producto producto = crearProducto("Teclado", "Integracion", "150.00", 10);
        Long productoId = producto.getId();

        Long ordenId = crearOrdenPorApi(cliente.getId(), productoId, 3);

        mockMvc.perform(patch("/api/v1/ordenes/{id}/confirmar", ordenId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CONFIRMED"));
        assertEquals(7, stockActual(productoId));

        mockMvc.perform(patch("/api/v1/ordenes/{id}/cancelar", ordenId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELLED"));
        assertEquals(10, stockActual(productoId));

        mockMvc.perform(patch("/api/v1/ordenes/{id}/cancelar", ordenId))
                .andExpect(status().isConflict());
    }

    @Test
    void rechazaConfirmarSiNoHayStockSuficiente() throws Exception {
        Cliente cliente = crearCliente();
        Producto producto = crearProducto("Monitor", "Integracion", "300.00", 2);
        Long productoId = producto.getId();

        Long ordenId = crearOrdenPorApi(cliente.getId(), productoId, 5);

        mockMvc.perform(patch("/api/v1/ordenes/{id}/confirmar", ordenId))
                .andExpect(status().isConflict());
        assertEquals(2, stockActual(productoId));
    }

    @Test
    void listaLasOrdenesDeUnCliente() throws Exception {
        Cliente cliente = crearCliente();
        Producto producto = crearProducto("Cable", "Integracion", "80.00", 10);
        crearOrdenPorApi(cliente.getId(), producto.getId(), 2);

        mockMvc.perform(get("/api/v1/clientes/{id}/ordenes", cliente.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].total").value(160.0));
    }
}
