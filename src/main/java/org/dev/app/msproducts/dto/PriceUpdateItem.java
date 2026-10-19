package org.dev.app.msproducts.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class PriceUpdateItem {

    @NotNull(message = "El productId no puede ser nulo")
    private Integer productId;

    @NotNull(message = "El newPrice no puede ser nulo")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a cero")
    private BigDecimal newPrice;

    public PriceUpdateItem() {
    }

    public PriceUpdateItem(Integer productId, BigDecimal newPrice) {
        this.productId = productId;
        this.newPrice = newPrice;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public BigDecimal getNewPrice() {
        return newPrice;
    }

    public void setNewPrice(BigDecimal newPrice) {
        this.newPrice = newPrice;
    }
}
