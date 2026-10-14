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

/**
 * Pruebas unitarias para la capa Web (Controlador) usando MockMvc y Mockito.
 * Valida que el endpoint DELETE /api/products procese el ID recibido por cabecera HTTP (Header).
 */
@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    // MockMvc permite simular peticiones HTTP (GET, POST, DELETE) sin necesidad de levantar un servidor real
    private MockMvc mockMvc;

    // Simulamos la capa de servicio para aislar la prueba exclusivamente al controlador
    @Mock
    private IProductService service;

    // Inyectamos el mock del servicio dentro de la instancia del controlador
    @InjectMocks
    private ProductController controller;

    @BeforeEach
    void setUp() {
        // Configuramos MockMvc en modo standalone asociando el controlador y el manejador global de excepciones
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    /**
     * Prueba 1: Caso exitoso de baja lógica.
     * Simula que el servicio ejecuta la baja sin errores y verifica que el endpoint retorne HTTP 200 OK.
     */
    @Test
    @DisplayName("DELETE /api/products con header 'id' debe retornar 200 OK y mensaje cuando se retira exitosamente")
    void deleteProduct_Success() throws Exception {
        // Arrange (Preparación): Simulamos que el servicio no lanza excepciones al recibir el ID 1
        Integer productId = 1;
        doNothing().when(service).deleteProduct(productId);

        // Act & Assert (Ejecución y Verificación):
        // Realizamos la petición DELETE pasando el ID mediante .header("id", productId)
        mockMvc.perform(delete("/api/products")
                        .header("id", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Producto retirado del catálogo con éxito."));

        // Verificamos que el controlador haya invocado el método del servicio exactamente una vez
        verify(service, times(1)).deleteProduct(productId);
    }

    /**
     * Prueba 2: Caso de producto no encontrado.
     * Simula que el servicio lanza ProductNotFoundException y verifica que el GlobalExceptionHandler devuelva HTTP 404 NOT FOUND.
     */
    @Test
    @DisplayName("DELETE /api/products con header 'id' debe retornar 404 NOT FOUND cuando el producto no existe")
    void deleteProduct_NotFound() throws Exception {
        // Arrange: Simulamos que al buscar el ID 99, el servicio lanza ProductNotFoundException
        Integer productId = 99;
        doThrow(new ProductNotFoundException("No se encontró el producto con id " + productId))
                .when(service).deleteProduct(productId);

        // Act & Assert:
        mockMvc.perform(delete("/api/products")
                        .header("id", productId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No se encontró el producto con id " + productId));

        verify(service, times(1)).deleteProduct(productId);
    }

    /**
     * Prueba 3: Caso de producto previamente inactivo.
     * Simula que el servicio lanza ProductAlreadyInactiveException y verifica respuesta HTTP 409 CONFLICT.
     */
    @Test
    @DisplayName("DELETE /api/products con header 'id' debe retornar 409 CONFLICT cuando el producto ya estaba inactivo")
    void deleteProduct_AlreadyInactive() throws Exception {
        // Arrange: Simulamos que el producto ya tenía active = false
        Integer productId = 1;
        doThrow(new ProductAlreadyInactiveException("El producto ya fue retirado del catálogo"))
                .when(service).deleteProduct(productId);

        // Act & Assert:
        mockMvc.perform(delete("/api/products")
                        .header("id", productId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("El producto ya fue retirado del catálogo"));

        verify(service, times(1)).deleteProduct(productId);
    }
}
