package org.dev.app.msproducts.dto;

import java.util.List;

public class BulkPriceUpdateResponse {
    private String message;
    private int totalUpdated;
    private List<ProductPriceUpdatedDTO> updatedProducts;

    public BulkPriceUpdateResponse() {
    }

    public BulkPriceUpdateResponse(String message, int totalUpdated, List<ProductPriceUpdatedDTO> updatedProducts) {
        this.message = message;
        this.totalUpdated = totalUpdated;
        this.updatedProducts = updatedProducts;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getTotalUpdated() {
        return totalUpdated;
    }

    public void setTotalUpdated(int totalUpdated) {
        this.totalUpdated = totalUpdated;
    }

    public List<ProductPriceUpdatedDTO> getUpdatedProducts() {
        return updatedProducts;
    }

    public void setUpdatedProducts(List<ProductPriceUpdatedDTO> updatedProducts) {
        this.updatedProducts = updatedProducts;
    }
}
