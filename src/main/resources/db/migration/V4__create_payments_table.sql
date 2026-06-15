CREATE TABLE IF NOT EXISTS payments (
    id             BIGINT         NOT NULL AUTO_INCREMENT,
    booking_id     BIGINT         NOT NULL,
    amount         DECIMAL(10,2)  NOT NULL,
    payment_method VARCHAR(50)    NOT NULL,
    transaction_id VARCHAR(100),
    status         ENUM('PENDING','COMPLETED','FAILED','REFUNDED')
                                  NOT NULL DEFAULT 'PENDING',
    paid_at        DATETIME,
    created_at     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_payments         PRIMARY KEY (id),
    CONSTRAINT uq_payments_booking UNIQUE (booking_id),
    CONSTRAINT fk_payments_booking FOREIGN KEY (booking_id) REFERENCES bookings(id)
);
