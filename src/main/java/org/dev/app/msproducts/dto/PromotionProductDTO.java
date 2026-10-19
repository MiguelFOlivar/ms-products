package org.dev.app.msproducts.dto;

import java.math.BigDecimal;

public class PromotionProductDTO {

    private final Integer productId;
    private final String name;
    private final BigDecimal originalPrice;
    private final BigDecimal finalPrice;

    public PromotionProductDTO(Integer productId, String name, BigDecimal originalPrice, BigDecimal finalPrice) {
        this.productId = productId;
        this.name = name;
        this.originalPrice = originalPrice;
        this.finalPrice = finalPrice;
    }

    public Integer getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    public BigDecimal getFinalPrice() {
        return finalPrice;
    }
}
