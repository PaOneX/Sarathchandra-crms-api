CREATE TABLE IF NOT EXISTS bookings (
    id           BIGINT         NOT NULL AUTO_INCREMENT,
    user_id      BIGINT         NOT NULL,
    car_id       BIGINT         NOT NULL,
    start_date   DATE           NOT NULL,
    end_date     DATE           NOT NULL,
    total_amount DECIMAL(10,2)  NOT NULL,
    status       ENUM('PENDING','APPROVED','REJECTED','CANCELLED','COMPLETED')
                                NOT NULL DEFAULT 'PENDING',
    created_at   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP
                                ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_bookings     PRIMARY KEY (id),
    CONSTRAINT fk_bookings_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_bookings_car  FOREIGN KEY (car_id)  REFERENCES cars(id)
);
