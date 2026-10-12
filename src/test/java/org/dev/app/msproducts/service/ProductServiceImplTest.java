package org.dev.app.msproducts.service;

import org.dev.app.msproducts.exceptions.ProductAlreadyInactiveException;
import org.dev.app.msproducts.exceptions.ProductNotFoundException;
import org.dev.app.msproducts.model.Product;
import org.dev.app.msproducts.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository repository;

    @InjectMocks
    private ProductServiceImpl service;

    @Test
    @DisplayName("Debe dar de baja lógica al producto cuando existe y está activo")
    void deleteProduct_Success() {
        // Arrange
        Integer productId = 1;
        Product product = new Product();
        product.setId(productId);
        product.setName("Laptop");
        product.setActive(true);

        when(repository.findById(productId)).thenReturn(Optional.of(product));
        when(repository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        service.deleteProduct(productId);

        // Assert
        assertFalse(product.getActive(), "El producto debería tener active = false");
        verify(repository, times(1)).findById(productId);
        verify(repository, times(1)).save(product);
    }

    @Test
    @DisplayName("Debe lanzar ProductNotFoundException cuando el ID no existe")
    void deleteProduct_NotFound_ThrowsException() {
        // Arrange
        Integer nonExistentId = 99;
        when(repository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class,
                () -> service.deleteProduct(nonExistentId)
        );

        assertTrue(exception.getMessage().contains("No se encontró el producto con id " + nonExistentId));
        verify(repository, times(1)).findById(nonExistentId);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar ProductAlreadyInactiveException cuando el producto ya fue retirado (active = false)")
    void deleteProduct_AlreadyInactive_ThrowsException() {
        // Arrange
        Integer productId = 1;
        Product inactiveProduct = new Product();
        inactiveProduct.setId(productId);
        inactiveProduct.setName("Laptop");
        inactiveProduct.setActive(false);

        when(repository.findById(productId)).thenReturn(Optional.of(inactiveProduct));

        // Act & Assert
        ProductAlreadyInactiveException exception = assertThrows(
                ProductAlreadyInactiveException.class,
                () -> service.deleteProduct(productId)
        );

        assertTrue(exception.getMessage().contains("El producto ya fue retirado del catálogo"));
        verify(repository, times(1)).findById(productId);
        verify(repository, never()).save(any());
    }
}
