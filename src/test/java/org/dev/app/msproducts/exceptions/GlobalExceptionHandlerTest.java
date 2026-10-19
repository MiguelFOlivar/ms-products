package org.dev.app.msproducts.exceptions;

import org.dev.app.msproducts.repository.StockMovementActorNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapsDomainExceptionsToTheirExpectedStatuses() {
        assertEquals(HttpStatus.NOT_FOUND,
                handler.handleProductNotFound(new ProductNotFoundException("missing")).getStatusCode());
        assertEquals(HttpStatus.CONFLICT,
                handler.handleProductAlreadyInactive(new ProductAlreadyInactiveException("inactive")).getStatusCode());
        assertEquals(HttpStatus.CONFLICT,
                handler.handleProductAlreadyExists(new ProductAlreadyExistsException("duplicate")).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND,
                handler.handlePromotionNotFound(new PromotionNotFoundException("missing")).getStatusCode());
        assertEquals(HttpStatus.CONFLICT,
                handler.handlePromotionConflict(new PromotionConflictException("overlap")).getStatusCode());
        assertEquals(HttpStatus.CONFLICT,
                handler.handleInsufficientStock(new InsufficientStockException("low stock")).getStatusCode());
        assertEquals(HttpStatus.CONFLICT,
                handler.handleStockMovementActorNotFound(
                        new StockMovementActorNotFoundException("missing actor")).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST,
                handler.handlePromotionException(new PromotionException("invalid promotion")).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND,
                handler.handleNoProducts(new NoProductsAvailableException("empty")).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST,
                handler.handleProductException(new ProductException("invalid product")).getStatusCode());
    }

    @Test
    void includesValidationFieldMessagesInBadRequestResponse() {
        BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "name", "required"));
        bindingResult.addError(new FieldError("request", "price", "must be positive"));
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(
                (MethodParameter) null, bindingResult);

        var response = handler.handleValidation(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("required", response.getBody().get("name"));
        assertEquals("must be positive", response.getBody().get("price"));
    }

    @Test
    void returnsStableBadRequestBodyForUnreadableJson() {
        var response = handler.handleJsonError(
                new HttpMessageNotReadableException("invalid json", new IllegalArgumentException(), null));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().get("status"));
        assertEquals("Solicitud inválida", response.getBody().get("error"));
        assertEquals("Verifica que los valores sean numéricos.", response.getBody().get("message"));
    }

    @Test
    void invalidStockExceptionRetainsItsMessage() {
        InvalidStockException exception = new InvalidStockException("stock inválido");
        assertEquals("stock inválido", exception.getMessage());
    }
}
