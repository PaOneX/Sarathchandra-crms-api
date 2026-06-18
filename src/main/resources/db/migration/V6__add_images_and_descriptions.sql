ALTER TABLE cars
    ADD COLUMN description TEXT NULL AFTER license_plate;

ALTER TABLE users
    ADD COLUMN profile_picture_url VARCHAR(500) NULL AFTER phone;

CREATE TABLE car_images (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    car_id       BIGINT       NOT NULL,
    storage_path VARCHAR(500) NOT NULL,
    url          VARCHAR(500) NOT NULL,
    sort_order   INT          NOT NULL DEFAULT 0,
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_car_images PRIMARY KEY (id),
    CONSTRAINT fk_car_images_car FOREIGN KEY (car_id) REFERENCES cars (id) ON DELETE CASCADE
);

CREATE INDEX idx_car_images_car_id ON car_images (car_id);
