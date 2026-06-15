package com.icet.carrental.exception;

public class CarNotAvailableException extends RuntimeException {

    public CarNotAvailableException(Long carId) {
        super("Car with id " + carId + " is not available for the selected period");
    }
}
