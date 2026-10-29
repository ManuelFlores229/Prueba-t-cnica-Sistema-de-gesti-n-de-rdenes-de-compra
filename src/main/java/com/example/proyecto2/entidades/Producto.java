package com.example.proyecto2.entidades;

import com.example.proyecto2.entidades.Estados.EstadoRegistro;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "productos",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"nombre", "categoria"}
                )
        }
)

public class Producto {


        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(nullable = false, length = 120)
        private String nombre;

        @Column(length = 500)
        private String descripcion;

        @Column(nullable = false, length = 80)
        private String categoria;

        @Column(nullable = false, precision = 12, scale = 2)
        private BigDecimal precio;

        @Column(nullable = false)
        private Integer stock;

        @Column(name = "fecha_creacion", nullable = false)
        private LocalDateTime fechaCreacion;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false, length = 20)
        private EstadoRegistro estado = EstadoRegistro.ACTIVO;

    @OneToMany
            (mappedBy = "producto", fetch = FetchType.LAZY)
        private List<DetalleOrden> detalles = new ArrayList<>();

        public Producto() {
        }

        @PrePersist
        void antesDeGuardar() {
            if (fechaCreacion == null) {
                fechaCreacion = LocalDateTime.now();
            }
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public void setDescripcion(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getCategoria() {
            return categoria;
        }

        public void setCategoria(String categoria) {
            this.categoria = categoria;
        }

        public BigDecimal getPrecio() {
            return precio;
        }

        public void setPrecio(BigDecimal precio) {
            this.precio = precio;
        }

        public Integer getStock() {
            return stock;
        }

        public void setStock(Integer stock) {
            this.stock = stock;
        }

        public LocalDateTime getFechaCreacion() {
            return fechaCreacion;
        }

        public void setFechaCreacion(LocalDateTime fechaCreacion) {
            this.fechaCreacion = fechaCreacion;
        }

        public EstadoRegistro getEstado() {
            return estado;
        }

        public void setEstado(EstadoRegistro estado) {
            this.estado = estado;
        }

        public List<DetalleOrden> getDetalles() {return detalles;}
    }
