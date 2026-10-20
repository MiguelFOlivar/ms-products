package org.dev.app.msproducts.service;

import org.dev.app.msproducts.dto.*;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IProductService {
    ProductResponseDTO saveProduct(ProductRegisterRequest dto);
    List<ProductResponseDTO> getAllProducts();
    List<ProductResponseDTO> searchProductsByName(String name);
    List<ProductResponseDTO> getInactiveProducts();
    ProductResponseDTO getProductById(Integer id);
    ProductResponseDTO updateProduct(Integer id, ProductRequestDTO dto);
    void deleteProduct(Integer id);
    Integer getStockByProductId(Integer id);
    List<ProductResponseDTO> filterProducts(Integer categoryId, ProductRequestDTO dto);
    BulkPriceUpdateResponse bulkUpdatePrices(BulkPriceUpdateRequest request);
    Page<ProductResponseDTO> catalogProducts(ProductRequestPagDTO dto);
}
