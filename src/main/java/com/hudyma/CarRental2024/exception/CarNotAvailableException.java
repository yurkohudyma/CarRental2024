package com.hudyma.CarRental2024.exception;

public class CarNotAvailableException extends RuntimeException{
    public CarNotAvailableException(String message) {
        super(message);
    }
}
