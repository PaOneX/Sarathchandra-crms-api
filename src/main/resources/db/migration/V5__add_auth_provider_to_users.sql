ALTER TABLE users
    MODIFY password VARCHAR(255) NULL,
    ADD COLUMN auth_provider ENUM('LOCAL','GOOGLE') NOT NULL DEFAULT 'LOCAL' AFTER password,
    ADD COLUMN google_id VARCHAR(255) NULL AFTER auth_provider,
    ADD UNIQUE INDEX uq_users_google_id (google_id);
