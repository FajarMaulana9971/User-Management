-- ============================================================
-- V1__create_base_tables.sql
-- Creates: roles, users, hobbies, user_hobbies
-- ============================================================

-- ---- REVINFO  --------------------
CREATE TABLE revinfo (
                         rev        SERIAL PRIMARY KEY,
                         revtstmp   BIGINT       NOT NULL,
                         changed_by VARCHAR(255)
);

-- ---- ROLES -------------------------------------------------
CREATE TABLE roles (
                       id         BIGSERIAL    PRIMARY KEY,
                       name       VARCHAR(50)  NOT NULL UNIQUE,
                       created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                       updated_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                       created_by        VARCHAR(255),
                       updated_by        VARCHAR(255)
);

-- ---- USERS -----------------------------------------
CREATE TABLE users (
                       id                BIGSERIAL    PRIMARY KEY,
                       username          VARCHAR(100) NOT NULL UNIQUE,
                       email             VARCHAR(255) NOT NULL UNIQUE,
                       password          VARCHAR(255) NOT NULL,
                       full_name         VARCHAR(255),
                       profile_picture   VARCHAR(500),
                       is_active         BOOLEAN      NOT NULL DEFAULT TRUE,
                       role_id           BIGINT       NOT NULL REFERENCES roles(id),
                       created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                       updated_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                       created_by        VARCHAR(255),
                       updated_by        VARCHAR(255)
);

-- ---- HOBBIES -----------------------------------------------
CREATE TABLE hobbies (
                         id          BIGSERIAL    PRIMARY KEY,
                         name        VARCHAR(100) NOT NULL UNIQUE,
                         description TEXT,
                         created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                         updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                         created_by  VARCHAR(255),
                         updated_by  VARCHAR(255)
);

-- ---- USER_HOBBIES (join table) -----------------------------
CREATE TABLE user_hobbies (
                              user_id  BIGINT NOT NULL REFERENCES users(id)   ON DELETE CASCADE,
                              hobby_id BIGINT NOT NULL REFERENCES hobbies(id) ON DELETE CASCADE,
                              PRIMARY KEY (user_id, hobby_id)
);

-- ============================================================
-- INDEXING
-- ============================================================

-- Users: frequent lookups by username and email
CREATE UNIQUE INDEX idx_users_username ON users(username);
CREATE UNIQUE INDEX idx_users_email    ON users(email);

-- Users: filter by active status + role (compound for RBAC queries)
CREATE INDEX idx_users_active_role ON users(is_active, role_id)
    WHERE is_active = TRUE;

-- Hobbies: name search
CREATE INDEX idx_hobbies_name ON hobbies(name);

-- User hobbies: lookup hobbies by user
CREATE INDEX idx_user_hobbies_user_id  ON user_hobbies(user_id);
CREATE INDEX idx_user_hobbies_hobby_id ON user_hobbies(hobby_id);

-- ============================================================
-- SEED: Default roles
-- ============================================================
INSERT INTO roles (name) VALUES ('ADMIN'), ('USER');