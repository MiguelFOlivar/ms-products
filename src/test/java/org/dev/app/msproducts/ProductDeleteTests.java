package org.dev.app.msproducts;

import org.dev.app.msproducts.controller.ProductController;
import org.dev.app.msproducts.exceptions.GlobalExceptionHandler;
import org.dev.app.msproducts.model.Product;
import org.dev.app.msproducts.repository.ProductRepository;
import org.dev.app.msproducts.security.SecurityConfig;
import org.dev.app.msproducts.service.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@EnableWebSecurity
@Import({SecurityConfig.class, ProductServiceImpl.class, GlobalExceptionHandler.class})
class ProductDeleteTests {
    @Autowired MockMvc mvc;
    @MockitoBean ProductRepository repository;
    private Product product;

    private String credentials(String user, String password) {
        return "Basic " + Base64.getEncoder().encodeToString(
                (user + ":" + password).getBytes(StandardCharsets.UTF_8));
    }

    @BeforeEach
    void prepareProduct() {
        product = new Product();
        product.setId(5);
        product.setName("Prueba DELETE");
        product.setStock(10);
        when(repository.findById(5)).thenReturn(Optional.of(product));
    }

    @Test
    void adminRetiresProductAndCatalogExcludesIt() throws Exception {
        mvc.perform(delete("/api/products/5")
                        .header("Authorization", credentials("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Producto retirado del catálogo con éxito."));
        assertFalse(product.getActive());
        assertEquals(10, product.getStock());
        verify(repository).save(product);
        verify(repository, never()).deleteById(anyInt());
        verify(repository, never()).delete(any(Product.class));

        Product activeProduct = new Product();
        activeProduct.setId(6);
        when(repository.findByActiveTrue()).thenReturn(List.of(activeProduct));
        mvc.perform(get("/api/products")
                        .header("Authorization", credentials("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(6));
        verify(repository).findByActiveTrue();
    }

    @Test
    void missingProductReturns404() throws Exception {
        mvc.perform(delete("/api/products/999")
                        .header("Authorization", credentials("admin", "admin123")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No se encontró el producto con id 999"));
        verify(repository, never()).save(any());
    }

    @Test
    void inactiveProductReturns409() throws Exception {
        product.setActive(false);
        mvc.perform(delete("/api/products/5")
                        .header("Authorization", credentials("admin", "admin123")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("El producto ya fue retirado del catálogo"));
        verify(repository, never()).save(any());
    }

    @Test
    void clientCannotDelete() throws Exception {
        mvc.perform(delete("/api/products/5")
                        .header("Authorization", credentials("client", "client123")))
                .andExpect(status().isForbidden());
        verifyNoInteractions(repository);
    }

    @Test
    void anonymousCannotDelete() throws Exception {
        mvc.perform(delete("/api/products/5"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(repository);
    }

    @Test
    void invalidIdReturns400() throws Exception {
        mvc.perform(delete("/api/products/abc")
                        .header("Authorization", credentials("admin", "admin123")))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(repository);
    }
}
