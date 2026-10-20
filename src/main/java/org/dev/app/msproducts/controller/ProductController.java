package org.dev.app.msproducts.controller;

import org.dev.app.msproducts.dto.*;
import org.dev.app.msproducts.exceptions.ProductException;
import org.dev.app.msproducts.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    @Autowired
    private IProductService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('CLIENT','ADMIN')")
    public ResponseEntity<Page<ProductResponseDTO>> getProducts(@RequestBody(required = false) ProductRequestPagDTO request){
        return ResponseEntity.ok(service.catalogProducts(request));
    }

    @GetMapping("/inactive")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProductResponseDTO>> getInactiveProducts() {
        return ResponseEntity.ok(service.getInactiveProducts());
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT','ADMIN')")
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable Integer id) {
        return ResponseEntity.ok(service.getProductById(id));
    }
    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteProduct(@RequestHeader("id") Integer id) {
        service.deleteProduct(id);
        return ResponseEntity.ok(Map.of("message", "Producto retirado del catálogo con éxito."));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENT','ADMIN')")
    public ResponseEntity<ProductResponseDTO> saveProduct(@Valid @RequestBody ProductRegisterRequest request){
        return ResponseEntity.ok(service.saveProduct(request));
    }
    @GetMapping("/{id}/stock")
    @PreAuthorize("hasAnyRole('CLIENT','ADMIN')")
    public ResponseEntity<Map<String, Object>> getProductStock(@PathVariable Integer id) {
        Integer stock = service.getStockByProductId(id);
        return ResponseEntity.ok(Map.of(
                "productId", id,
                "stock", stock
        ));
    }
    @PatchMapping("/prices/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BulkPriceUpdateResponse> bulkUpdatePrices(
            @Valid @RequestBody BulkPriceUpdateRequest request) {
        return ResponseEntity.ok(service.bulkUpdatePrices(request));
    }

    @PutMapping("/{id}")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponseDTO> updateProduct(@PathVariable Integer id,
                                                            @Valid @RequestBody ProductRequestDTO request,
                                                            @RequestHeader(value = "rol", required = false) String rol){
        if(!"admin".equalsIgnoreCase(rol)){
            throw new ProductException("Acceso denegado: Se requieren permisos de administrador");
        }
        return ResponseEntity.ok(service.updateProduct(id, request));
    }

    @GetMapping("/filter")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProductResponseDTO>> filterProducts(@RequestBody(required = false) ProductRequestDTO request,
                                                                   @RequestHeader(value = "rol", required = false) String rol){
        if(!"admin".equalsIgnoreCase(rol)){
            throw new ProductException("Acceso denegado: Se requieren permisos de administrador");
        }
        Integer categoryId = (request != null) ? request.getCategoryId() : null;
        return ResponseEntity.ok(service.filterProducts(categoryId, request));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('CLIENT','ADMIN')")
    public ResponseEntity<List<ProductResponseDTO>> searchByName(@RequestParam String name) {
        return ResponseEntity.ok(service.searchProductsByName(name));
    }
}
