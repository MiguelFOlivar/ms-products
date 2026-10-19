package org.dev.app.msproducts.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.dev.app.msproducts.dto.BulkPriceUpdateRequest;
import org.dev.app.msproducts.dto.BulkPriceUpdateResponse;
import org.dev.app.msproducts.dto.PriceUpdateItem;
import org.dev.app.msproducts.dto.ProductPriceUpdatedDTO;
import org.dev.app.msproducts.exceptions.GlobalExceptionHandler;
import org.dev.app.msproducts.exceptions.ProductException;
import org.dev.app.msproducts.exceptions.ProductNotFoundException;
import org.dev.app.msproducts.service.IProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminProductControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IProductService service;

    @InjectMocks
    private AdminProductController controller;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("PATCH /api/admin/products/prices/bulk - Éxito con lote válido retorna 200 OK")
    void bulkUpdatePrices_Success() throws Exception {
        BulkPriceUpdateRequest request = new BulkPriceUpdateRequest(List.of(
                new PriceUpdateItem(10, new BigDecimal("899.99")),
                new PriceUpdateItem(11, new BigDecimal("1299.00"))
        ));

        BulkPriceUpdateResponse response = new BulkPriceUpdateResponse(
                "Lote de precios actualizado con éxito.",
                2,
                List.of(
                        new ProductPriceUpdatedDTO(10, "Laptop", new BigDecimal("500.00"), new BigDecimal("899.99")),
                        new ProductPriceUpdatedDTO(11, "Teclado", new BigDecimal("1000.00"), new BigDecimal("1299.00"))
                )
        );

        when(service.bulkUpdatePrices(any(BulkPriceUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(patch("/api/admin/products/prices/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Lote de precios actualizado con éxito."))
                .andExpect(jsonPath("$.totalUpdated").value(2))
                .andExpect(jsonPath("$.updatedProducts[0].productId").value(10))
                .andExpect(jsonPath("$.updatedProducts[0].newPrice").value(899.99));

        verify(service, times(1)).bulkUpdatePrices(any(BulkPriceUpdateRequest.class));
    }

    @Test
    @DisplayName("PATCH /api/admin/products/prices/bulk - Lote vacío retorna 400 Bad Request")
    void bulkUpdatePrices_EmptyBatch() throws Exception {
        BulkPriceUpdateRequest request = new BulkPriceUpdateRequest(List.of());

        mockMvc.perform(patch("/api/admin/products/prices/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /api/admin/products/prices/bulk - IDs duplicados retorna 400 Bad Request")
    void bulkUpdatePrices_DuplicateIds() throws Exception {
        BulkPriceUpdateRequest request = new BulkPriceUpdateRequest(List.of(
                new PriceUpdateItem(10, new BigDecimal("100.00")),
                new PriceUpdateItem(10, new BigDecimal("200.00"))
        ));

        when(service.bulkUpdatePrices(any(BulkPriceUpdateRequest.class)))
                .thenThrow(new ProductException("No se permiten IDs duplicados dentro de la misma solicitud: [10]"));

        mockMvc.perform(patch("/api/admin/products/prices/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("No se permiten IDs duplicados dentro de la misma solicitud: [10]"));
    }

    @Test
    @DisplayName("PATCH /api/admin/products/prices/bulk - Precio inválido (<= 0) retorna 400 Bad Request")
    void bulkUpdatePrices_InvalidPrice() throws Exception {
        BulkPriceUpdateRequest request = new BulkPriceUpdateRequest(List.of(
                new PriceUpdateItem(10, new BigDecimal("-5.00"))
        ));

        mockMvc.perform(patch("/api/admin/products/prices/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /api/admin/products/prices/bulk - Producto inexistente retorna 404 Not Found")
    void bulkUpdatePrices_ProductNotFound() throws Exception {
        BulkPriceUpdateRequest request = new BulkPriceUpdateRequest(List.of(
                new PriceUpdateItem(99, new BigDecimal("500.00"))
        ));

        when(service.bulkUpdatePrices(any(BulkPriceUpdateRequest.class)))
                .thenThrow(new ProductNotFoundException("El lote fue rechazado porque los siguientes productos no existen: [99]"));

        mockMvc.perform(patch("/api/admin/products/prices/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("El lote fue rechazado porque los siguientes productos no existen: [99]"));
    }
}
