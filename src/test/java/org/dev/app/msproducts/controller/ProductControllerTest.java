package org.dev.app.msproducts.controller;

import org.dev.app.msproducts.exceptions.GlobalExceptionHandler;
import org.dev.app.msproducts.exceptions.ProductAlreadyInactiveException;
import org.dev.app.msproducts.exceptions.ProductNotFoundException;
import org.dev.app.msproducts.service.IProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IProductService service;

    @InjectMocks
    private ProductController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("DELETE /api/products con header 'id' debe retornar 200 OK y mensaje cuando se retira exitosamente")
    void deleteProduct_Success() throws Exception {
        // Arrange
        Integer productId = 1;
        doNothing().when(service).deleteProduct(productId);

        // Act & Assert
        mockMvc.perform(delete("/api/products")
                        .header("id", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Producto retirado del catálogo con éxito."));

        verify(service, times(1)).deleteProduct(productId);
    }

    @Test
    @DisplayName("DELETE /api/products con header 'id' debe retornar 404 NOT FOUND cuando el producto no existe")
    void deleteProduct_NotFound() throws Exception {
        // Arrange
        Integer productId = 99;
        doThrow(new ProductNotFoundException("No se encontró el producto con id " + productId))
                .when(service).deleteProduct(productId);

        // Act & Assert
        mockMvc.perform(delete("/api/products")
                        .header("id", productId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No se encontró el producto con id " + productId));

        verify(service, times(1)).deleteProduct(productId);
    }

    @Test
    @DisplayName("DELETE /api/products con header 'id' debe retornar 409 CONFLICT cuando el producto ya estaba inactivo")
    void deleteProduct_AlreadyInactive() throws Exception {
        // Arrange
        Integer productId = 1;
        doThrow(new ProductAlreadyInactiveException("El producto ya fue retirado del catálogo"))
                .when(service).deleteProduct(productId);

        // Act & Assert
        mockMvc.perform(delete("/api/products")
                        .header("id", productId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("El producto ya fue retirado del catálogo"));

        verify(service, times(1)).deleteProduct(productId);
    }
}
