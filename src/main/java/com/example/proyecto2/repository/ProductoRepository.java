package com.example.proyecto2.repository;

import com.example.proyecto2.entidades.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByCategoriaIgnoreCase(String categoria);

    Page<Producto> findByNombreContainingIgnoreCase(
            String nombre,
            Pageable pageable
    );

    // Comprueba si ya existe un producto con el mismo nombre y categoría.
    boolean existsByNombreIgnoreCaseAndCategoriaIgnoreCase(
            String nombre,
            String categoria
    );

    // Busca productos con existencias iguales o inferiores a 5.
    List<Producto> findByStockLessThanEqual(Integer stock);
}
