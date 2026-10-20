package org.dev.app.msproducts.service;

import org.dev.app.msproducts.dto.StockAdjustmentRequest;
import org.dev.app.msproducts.dto.StockMovementResponse;
import org.dev.app.msproducts.dto.StockMovementType;
import org.dev.app.msproducts.exceptions.InsufficientStockException;
import org.dev.app.msproducts.exceptions.ProductException;
import org.dev.app.msproducts.exceptions.ProductNotFoundException;
import org.dev.app.msproducts.model.Category;
import org.dev.app.msproducts.model.Product;
import org.dev.app.msproducts.model.StockMovement;
import org.dev.app.msproducts.repository.AdminActorRepository;
import org.dev.app.msproducts.repository.ProductRepository;
import org.dev.app.msproducts.repository.StockMovementHistoryProjection;
import org.dev.app.msproducts.repository.StockMovementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockMovementServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private StockMovementRepository stockMovementRepository;
    @Mock
    private AdminActorRepository adminActorRepository;

    private StockMovementService service;

    private Product product;

    @BeforeEach
    void setUp() {
        service = new StockMovementService(productRepository, stockMovementRepository,
                adminActorRepository,
                Clock.fixed(Instant.parse("2026-10-19T12:00:00Z"), ZoneOffset.UTC));
        Category category = new Category();
        category.setId(1);
        product = new Product();
        product.setId(12);
        product.setName("Producto de prueba");
        product.setStock(150);
        product.setCategory(category);
    }

    @Test
    void recordsStockEntryAndUsesAdminAsActor() {
        when(productRepository.findByIdForUpdate(12)).thenReturn(Optional.of(product));
        when(adminActorRepository.findActiveAdminByEmail("admin@demo.com"))
                .thenReturn(new AdminActorRepository.ActorDetails(1L, "Admin Demo Simulado"));
        when(productRepository.updateStockValue(12, 155)).thenReturn(1);
        when(stockMovementRepository.save(any(StockMovement.class))).thenAnswer(invocation -> {
            StockMovement saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 20L);
            return saved;
        });

        StockMovementResponse response = service.adjustStock(12, request(5, StockMovementType.IN), "admin@demo.com");

        assertEquals(155, response.getStockAfter());
        assertEquals("IN", response.getMovementType());
        assertEquals(1L, response.getActorId());
        assertEquals("Admin Demo Simulado", response.getActor());
        assertEquals("Reposición", response.getReason());
        ArgumentCaptor<StockMovement> movementCaptor = ArgumentCaptor.forClass(StockMovement.class);
        verify(stockMovementRepository).save(movementCaptor.capture());
        assertEquals(1L, movementCaptor.getValue().getActorId());
        assertEquals(LocalDateTime.parse("2026-10-19T12:00:00"), movementCaptor.getValue().getCreatedAt());
    }

    @Test
    void recordsValidStockExit() {
        when(productRepository.findByIdForUpdate(12)).thenReturn(Optional.of(product));
        when(adminActorRepository.findActiveAdminByEmail("admin@demo.com"))
                .thenReturn(new AdminActorRepository.ActorDetails(1L, "Admin Demo Simulado"));
        when(productRepository.updateStockValue(12, 130)).thenReturn(1);
        when(stockMovementRepository.save(any(StockMovement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StockMovementResponse response = service.adjustStock(12, request(20, StockMovementType.OUT), "admin@demo.com");

        assertEquals(130, response.getStockAfter());
        assertEquals("OUT", response.getMovementType());
    }

    @Test
    void rejectsExitThatWouldMakeStockNegativeWithoutWriting() {
        when(productRepository.findByIdForUpdate(12)).thenReturn(Optional.of(product));

        assertThrows(InsufficientStockException.class,
                () -> service.adjustStock(12, request(151, StockMovementType.OUT), "admin@demo.com"));

        verify(productRepository, never()).updateStockValue(anyInt(), anyInt());
        verifyNoInteractions(stockMovementRepository, adminActorRepository);
    }

    @Test
    void rejectsZeroQuantityWithoutWriting() {
        assertThrows(ProductException.class,
                () -> service.adjustStock(12, request(0, StockMovementType.IN), "admin@demo.com"));

        verify(productRepository, never()).updateStockValue(anyInt(), anyInt());
        verifyNoInteractions(stockMovementRepository, adminActorRepository);
    }

    @Test
    void reportsMissingProduct() {
        when(productRepository.findByIdForUpdate(404)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class,
                () -> service.adjustStock(404, request(1, StockMovementType.IN), "admin@demo.com"));

        verifyNoInteractions(adminActorRepository, stockMovementRepository);
    }

    @Test
    void movementWriteFailurePropagatesAfterStockUpdateForTransactionalRollback() {
        when(productRepository.findByIdForUpdate(12)).thenReturn(Optional.of(product));
        when(adminActorRepository.findActiveAdminByEmail("admin@demo.com"))
                .thenReturn(new AdminActorRepository.ActorDetails(1L, "Admin Demo Simulado"));
        when(productRepository.updateStockValue(12, 155)).thenReturn(1);
        when(stockMovementRepository.save(any(StockMovement.class)))
                .thenThrow(new IllegalStateException("Simulated persistence failure"));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> service.adjustStock(12, request(5, StockMovementType.IN), "admin@demo.com"));

        assertEquals("Simulated persistence failure", exception.getMessage());
        verify(productRepository).updateStockValue(12, 155);
        verify(stockMovementRepository).save(any(StockMovement.class));
    }

    @Test
    void listsMovementHistoryForExistingProduct() {
        when(productRepository.existsById(12)).thenReturn(true);
        StockMovementHistoryProjection movement = mock(StockMovementHistoryProjection.class);
        when(movement.getId()).thenReturn(20L);
        when(movement.getProductId()).thenReturn(12);
        when(movement.getQuantity()).thenReturn(5);
        when(movement.getMovementType()).thenReturn("IN");
        when(movement.getReason()).thenReturn("Reposición");
        when(movement.getActorId()).thenReturn(1L);
        when(movement.getActor()).thenReturn("Admin Demo Simulado");
        when(movement.getCreatedAt()).thenReturn(LocalDateTime.parse("2026-10-19T12:00:00"));
        when(stockMovementRepository.findHistoryByProductId(12)).thenReturn(List.of(movement));

        List<StockMovementResponse> result = service.getStockMovements(12);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getActorId());
        assertEquals("Admin Demo Simulado", result.get(0).getActor());
        assertEquals("IN", result.get(0).getMovementType());
    }

    @Test
    void rejectsMissingOrInvalidAdjustmentFieldsBeforeAccessingRepositories() {
        assertThrows(ProductException.class, () -> service.adjustStock(12, null, "admin@demo.com"));

        StockAdjustmentRequest missingQuantity = request(1, StockMovementType.IN);
        missingQuantity.setQuantity(null);
        assertThrows(ProductException.class, () -> service.adjustStock(12, missingQuantity, "admin@demo.com"));
        assertThrows(ProductException.class,
                () -> service.adjustStock(12, request(-1, StockMovementType.IN), "admin@demo.com"));

        StockAdjustmentRequest missingType = request(1, StockMovementType.IN);
        missingType.setMovementType(null);
        assertThrows(ProductException.class, () -> service.adjustStock(12, missingType, "admin@demo.com"));

        StockAdjustmentRequest missingReason = request(1, StockMovementType.IN);
        missingReason.setReason("  ");
        assertThrows(ProductException.class, () -> service.adjustStock(12, missingReason, "admin@demo.com"));
        StockAdjustmentRequest nullReason = request(1, StockMovementType.IN);
        nullReason.setReason(null);
        assertThrows(ProductException.class, () -> service.adjustStock(12, nullReason, "admin@demo.com"));
        StockAdjustmentRequest longReason = request(1, StockMovementType.IN);
        longReason.setReason("x".repeat(256));
        assertThrows(ProductException.class, () -> service.adjustStock(12, longReason, "admin@demo.com"));

        verifyNoInteractions(productRepository, stockMovementRepository, adminActorRepository);
    }

    @Test
    void rejectsStockArithmeticOverflow() {
        product.setStock(Integer.MAX_VALUE);
        when(productRepository.findByIdForUpdate(12)).thenReturn(Optional.of(product));

        assertThrows(ProductException.class,
                () -> service.adjustStock(12, request(1, StockMovementType.IN), "admin@demo.com"));

        verifyNoInteractions(stockMovementRepository, adminActorRepository);
    }

    @Test
    void reportsFailedConditionalStockUpdate() {
        when(productRepository.findByIdForUpdate(12)).thenReturn(Optional.of(product));
        when(adminActorRepository.findActiveAdminByEmail("admin@demo.com"))
                .thenReturn(new AdminActorRepository.ActorDetails(1L, "Admin Demo Simulado"));
        when(productRepository.updateStockValue(12, 155)).thenReturn(0);

        assertThrows(ProductNotFoundException.class,
                () -> service.adjustStock(12, request(5, StockMovementType.IN), "admin@demo.com"));

        verifyNoInteractions(stockMovementRepository);
    }

    @Test
    void rejectsHistoryForMissingProductAndReturnsEmptyHistoryForExistingProduct() {
        when(productRepository.existsById(404)).thenReturn(false);
        assertThrows(ProductNotFoundException.class, () -> service.getStockMovements(404));

        when(productRepository.existsById(12)).thenReturn(true);
        when(stockMovementRepository.findHistoryByProductId(12)).thenReturn(List.of());
        assertTrue(service.getStockMovements(12).isEmpty());
    }

    private StockAdjustmentRequest request(int quantity, StockMovementType type) {
        StockAdjustmentRequest request = new StockAdjustmentRequest();
        request.setQuantity(quantity);
        request.setMovementType(type);
        request.setReason("Reposición");
        return request;
    }
}
