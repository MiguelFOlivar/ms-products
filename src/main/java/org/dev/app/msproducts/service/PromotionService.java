package org.dev.app.msproducts.service;

import org.dev.app.msproducts.dto.PromotionRequest;
import org.dev.app.msproducts.dto.PromotionProductDTO;
import org.dev.app.msproducts.dto.PromotionResponse;
import org.dev.app.msproducts.exceptions.ProductNotFoundException;
import org.dev.app.msproducts.exceptions.PromotionConflictException;
import org.dev.app.msproducts.exceptions.PromotionException;
import org.dev.app.msproducts.exceptions.PromotionNotFoundException;
import org.dev.app.msproducts.model.Product;
import org.dev.app.msproducts.model.Promotion;
import org.dev.app.msproducts.repository.ProductRepository;
import org.dev.app.msproducts.repository.PromotionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class PromotionService {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private final PromotionRepository promotionRepository;
    private final ProductRepository productRepository;
    private final Clock clock;

    public PromotionService(PromotionRepository promotionRepository,
                            ProductRepository productRepository,
                            Clock clock) {
        this.promotionRepository = promotionRepository;
        this.productRepository = productRepository;
        this.clock = clock;
    }

    @Transactional
    public PromotionResponse create(PromotionRequest request) {
        validateRequest(request);
        Set<Product> products = lockProducts(request.getProductIds());
        boolean active = request.getActive() == null || request.getActive();
        ensureNoOverlap(request, products, active, null);

        Promotion promotion = new Promotion();
        updatePromotion(promotion, request, products, active);
        return toResponse(promotionRepository.save(promotion));
    }

    @Transactional
    public PromotionResponse update(Long id, PromotionRequest request) {
        validateRequest(request);
        Promotion promotion = promotionRepository.findLockedById(id)
                .orElseThrow(() -> new PromotionNotFoundException("No se encontró la promoción con id " + id));

        Set<Product> products = lockProducts(request.getProductIds());
        boolean active = request.getActive() == null ? promotion.getActive() : request.getActive();
        ensureNoOverlap(request, products, active, id);

        updatePromotion(promotion, request, products, active);
        return toResponse(promotionRepository.save(promotion));
    }

    @Transactional(readOnly = true)
    public List<PromotionResponse> getCurrentlyActive() {
        LocalDateTime now = LocalDateTime.now(clock);
        return promotionRepository.findCurrentlyActive(now).stream()
                .filter(promotion -> Boolean.TRUE.equals(promotion.getActive())
                        && !promotion.getStartsAt().isAfter(now)
                        && promotion.getEndsAt().isAfter(now))
                .map(this::toResponse)
                .toList();
    }

    private void validateRequest(PromotionRequest request) {
        if (request.getStartsAt() == null || request.getEndsAt() == null
                || !request.getStartsAt().isBefore(request.getEndsAt())) {
            throw new PromotionException("La fecha de inicio debe ser anterior a la fecha de fin");
        }
        if (request.getDiscountPercentage() == null
                || request.getDiscountPercentage().compareTo(BigDecimal.ZERO) <= 0
                || request.getDiscountPercentage().compareTo(new BigDecimal("50")) > 0
                || request.getDiscountPercentage().scale() > 2) {
            throw new PromotionException("El descuento debe ser mayor que 0, máximo 50% y admitir hasta dos decimales");
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new PromotionException("El nombre de la promoción es obligatorio");
        }
        if (request.getProductIds() == null || request.getProductIds().isEmpty()
                || request.getProductIds().stream().anyMatch(id -> id == null || id <= 0)) {
            throw new PromotionException("La promoción debe incluir productos con identificadores válidos");
        }
        if (new LinkedHashSet<>(request.getProductIds()).size() != request.getProductIds().size()) {
            throw new PromotionException("No se permiten productos duplicados en una promoción");
        }
    }

    private Set<Product> lockProducts(List<Integer> productIds) {
        List<Integer> sortedIds = productIds.stream().sorted().toList();
        List<Product> products = new ArrayList<>();
        List<Integer> missingIds = new ArrayList<>();
        for (Integer id : sortedIds) {
            productRepository.findByIdForUpdate(id).ifPresentOrElse(products::add, () -> missingIds.add(id));
        }
        if (!missingIds.isEmpty()) {
            throw new ProductNotFoundException("No se encontraron los productos con ids " + missingIds);
        }
        return new LinkedHashSet<>(products);
    }

    private void ensureNoOverlap(PromotionRequest request, Set<Product> products, boolean active, Long excludedId) {
        if (!active) {
            return;
        }
        List<Integer> productIds = products.stream().map(Product::getId).toList();
        List<Promotion> overlaps = promotionRepository.findOverlappingPromotions(
                productIds, request.getStartsAt(), request.getEndsAt(), excludedId);
        if (!overlaps.isEmpty()) {
            throw new PromotionConflictException(
                    "Ya existe una promoción activa superpuesta para uno o más productos");
        }
    }

    private void updatePromotion(Promotion promotion, PromotionRequest request,
                                 Set<Product> products, boolean active) {
        promotion.setName(request.getName().trim());
        promotion.setDiscountPercentage(request.getDiscountPercentage());
        promotion.setStartsAt(request.getStartsAt());
        promotion.setEndsAt(request.getEndsAt());
        promotion.setActive(active);
        promotion.setProducts(products);
    }

    private PromotionResponse toResponse(Promotion promotion) {
        List<PromotionProductDTO> products = promotion.getProducts().stream()
                .sorted(Comparator.comparing(Product::getId))
                .map(product -> {
                    BigDecimal finalPrice = product.getPrice()
                            .multiply(ONE_HUNDRED.subtract(promotion.getDiscountPercentage()))
                            .divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
                    if (finalPrice.signum() < 0) {
                        throw new PromotionException("El precio final de un producto no puede ser negativo");
                    }
                    return new PromotionProductDTO(
                            product.getId(), product.getName(),
                            product.getPrice().setScale(2, RoundingMode.HALF_UP), finalPrice);
                })
                .toList();
        return new PromotionResponse(
                promotion.getId(), promotion.getName(), promotion.getDiscountPercentage(),
                promotion.getStartsAt(), promotion.getEndsAt(), promotion.getActive(), products);
    }
}
