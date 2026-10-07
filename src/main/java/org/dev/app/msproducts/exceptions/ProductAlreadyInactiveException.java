package org.dev.app.msproducts.exceptions;

public class ProductAlreadyInactiveException extends RuntimeException {
    public ProductAlreadyInactiveException(String message) {
        super(message);
    }
}
