package org.dev.app.msproducts.service;

import org.dev.app.msproducts.dto.ProductRequestDTO;
import org.dev.app.msproducts.dto.ProductResponseDTO;

import java.util.List;

public interface IProductService {
    ProductResponseDTO saveProduct(ProductRequestDTO dto);
    List<ProductResponseDTO> getAllProducts();
    ProductResponseDTO getProductById(Integer id);
    ProductResponseDTO updateProduct(Integer id, ProductRequestDTO dto);
    ProductResponseDTO updateStock(Integer id, Integer stock);
    void deleteProduct(Integer id);
}
