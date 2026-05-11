-- Esquema para client-service (R2DBC ejecutará este script al arrancar si spring.sql.init.mode=always)
-- Ajusta nombres/columnas si tu BD actual difiere.

CREATE TABLE IF NOT EXISTS clients (
  id BIGSERIAL PRIMARY KEY,
  first_name VARCHAR(255) NOT NULL,
  last_name VARCHAR(255) NOT NULL,
  dni VARCHAR(50) NOT NULL UNIQUE,
  email VARCHAR(255) NOT NULL UNIQUE,
  phone VARCHAR(50) NOT NULL,
  fecha_creacion TIMESTAMP
);
