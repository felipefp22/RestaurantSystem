package com.RestaurantSystem.Infra.Exceptions.ExceptionsToThrow;

public class EmailAlreadyConfirmedException extends RuntimeException {
    public EmailAlreadyConfirmedException(String message) {
        super(message);
    }
}