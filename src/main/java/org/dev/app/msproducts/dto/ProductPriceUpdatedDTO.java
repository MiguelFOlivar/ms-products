package org.dev.app.msproducts.dto;

import java.math.BigDecimal;

public class ProductPriceUpdatedDTO {
    private Integer productId;
    private String name;
    private BigDecimal oldPrice;
    private BigDecimal newPrice;

    public ProductPriceUpdatedDTO() {
    }

    public ProductPriceUpdatedDTO(Integer productId, String name, BigDecimal oldPrice, BigDecimal newPrice) {
        this.productId = productId;
        this.name = name;
        this.oldPrice = oldPrice;
        this.newPrice = newPrice;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getOldPrice() {
        return oldPrice;
    }

    public void setOldPrice(BigDecimal oldPrice) {
        this.oldPrice = oldPrice;
    }

    public BigDecimal getNewPrice() {
        return newPrice;
    }

    public void setNewPrice(BigDecimal newPrice) {
        this.newPrice = newPrice;
    }
}
