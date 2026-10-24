
CREATE TABLE clientes (
                          id BIGSERIAL PRIMARY KEY,
                          nombre VARCHAR(120) NOT NULL,
                          email VARCHAR(160) NOT NULL UNIQUE,
                          telefono VARCHAR(25) NOT NULL,
                          fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO'
);

CREATE TABLE productos (
                           id BIGSERIAL PRIMARY KEY,
                           nombre VARCHAR(120) NOT NULL,
                           descripcion VARCHAR(500),
                           categoria VARCHAR(80) NOT NULL,
                           precio NUMERIC(12,2) NOT NULL CHECK (precio > 0),
                           stock INTEGER NOT NULL CHECK (stock >= 0),
                           fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
                           UNIQUE (nombre, categoria)
);

CREATE TABLE ordenes (
                         id BIGSERIAL PRIMARY KEY,
                         cliente_id BIGINT NOT NULL REFERENCES clientes(id),
                         fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         total NUMERIC(14,2) NOT NULL DEFAULT 0,
                         estado VARCHAR(20) NOT NULL DEFAULT 'CREATED'
);

CREATE TABLE detalles_orden (
                                id BIGSERIAL PRIMARY KEY,
                                orden_id BIGINT NOT NULL REFERENCES ordenes(id),
                                producto_id BIGINT NOT NULL REFERENCES productos(id),
                                cantidad INTEGER NOT NULL CHECK (cantidad > 0),
                                precio_unitario NUMERIC(12,2) NOT NULL,
                                subtotal NUMERIC(14,2) NOT NULL
);
