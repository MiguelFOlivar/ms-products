package org.dev.app.msproducts.controller;

import org.dev.app.msproducts.dto.ProductRequestDTO;
import org.dev.app.msproducts.dto.ProductResponseDTO;
import org.dev.app.msproducts.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    @Autowired
    private IProductService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('CLIENT','ADMIN')")
    public ResponseEntity<List<ProductResponseDTO>> getProducts() {
        return ResponseEntity.ok(service.getAllProducts());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT','ADMIN')")
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable Integer id) {
        return ResponseEntity.ok(service.getProductById(id));
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteProduct(@PathVariable Integer id) {
        service.deleteProduct(id);
        return ResponseEntity.ok(Map.of("message", "Producto retirado del catálogo con éxito."));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENT','ADMIN')")
    public ResponseEntity<ProductResponseDTO> saveProduct(@RequestBody ProductRequestDTO request){
        return ResponseEntity.ok(service.saveProduct(request));
    }

    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponseDTO> updateStock(@PathVariable Integer id,
                                                          @RequestParam Integer stock) {
        return ResponseEntity.ok(service.updateStock(id, stock));
    }
}
