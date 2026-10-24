package com.example.proyecto2.repository;

import com.example.proyecto2.entidades.Orden;
import com.example.proyecto2.entidades.Estados.EstadoOrden;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdenRepository extends JpaRepository<Orden, Long> {

    Page<Orden> findByClienteId(Long clienteId, Pageable pageable);

    Page<Orden> findByEstado(EstadoOrden estado, Pageable pageable);
}