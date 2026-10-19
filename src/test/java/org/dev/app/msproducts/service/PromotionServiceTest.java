package org.dev.app.msproducts.service;

import org.dev.app.msproducts.dto.PromotionRequest;
import org.dev.app.msproducts.dto.PromotionResponse;
import org.dev.app.msproducts.exceptions.PromotionConflictException;
import org.dev.app.msproducts.exceptions.PromotionException;
import org.dev.app.msproducts.model.Product;
import org.dev.app.msproducts.model.Promotion;
import org.dev.app.msproducts.repository.ProductRepository;
import org.dev.app.msproducts.repository.PromotionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PromotionServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 6, 1, 12, 0);

    @Mock
    private PromotionRepository promotionRepository;
    @Mock
    private ProductRepository productRepository;

    private PromotionService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-06-01T12:00:00Z"), ZoneOffset.UTC);
        service = new PromotionService(promotionRepository, productRepository, clock);
    }

    @Test
    void getCurrentlyActiveUsesCurrentTimeAndReturnsOnlyRepositoryMatches() {
        Promotion current = promotion(1L, "Vigente", NOW.minusDays(1), NOW.plusDays(1), true);
        Promotion future = promotion(2L, "Futura", NOW.plusDays(1), NOW.plusDays(2), true);
        Promotion expired = promotion(3L, "Vencida", NOW.minusDays(2), NOW.minusDays(1), true);
        when(promotionRepository.findCurrentlyActive(NOW)).thenReturn(List.of(current, future, expired));

        List<PromotionResponse> result = service.getCurrentlyActive();

        assertEquals(1, result.size());
        assertEquals("Vigente", result.get(0).getName());
        assertNotEquals(future.getId(), result.get(0).getId());
        assertNotEquals(expired.getId(), result.get(0).getId());
        verify(promotionRepository).findCurrentlyActive(NOW);
    }

    @Test
    void createsPromotionWithMaximumAllowedDiscountAndRoundsFinalPriceToTwoDecimals() {
        Product product = product(7, "Producto", "19.99");
        when(productRepository.findByIdForUpdate(7)).thenReturn(Optional.of(product));
        when(promotionRepository.findOverlappingPromotions(anyList(), any(), any(), isNull()))
                .thenReturn(List.of());
        when(promotionRepository.save(any(Promotion.class))).thenAnswer(invocation -> {
            Promotion saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        PromotionResponse response = service.create(request(
                new BigDecimal("50.00"), NOW.plusDays(1), NOW.plusDays(2), true));

        assertEquals(10L, response.getId());
        assertEquals(new BigDecimal("50.00"), response.getDiscountPercentage());
        assertEquals(new BigDecimal("19.99"), response.getProducts().get(0).getOriginalPrice());
        assertEquals(new BigDecimal("10.00"), response.getProducts().get(0).getFinalPrice());
        assertEquals(2, response.getProducts().get(0).getFinalPrice().scale());
    }

    @Test
    void rejectsOverlappingPromotionForSameProduct() {
        when(productRepository.findByIdForUpdate(7)).thenReturn(Optional.of(product(7, "Producto", "100.00")));
        when(promotionRepository.findOverlappingPromotions(anyList(), any(), any(), isNull()))
                .thenReturn(List.of(promotion(1L, "Existente", NOW, NOW.plusDays(2), true)));

        assertThrows(PromotionConflictException.class,
                () -> service.create(request(new BigDecimal("20.00"), NOW.plusDays(1), NOW.plusDays(3), true)));
        verify(promotionRepository, never()).save(any(Promotion.class));
    }

    @Test
    void rejectsStartDateThatIsNotBeforeEndDate() {
        PromotionRequest request = request(new BigDecimal("20.00"), NOW.plusDays(1), NOW.plusDays(1), true);

        assertThrows(PromotionException.class, () -> service.create(request));
        verifyNoInteractions(productRepository, promotionRepository);
    }

    @Test
    void rejectsDiscountAboveMaximum() {
        PromotionRequest request = request(new BigDecimal("50.01"), NOW.plusDays(1), NOW.plusDays(2), true);

        assertThrows(PromotionException.class, () -> service.create(request));
        verifyNoInteractions(productRepository, promotionRepository);
    }

    @Test
    void updateWithActiveFalseDeactivatesPromotionWithoutCheckingOverlap() {
        Promotion existing = promotion(4L, "Existente", NOW, NOW.plusDays(2), true);
        when(promotionRepository.findLockedById(4L)).thenReturn(Optional.of(existing));
        when(productRepository.findByIdForUpdate(7)).thenReturn(Optional.of(product(7, "Producto", "100.00")));
        when(promotionRepository.save(existing)).thenReturn(existing);
        PromotionRequest request = request(new BigDecimal("20.00"), NOW, NOW.plusDays(2), false);

        PromotionResponse response = service.update(4L, request);

        assertFalse(response.getActive());
        verify(promotionRepository, never()).findOverlappingPromotions(anyList(), any(), any(), any());
        verify(promotionRepository).save(existing);
    }

    private PromotionRequest request(BigDecimal discount, LocalDateTime startsAt,
                                     LocalDateTime endsAt, boolean active) {
        PromotionRequest request = new PromotionRequest();
        request.setName("Promoción de prueba");
        request.setDiscountPercentage(discount);
        request.setStartsAt(startsAt);
        request.setEndsAt(endsAt);
        request.setProductIds(List.of(7));
        request.setActive(active);
        return request;
    }

    private Product product(Integer id, String name, String price) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setPrice(new BigDecimal(price));
        return product;
    }

    private Promotion promotion(Long id, String name, LocalDateTime startsAt,
                                LocalDateTime endsAt, boolean active) {
        Promotion promotion = new Promotion();
        promotion.setId(id);
        promotion.setName(name);
        promotion.setDiscountPercentage(new BigDecimal("10.00"));
        promotion.setStartsAt(startsAt);
        promotion.setEndsAt(endsAt);
        promotion.setActive(active);
        promotion.setProducts(Set.of(product(7, "Producto", "19.99")));
        return promotion;
    }
}
