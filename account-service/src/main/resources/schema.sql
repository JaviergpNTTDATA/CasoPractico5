-- Schema for account-service (R2DBC)
-- Loaded automatically by Spring Boot when spring.sql.init.mode=always (recommended in config-repo).

-- =========================
-- Sequences
-- =========================
-- Used by IbanGenerator
CREATE SEQUENCE IF NOT EXISTS iban_seq START WITH 1 INCREMENT BY 1;

-- (Optional) Sequences for ids if you prefer explicit sequences. For identity columns they are not required.
-- CREATE SEQUENCE IF NOT EXISTS accounts_id_seq START WITH 1 INCREMENT BY 1;
-- CREATE SEQUENCE IF NOT EXISTS movements_id_seq START WITH 1 INCREMENT BY 1;

-- =========================
-- Tables
-- =========================
CREATE TABLE IF NOT EXISTS accounts (
    id          BIGSERIAL PRIMARY KEY,
    iban        VARCHAR(34) NOT NULL UNIQUE,
    client_id   BIGINT NOT NULL,
    balance     NUMERIC(19, 2) NOT NULL DEFAULT 0,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_accounts_client_id ON accounts(client_id);
CREATE INDEX IF NOT EXISTS idx_accounts_iban ON accounts(iban);

CREATE TABLE IF NOT EXISTS movements (
    id          BIGSERIAL PRIMARY KEY,
    account_id  BIGINT NOT NULL,
    type        VARCHAR(64) NOT NULL,
    amount      NUMERIC(19, 2) NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_movements_account
        FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_movements_account_id ON movements(account_id);
CREATE INDEX IF NOT EXISTS idx_movements_created_at ON movements(created_at);

-- Notes:
-- - Movement.type is stored as String (enum name) by Spring Data JDBC/R2DBC by default.
-- - If you later need stricter constraints, you can replace type with a PostgreSQL ENUM.
