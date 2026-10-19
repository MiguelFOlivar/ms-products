package org.dev.app.msproducts.controller;

import org.dev.app.msproducts.dto.StockAdjustmentRequest;
import org.dev.app.msproducts.dto.StockMovementResponse;
import org.dev.app.msproducts.service.StockMovementService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockMovementControllerTest {

    @Mock
    private StockMovementService stockMovementService;

    @InjectMocks
    private StockMovementController controller;

    @Test
    void delegatesStockAdjustmentAndHistoryRequests() {
        StockAdjustmentRequest request = new StockAdjustmentRequest();
        StockMovementResponse movement = new StockMovementResponse(
                1L, 7, 2, "IN", "Restock", "admin", LocalDateTime.now(), 102);
        when(stockMovementService.adjustStock(7, request)).thenReturn(movement);
        assertEquals(201, controller.adjustStock(7, request).getStatusCode().value());
        assertSame(movement, controller.adjustStock(7, request).getBody());

        when(stockMovementService.getStockMovements(7)).thenReturn(List.of(movement));
        assertEquals(List.of(movement), controller.getStockMovements(7).getBody());
    }
}
