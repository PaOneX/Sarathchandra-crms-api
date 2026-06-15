CREATE TABLE IF NOT EXISTS cars (
    id               BIGINT         NOT NULL AUTO_INCREMENT,
    brand            VARCHAR(100)   NOT NULL,
    model            VARCHAR(100)   NOT NULL,
    fuel_type        ENUM('PETROL','DIESEL','ELECTRIC','HYBRID') NOT NULL,
    seating_capacity INT            NOT NULL,
    daily_rate       DECIMAL(10,2)  NOT NULL,
    status           ENUM('AVAILABLE','RENTED','UNDER_MAINTENANCE')
                                    NOT NULL DEFAULT 'AVAILABLE',
    year             INT,
    license_plate    VARCHAR(20),
    created_at       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP
                                    ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_cars PRIMARY KEY (id),
    CONSTRAINT uq_cars_license_plate UNIQUE (license_plate)
);
