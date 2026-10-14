package org.dev.app.msproducts.service;

import org.dev.app.msproducts.dto.ProductRequestDTO;
import org.dev.app.msproducts.dto.ProductResponseDTO;
import org.dev.app.msproducts.exceptions.NoProductsAvailableException;
import org.dev.app.msproducts.model.Product;
import org.dev.app.msproducts.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.dev.app.msproducts.exceptions.ProductNotFoundException;
import org.dev.app.msproducts.exceptions.ProductAlreadyInactiveException;
import org.springframework.transaction.annotation.Transactional;

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
                ))
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

    /**
     * Aplica la baja lógica a un producto cambiando su campo 'active' a false.
     * 
     * @Override: Implementa la definición del método en IProductService.
     * @Transactional: Garantiza que la operación sobre la base de datos sea atómica; si ocurre un error se hace rollback.
     * 
     * @param id Identificador único del producto a retirar.
     * @throws ProductNotFoundException Si el producto no existe en la base de datos (HTTP 404).
     * @throws ProductAlreadyInactiveException Si el producto ya se encontraba inactivo previamente (HTTP 409).
     */
    @Override
    @Transactional
    public void deleteProduct(Integer id) {
        // 1. Buscamos el producto por su ID en el repositorio; si no existe lanzamos excepción controlada
        Product product = repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(
                        "No se encontró el producto con id " + id));

        // 2. Validación de regla de negocio: si ya está dado de baja (active == false), evitamos operaciones redundantes
        if (Boolean.FALSE.equals(product.getActive())) {
            throw new ProductAlreadyInactiveException("El producto ya fue retirado del catálogo");
        }

        // 3. Baja lógica: marcamos el producto como inactivo sin eliminar el registro físico de la base de datos
        product.setActive(false);

        // 4. Persistimos el cambio de estado en la base de datos
        repository.save(product);
    }

}
