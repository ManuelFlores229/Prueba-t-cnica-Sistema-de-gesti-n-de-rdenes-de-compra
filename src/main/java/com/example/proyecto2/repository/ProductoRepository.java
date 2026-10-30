package com.example.proyecto2.repository;

import com.example.proyecto2.entidades.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductoRepository
        extends JpaRepository<Producto, Long>, JpaSpecificationExecutor<Producto> {

    boolean existsByNombreIgnoreCaseAndCategoriaIgnoreCase(
            String nombre,
            String categoria
    );
}
