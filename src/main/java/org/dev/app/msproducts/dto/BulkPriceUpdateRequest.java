package org.dev.app.msproducts.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class BulkPriceUpdateRequest {

    @NotEmpty(message = "El lote de actualizaciones no puede estar vacío")
    @Valid
    private List<PriceUpdateItem> updates;

    public BulkPriceUpdateRequest() {
    }

    public BulkPriceUpdateRequest(List<PriceUpdateItem> updates) {
        this.updates = updates;
    }

    public List<PriceUpdateItem> getUpdates() {
        return updates;
    }

    public void setUpdates(List<PriceUpdateItem> updates) {
        this.updates = updates;
    }
}
