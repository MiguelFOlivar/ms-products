package org.dev.app.msproducts.exceptions;

public class InvalidCsvException extends RuntimeException {

    public InvalidCsvException(String mensaje) {
        super(mensaje);
    }
}