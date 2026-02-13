CREATE TABLE users
(
    id             SERIAL PRIMARY KEY,
    first_name     VARCHAR(100),
    last_name      VARCHAR(100),
    email          VARCHAR(255) NOT NULL UNIQUE,
    password       VARCHAR(255) NOT NULL,
    phone          VARCHAR(50),
    license_number VARCHAR(100),
    status         VARCHAR(50) DEFAULT 'ACTIVE',
    created_date  DATE,
    updated_date  DATE,
    role           INTEGER
);
