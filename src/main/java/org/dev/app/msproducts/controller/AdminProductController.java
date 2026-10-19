package org.dev.app.msproducts.controller;

import jakarta.validation.Valid;
import org.dev.app.msproducts.dto.BulkPriceUpdateRequest;
import org.dev.app.msproducts.dto.BulkPriceUpdateResponse;
import org.dev.app.msproducts.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/products")
public class AdminProductController {

    @Autowired
    private IProductService service;

    @PatchMapping("/prices/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BulkPriceUpdateResponse> bulkUpdatePrices(
            @Valid @RequestBody BulkPriceUpdateRequest request) {
        return ResponseEntity.ok(service.bulkUpdatePrices(request));
    }
}
