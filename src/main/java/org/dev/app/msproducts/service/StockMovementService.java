package org.dev.app.msproducts.service;

import org.dev.app.msproducts.dto.StockAdjustmentRequest;
import org.dev.app.msproducts.dto.StockMovementResponse;
import org.dev.app.msproducts.dto.StockMovementType;
import org.dev.app.msproducts.exceptions.InsufficientStockException;
import org.dev.app.msproducts.exceptions.ProductException;
import org.dev.app.msproducts.exceptions.ProductNotFoundException;
import org.dev.app.msproducts.model.Product;
import org.dev.app.msproducts.model.StockMovement;
import org.dev.app.msproducts.repository.AdminActorRepository;
import org.dev.app.msproducts.repository.ProductRepository;
import org.dev.app.msproducts.repository.StockMovementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class StockMovementService {

    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;
    private final AdminActorRepository adminActorRepository;
    private final Clock clock;

    public StockMovementService(ProductRepository productRepository,
                                StockMovementRepository stockMovementRepository,
                                AdminActorRepository adminActorRepository,
                                Clock clock) {
        this.productRepository = productRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.adminActorRepository = adminActorRepository;
        this.clock = clock;
    }

    @Transactional
    public StockMovementResponse adjustStock(Integer productId, StockAdjustmentRequest request) {
        if (request == null || request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new ProductException("La cantidad debe ser mayor que cero");
        }
        if (request.getMovementType() == null) {
            throw new ProductException("El tipo de movimiento es obligatorio");
        }
        if (request.getReason() == null || request.getReason().isBlank()
                || request.getReason().length() > 255) {
            throw new ProductException("El motivo es obligatorio y no puede superar 255 caracteres");
        }

        Product product = productRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> new ProductNotFoundException(
                        "No se encontró el producto con id " + productId));

        int newStock;
        try {
            newStock = request.getMovementType() == StockMovementType.IN
                    ? Math.addExact(product.getStock(), request.getQuantity())
                    : Math.subtractExact(product.getStock(), request.getQuantity());
        } catch (ArithmeticException ex) {
            throw new ProductException("El ajuste excede el rango de stock permitido");
        }

        if (newStock < 0) {
            throw new InsufficientStockException(
                    "Stock insuficiente para retirar " + request.getQuantity()
                            + " unidades; stock actual: " + product.getStock());
        }

        Long actorId = adminActorRepository.findDemoAdminId();
        int updatedRows = productRepository.updateStockValue(productId, newStock);
        if (updatedRows != 1) {
            throw new ProductNotFoundException("No se pudo actualizar el producto con id " + productId);
        }

        StockMovement movement = new StockMovement(
                productId,
                request.getMovementType().name(),
                request.getQuantity(),
                request.getReason().trim(),
                actorId,
                LocalDateTime.now(clock));
        StockMovement savedMovement = stockMovementRepository.save(movement);

        return toResponse(savedMovement, newStock);
    }

    @Transactional(readOnly = true)
    public List<StockMovementResponse> getStockMovements(Integer productId) {
        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException("No se encontró el producto con id " + productId);
        }
        return stockMovementRepository.findByProductIdOrderByCreatedAtDescIdDesc(productId).stream()
                .map(movement -> toResponse(movement, null))
                .toList();
    }

    private StockMovementResponse toResponse(StockMovement movement, Integer stockAfter) {
        return new StockMovementResponse(
                movement.getId(),
                movement.getProductId(),
                movement.getQuantity(),
                movement.getMovementType(),
                movement.getReason(),
                "admin",
                movement.getCreatedAt(),
                stockAfter);
    }
}
