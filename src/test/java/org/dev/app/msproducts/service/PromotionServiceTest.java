package org.dev.app.msproducts.service;

import org.dev.app.msproducts.dto.PromotionRequest;
import org.dev.app.msproducts.dto.PromotionResponse;
import org.dev.app.msproducts.exceptions.ProductNotFoundException;
import org.dev.app.msproducts.exceptions.PromotionConflictException;
import org.dev.app.msproducts.exceptions.PromotionException;
import org.dev.app.msproducts.exceptions.PromotionNotFoundException;
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
import java.util.Arrays;
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
        Promotion inactive = promotion(4L, "Inactiva", NOW.minusDays(1), NOW.plusDays(1), false);
        Promotion startsNow = promotion(5L, "Inicia ahora", NOW, NOW.plusDays(1), true);
        Promotion endsNow = promotion(6L, "Termina ahora", NOW.minusDays(1), NOW, true);
        when(promotionRepository.findCurrentlyActive(NOW))
                .thenReturn(List.of(current, future, expired, inactive, startsNow, endsNow));

        List<PromotionResponse> result = service.getCurrentlyActive();

        assertEquals(2, result.size());
        assertEquals("Vigente", result.get(0).getName());
        assertEquals("Inicia ahora", result.get(1).getName());
        assertNotEquals(future.getId(), result.get(0).getId());
        assertNotEquals(expired.getId(), result.get(0).getId());
        assertNotEquals(inactive.getId(), result.get(0).getId());
        assertNotEquals(endsNow.getId(), result.get(0).getId());
        verify(promotionRepository).findCurrentlyActive(NOW);
    }

    @Test
    void getCurrentlyActiveReturnsEmptyWhenRepositoryHasNoPromotions() {
        when(promotionRepository.findCurrentlyActive(NOW)).thenReturn(List.of());

        assertTrue(service.getCurrentlyActive().isEmpty());
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
    void rejectsMissingPromotionDates() {
        PromotionRequest missingStart = request(new BigDecimal("20.00"), null, NOW.plusDays(1), true);
        PromotionRequest missingEnd = request(new BigDecimal("20.00"), NOW, null, true);

        assertThrows(PromotionException.class, () -> service.create(missingStart));
        assertThrows(PromotionException.class, () -> service.create(missingEnd));
        verifyNoInteractions(productRepository, promotionRepository);
    }

    @Test
    void rejectsDiscountAboveMaximum() {
        PromotionRequest request = request(new BigDecimal("50.01"), NOW.plusDays(1), NOW.plusDays(2), true);

        assertThrows(PromotionException.class, () -> service.create(request));
        verifyNoInteractions(productRepository, promotionRepository);
    }

    @Test
    void rejectsNullZeroNegativeAndOverPrecisionDiscounts() {
        PromotionRequest missingDiscount = request(null, NOW, NOW.plusDays(1), true);
        PromotionRequest zeroDiscount = request(BigDecimal.ZERO, NOW, NOW.plusDays(1), true);
        PromotionRequest negativeDiscount = request(new BigDecimal("-0.01"), NOW, NOW.plusDays(1), true);
        PromotionRequest overPrecisionDiscount = request(new BigDecimal("10.001"), NOW, NOW.plusDays(1), true);

        assertThrows(PromotionException.class, () -> service.create(missingDiscount));
        assertThrows(PromotionException.class, () -> service.create(zeroDiscount));
        assertThrows(PromotionException.class, () -> service.create(negativeDiscount));
        assertThrows(PromotionException.class, () -> service.create(overPrecisionDiscount));
        verifyNoInteractions(productRepository, promotionRepository);
    }

    @Test
    void rejectsNullOrBlankPromotionName() {
        PromotionRequest missingName = request(new BigDecimal("20.00"), NOW, NOW.plusDays(1), true);
        missingName.setName(null);
        PromotionRequest blankName = request(new BigDecimal("20.00"), NOW, NOW.plusDays(1), true);
        blankName.setName("   ");

        assertThrows(PromotionException.class, () -> service.create(missingName));
        assertThrows(PromotionException.class, () -> service.create(blankName));
        verifyNoInteractions(productRepository, promotionRepository);
    }

    @Test
    void rejectsMissingEmptyInvalidAndDuplicateProductIds() {
        PromotionRequest missingIds = request(new BigDecimal("20.00"), NOW, NOW.plusDays(1), true);
        missingIds.setProductIds(null);
        PromotionRequest emptyIds = request(new BigDecimal("20.00"), NOW, NOW.plusDays(1), true);
        emptyIds.setProductIds(List.of());
        PromotionRequest nullId = request(new BigDecimal("20.00"), NOW, NOW.plusDays(1), true);
        nullId.setProductIds(Arrays.asList((Integer) null));
        PromotionRequest nonPositiveId = request(new BigDecimal("20.00"), NOW, NOW.plusDays(1), true);
        nonPositiveId.setProductIds(List.of(0));
        PromotionRequest duplicateIds = request(new BigDecimal("20.00"), NOW, NOW.plusDays(1), true);
        duplicateIds.setProductIds(List.of(7, 7));

        assertThrows(PromotionException.class, () -> service.create(missingIds));
        assertThrows(PromotionException.class, () -> service.create(emptyIds));
        assertThrows(PromotionException.class, () -> service.create(nullId));
        assertThrows(PromotionException.class, () -> service.create(nonPositiveId));
        assertThrows(PromotionException.class, () -> service.create(duplicateIds));
        verifyNoInteractions(productRepository, promotionRepository);
    }

    @Test
    void createsInactivePromotionWithoutCheckingForOverlapsWhenActiveIsNullOrFalse() {
        Product product = product(7, "Producto", "100.00");
        when(productRepository.findByIdForUpdate(7)).thenReturn(Optional.of(product));
        when(promotionRepository.save(any(Promotion.class))).thenAnswer(invocation -> invocation.getArgument(0));
        PromotionRequest inactiveRequest = request(new BigDecimal("20.00"), NOW, NOW.plusDays(1), false);

        PromotionResponse response = service.create(inactiveRequest);

        assertFalse(response.getActive());
        verify(promotionRepository, never()).findOverlappingPromotions(anyList(), any(), any(), any());
        verify(promotionRepository).save(any(Promotion.class));
    }

    @Test
    void defaultsNullActiveFlagToTrueOnCreate() {
        when(productRepository.findByIdForUpdate(7)).thenReturn(Optional.of(product(7, "Producto", "100.00")));
        when(promotionRepository.findOverlappingPromotions(anyList(), any(), any(), isNull())).thenReturn(List.of());
        when(promotionRepository.save(any(Promotion.class))).thenAnswer(invocation -> invocation.getArgument(0));
        PromotionRequest request = request(new BigDecimal("20.00"), NOW, NOW.plusDays(1), true);
        request.setActive(null);

        assertTrue(service.create(request).getActive());
        verify(promotionRepository).findOverlappingPromotions(List.of(7), NOW, NOW.plusDays(1), null);
    }

    @Test
    void rejectsMissingProductsAndRequestsLocksInSortedOrder() {
        PromotionRequest request = request(new BigDecimal("20.00"), NOW, NOW.plusDays(1), false);
        request.setProductIds(List.of(9, 3, 7));
        when(productRepository.findByIdForUpdate(3)).thenReturn(Optional.of(product(3, "Tres", "30.00")));
        when(productRepository.findByIdForUpdate(7)).thenReturn(Optional.empty());
        when(productRepository.findByIdForUpdate(9)).thenReturn(Optional.empty());

        ProductNotFoundException exception = assertThrows(ProductNotFoundException.class,
                () -> service.create(request));

        assertTrue(exception.getMessage().contains("[7, 9]"));
        var inOrder = inOrder(productRepository);
        inOrder.verify(productRepository).findByIdForUpdate(3);
        inOrder.verify(productRepository).findByIdForUpdate(7);
        inOrder.verify(productRepository).findByIdForUpdate(9);
        verifyNoInteractions(promotionRepository);
    }

    @Test
    void sortsProductsInPromotionResponseById() {
        Product firstProduct = product(3, "Primero", "20.00");
        Product secondProduct = product(9, "Segundo", "40.00");
        when(productRepository.findByIdForUpdate(3)).thenReturn(Optional.of(firstProduct));
        when(productRepository.findByIdForUpdate(9)).thenReturn(Optional.of(secondProduct));
        when(promotionRepository.findOverlappingPromotions(anyList(), any(), any(), isNull())).thenReturn(List.of());
        when(promotionRepository.save(any(Promotion.class))).thenAnswer(invocation -> invocation.getArgument(0));
        PromotionRequest request = request(new BigDecimal("10.00"), NOW, NOW.plusDays(1), true);
        request.setProductIds(List.of(9, 3));

        PromotionResponse response = service.create(request);

        assertEquals(List.of(3, 9), response.getProducts().stream()
                .map(product -> product.getProductId()).toList());
        verify(promotionRepository).findOverlappingPromotions(List.of(3, 9), NOW, NOW.plusDays(1), null);
    }

    @Test
    void rejectsUpdateWhenPromotionDoesNotExist() {
        when(promotionRepository.findLockedById(99L)).thenReturn(Optional.empty());

        assertThrows(PromotionNotFoundException.class,
                () -> service.update(99L, request(new BigDecimal("20.00"), NOW, NOW.plusDays(1), true)));
        verifyNoInteractions(productRepository);
        verify(promotionRepository, never()).save(any(Promotion.class));
    }

    @Test
    void preservesExistingActiveFlagWhenUpdateRequestOmitsIt() {
        Promotion existing = promotion(4L, "Existente", NOW, NOW.plusDays(2), false);
        when(promotionRepository.findLockedById(4L)).thenReturn(Optional.of(existing));
        when(productRepository.findByIdForUpdate(7)).thenReturn(Optional.of(product(7, "Producto", "100.00")));
        when(promotionRepository.save(existing)).thenReturn(existing);
        PromotionRequest request = request(new BigDecimal("20.00"), NOW, NOW.plusDays(1), true);
        request.setName("  Nombre actualizado  ");
        request.setActive(null);

        PromotionResponse response = service.update(4L, request);

        assertFalse(response.getActive());
        assertEquals("Nombre actualizado", response.getName());
        verify(promotionRepository, never()).findOverlappingPromotions(anyList(), any(), any(), any());
        verify(promotionRepository).save(existing);
    }

    @Test
    void excludesUpdatedPromotionWhenCheckingOverlaps() {
        Promotion existing = promotion(4L, "Existente", NOW, NOW.plusDays(2), true);
        when(promotionRepository.findLockedById(4L)).thenReturn(Optional.of(existing));
        when(productRepository.findByIdForUpdate(7)).thenReturn(Optional.of(product(7, "Producto", "100.00")));
        when(promotionRepository.findOverlappingPromotions(anyList(), any(), any(), eq(4L))).thenReturn(List.of());
        when(promotionRepository.save(existing)).thenReturn(existing);

        assertTrue(service.update(4L, request(new BigDecimal("20.00"), NOW, NOW.plusDays(1), true)).getActive());

        verify(promotionRepository).findOverlappingPromotions(List.of(7), NOW, NOW.plusDays(1), 4L);
    }

    @Test
    void rejectsPromotionWithNegativeFinalProductPrice() {
        when(productRepository.findByIdForUpdate(7)).thenReturn(Optional.of(product(7, "Producto", "-100.00")));
        when(promotionRepository.findOverlappingPromotions(anyList(), any(), any(), isNull())).thenReturn(List.of());
        when(promotionRepository.save(any(Promotion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThrows(PromotionException.class,
                () -> service.create(request(new BigDecimal("20.00"), NOW, NOW.plusDays(1), true)));
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
