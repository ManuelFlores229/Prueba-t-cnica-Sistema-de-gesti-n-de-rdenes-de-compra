package com.example.proyecto2.repository;

import com.example.proyecto2.entidades.DetalleOrden;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetalleOrdenRepository
        extends JpaRepository<DetalleOrden, Long> {

    List<DetalleOrden> findByOrdenId(Long ordenId);

    List<DetalleOrden> findByProductoId(Long productoId);
}
