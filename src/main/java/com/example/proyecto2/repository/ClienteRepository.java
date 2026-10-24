package com.example.proyecto2.repository;

import com.example.proyecto2.entidades.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    boolean existsByEmailIgnoreCase(String email);

    Optional<Cliente> findByEmailIgnoreCase(String email);

    Page<Cliente> findByNombreContainingIgnoreCase(
            String nombre,
            Pageable pageable
    );
}
