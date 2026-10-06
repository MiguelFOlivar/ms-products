package org.dev.app.msproducts.service;

import org.dev.app.msproducts.dto.ProductRequestDTO;
import org.dev.app.msproducts.dto.ProductResponseDTO;
import org.dev.app.msproducts.exceptions.NoProductsAvailableException;
import org.dev.app.msproducts.model.Product;
import org.dev.app.msproducts.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.dev.app.msproducts.exceptions.ProductNotFoundException;

import java.util.List;

@Service
public class ProductServiceImpl implements IProductService{
    @Autowired
    private ProductRepository repository;

    @Override
    public List<ProductResponseDTO> getAllProducts() {
        List<Product> products = repository.findByActiveTrue();
        //products = List.of(); // con esto probamos la excepcion de lista vacia
        if (products.isEmpty()) {
            throw new NoProductsAvailableException("No hay productos disponibles");
        }
        return products.stream()
                .map(p -> new ProductResponseDTO(
                        p.getId(),
                        p.getName(),
                        p.getDescription(),
                        p.getPrice(),
                        p.getStock(),
                        p.getActive()
                )).peek(p -> System.out.println(p))
                .toList();
    }

    @Override
    public ProductResponseDTO saveProduct(ProductRequestDTO dto) {
        Product product = new Product();
        product.setName(dto.getName());
        product.setPrice(dto.getPrice());
        product.setDescription(dto.getDescription());
        product.setStock(dto.getStock());


        product = repository.save(product);
        return new ProductResponseDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getActive()
        );
    }


    @Override
    public ProductResponseDTO getProductById(Integer id) {
        Product p = repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("No se encontró el producto con id " + id));

        return new ProductResponseDTO(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getPrice(),
                p.getStock(),
                p.getActive()
        );
    }

    @Override
    public ProductResponseDTO updateProduct(Integer id, ProductRequestDTO dto) {
        // TO DO
        return null;
    }

    @Override
    public void deleteProduct(Integer id) {
        // TO DO

    }

}
