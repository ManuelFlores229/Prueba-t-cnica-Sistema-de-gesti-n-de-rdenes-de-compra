
package com.example.proyecto2.service;

import com.example.proyecto2.entidades.Cliente;
import com.example.proyecto2.entidades.DetalleOrden;
import com.example.proyecto2.entidades.Estados.EstadoOrden;
import com.example.proyecto2.entidades.Orden;
import com.example.proyecto2.entidades.Producto;
import com.example.proyecto2.exception.ConflictoNegocioException;
import com.example.proyecto2.repository.ClienteRepository;
import com.example.proyecto2.repository.OrdenRepository;
import com.example.proyecto2.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class OrdenServiceIntegrationTest {

    @Autowired
    private OrdenService ordenService;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private OrdenRepository ordenRepository;

    @Test
    void debeRevertirTodosLosCambiosSiFallaLaConfirmacion() {

        // 1. Crear un cliente exclusivo para esta prueba.
        Cliente cliente = new Cliente();
        cliente.setNombre("Cliente prueba rollback");
        cliente.setEmail("rollback_" + System.nanoTime() + "@test.com");
        cliente.setTelefono("5551234567");

        cliente = clienteRepository.saveAndFlush(cliente);

        // 2. Crear el primer producto: tiene stock suficiente.
        Producto laptop = new Producto();
        String identificadorPrueba = String.valueOf(System.nanoTime());
        laptop.setNombre("Laptop rollback " + identificadorPrueba);
        laptop.setCategoria("Prueba rollback");
        laptop.setPrecio(new BigDecimal("12500.00"));
        laptop.setStock(10);

        laptop = productoRepository.saveAndFlush(laptop);

        // 3. Crear el segundo producto: no tiene stock suficiente.
        Producto tablet = new Producto();
        tablet.setNombre("Tablet rollback " + identificadorPrueba);
        tablet.setCategoria("Prueba rollback");
        tablet.setPrecio(new BigDecimal("2500.00"));
        tablet.setStock(1);

        tablet = productoRepository.saveAndFlush(tablet);

        // 4. Crear una orden con ambos productos.
        Orden orden = new Orden();
        orden.setCliente(cliente);
        orden.setEstado(EstadoOrden.CREATED);
        orden.setTotal(new BigDecimal("42500.00"));

        DetalleOrden detalleLaptop = new DetalleOrden();
        detalleLaptop.setProducto(laptop);
        detalleLaptop.setCantidad(3);
        detalleLaptop.setPrecioUnitario(laptop.getPrecio());
        detalleLaptop.calcularSubtotal();
        orden.agregarDetalle(detalleLaptop);

        DetalleOrden detalleTablet = new DetalleOrden();
        detalleTablet.setProducto(tablet);
        detalleTablet.setCantidad(5);
        detalleTablet.setPrecioUnitario(tablet.getPrecio());
        detalleTablet.calcularSubtotal();
        orden.agregarDetalle(detalleTablet);

        orden = ordenRepository.saveAndFlush(orden);

        Long ordenId = orden.getId();
        Long laptopId = laptop.getId();
        Long tabletId = tablet.getId();

        // 5. Confirmar debe fallar por falta de stock de la tablet.
        assertThrows(
                ConflictoNegocioException.class,
                () -> ordenService.confirmar(ordenId)
        );

        // 6. Volver a consultar los datos guardados en PostgreSQL.
        Producto laptopDespues =
                productoRepository.findById(laptopId).orElseThrow();

        Producto tabletDespues =
                productoRepository.findById(tabletId).orElseThrow();

        Orden ordenDespues =
                ordenRepository.findById(ordenId).orElseThrow();

        // 7. Verificar que ningún cambio parcial quedó guardado.
        assertEquals(10, laptopDespues.getStock());
        assertEquals(1, tabletDespues.getStock());
        assertEquals(EstadoOrden.CREATED, ordenDespues.getEstado());
    }
}
