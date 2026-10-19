package org.dev.app.msproducts.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class PromotionResponse {

    private final Long id;
    private final String name;
    private final BigDecimal discountPercentage;
    private final LocalDateTime startsAt;
    private final LocalDateTime endsAt;
    private final Boolean active;
    private final List<PromotionProductDTO> products;

    public PromotionResponse(Long id, String name, BigDecimal discountPercentage,
                             LocalDateTime startsAt, LocalDateTime endsAt,
                             Boolean active, List<PromotionProductDTO> products) {
        this.id = id;
        this.name = name;
        this.discountPercentage = discountPercentage;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.active = active;
        this.products = products;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getDiscountPercentage() {
        return discountPercentage;
    }

    public LocalDateTime getStartsAt() {
        return startsAt;
    }

    public LocalDateTime getEndsAt() {
        return endsAt;
    }

    public Boolean getActive() {
        return active;
    }

    public List<PromotionProductDTO> getProducts() {
        return products;
    }
}
