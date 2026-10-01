package org.dev.app.msproducts.exceptions;

public class NoProductsAvailableException extends RuntimeException{
    public NoProductsAvailableException(String message) {
        super(message);
    }
}
