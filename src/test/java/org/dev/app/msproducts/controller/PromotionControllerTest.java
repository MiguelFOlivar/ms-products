package org.dev.app.msproducts.controller;

import org.dev.app.msproducts.dto.PromotionRequest;
import org.dev.app.msproducts.dto.PromotionResponse;
import org.dev.app.msproducts.service.PromotionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromotionControllerTest {

    @Mock
    private PromotionService promotionService;

    @InjectMocks
    private PromotionController controller;

    @Test
    void delegatesCreateUpdateAndActivePromotionRequests() {
        LocalDateTime startsAt = LocalDateTime.of(2026, 10, 19, 10, 0);
        PromotionResponse promotion = new PromotionResponse(
                1L, "Promo", new BigDecimal("10.00"), startsAt, startsAt.plusDays(1), true, List.of());
        PromotionRequest request = new PromotionRequest();
        when(promotionService.create(request)).thenReturn(promotion);
        assertEquals(201, controller.create(request).getStatusCode().value());
        assertSame(promotion, controller.create(request).getBody());

        when(promotionService.update(1L, request)).thenReturn(promotion);
        assertSame(promotion, controller.update(1L, request).getBody());

        when(promotionService.getCurrentlyActive()).thenReturn(List.of(promotion));
        assertEquals(List.of(promotion), controller.getCurrentlyActive().getBody());
    }
}
