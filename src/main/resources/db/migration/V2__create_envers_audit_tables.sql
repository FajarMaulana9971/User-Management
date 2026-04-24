-- ============================================================
-- V2__create_envers_audit_tables.sql
-- Explicit Envers audit tables
-- ============================================================

-- ---- USERS_AUD ---------------------------------------------
CREATE TABLE users_aud (
                           id              BIGINT       NOT NULL,
                           rev             INTEGER      NOT NULL REFERENCES revinfo(rev),
                           revtype         SMALLINT     NOT NULL,  -- 0=ADD 1=MOD 2=DEL
                           username        VARCHAR(100),
                           email           VARCHAR(255),
                           password        VARCHAR(255),
                           full_name       VARCHAR(255),
                           profile_picture VARCHAR(500),
                           is_active       BOOLEAN,
                           role_id         BIGINT,
                           created_by      VARCHAR(255),
                           updated_by      VARCHAR(255),
                           PRIMARY KEY (id, rev)
);

-- ---- HOBBIES_AUD -------------------------------------------
CREATE TABLE hobbies_aud (
                             id          BIGINT       NOT NULL,
                             rev         INTEGER      NOT NULL REFERENCES revinfo(rev),
                             revtype     SMALLINT     NOT NULL,
                             name        VARCHAR(100),
                             description TEXT,
                             created_by  VARCHAR(255),
                             updated_by  VARCHAR(255),
                             PRIMARY KEY (id, rev)
);

-- ---- USER_HOBBIES_AUD --------------------------------------
CREATE TABLE user_hobbies_aud (
                                  user_id  BIGINT   NOT NULL,
                                  hobby_id BIGINT   NOT NULL,
                                  rev      INTEGER  NOT NULL REFERENCES revinfo(rev),
                                  revtype  SMALLINT NOT NULL,
                                  PRIMARY KEY (user_id, hobby_id, rev)
);

-- ============================================================
-- INDEXES on audit tables (for /history queries)
-- ============================================================
CREATE INDEX idx_users_aud_id  ON users_aud(id);
CREATE INDEX idx_users_aud_rev ON users_aud(rev);

CREATE INDEX idx_hobbies_aud_id  ON hobbies_aud(id);
CREATE INDEX idx_hobbies_aud_rev ON hobbies_aud(rev);