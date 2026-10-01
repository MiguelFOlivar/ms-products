package org.dev.app.msproducts.controller;

import org.dev.app.msproducts.dto.ProductRegisterRequest;
import org.dev.app.msproducts.dto.ProductRegisterResponse;
import org.dev.app.msproducts.dto.ProductRequestDTO;
import org.dev.app.msproducts.dto.ProductResponseDTO;
import org.dev.app.msproducts.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENT','ADMIN')")
    public ResponseEntity<ProductResponseDTO> saveProduct(@RequestBody ProductRequestDTO request){
        return ResponseEntity.ok(service.saveProduct(request));
    }

}
