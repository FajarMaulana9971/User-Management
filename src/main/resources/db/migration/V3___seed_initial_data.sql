-- ============================================================
-- V3__seed_initial_data.sql
-- Seed: superadmin user + hobbies
-- ============================================================

-- ---- HOBBIES -----------------------------------------------
INSERT INTO hobbies (name, description, created_by)
VALUES
    ('renang', 'Olahraga air', 'system'),
    ('futsal', 'Olahraga bola indoor', 'system'),
    ('lari', 'Olahraga kardio', 'system');

-- ---- SUPER ADMIN USER --------------------------------------

INSERT INTO users (
    username,
    email,
    password,
    full_name,
    is_active,
    role_id,
    created_by
)
VALUES (
           'superadmin',
           'superadmin@example.com',
           '$2a$10$PJoZ54q.PDXqrzJmbXwAZOXb8cL6xuoOsb8q8sTIeMbkuD1rvK8Aq', -- password: admin123
           'Super Admin',
           TRUE,
           (SELECT id FROM roles WHERE name = 'ADMIN'),
           'system'
       );