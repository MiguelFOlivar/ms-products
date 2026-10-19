package org.dev.app.msproducts.service;

import org.dev.app.msproducts.dto.ProductRegisterRequest;
import org.dev.app.msproducts.dto.ProductRequestDTO;
import org.dev.app.msproducts.dto.ProductResponseDTO;
import org.dev.app.msproducts.dto.BulkPriceUpdateRequest;
import org.dev.app.msproducts.dto.BulkPriceUpdateResponse;
import org.dev.app.msproducts.dto.PriceUpdateItem;
import org.dev.app.msproducts.dto.ProductPriceUpdatedDTO;
import org.dev.app.msproducts.model.PriceHistory;
import org.dev.app.msproducts.repository.PriceHistoryRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.dev.app.msproducts.exceptions.NoProductsAvailableException;
import org.dev.app.msproducts.exceptions.ProductAlreadyExistsException;
import org.dev.app.msproducts.exceptions.ProductException;
import org.dev.app.msproducts.model.Category;
import org.dev.app.msproducts.model.Product;
import org.dev.app.msproducts.repository.CategoryRepository;
import org.dev.app.msproducts.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.dev.app.msproducts.exceptions.ProductNotFoundException;
import org.dev.app.msproducts.exceptions.ProductAlreadyInactiveException;
import org.springframework.transaction.annotation.Transactional;


import java.awt.print.PrinterException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductServiceImpl implements IProductService{
    @Autowired
    private ProductRepository repository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private PriceHistoryRepository priceHistoryRepository;


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
                        p.getActive(),
                        p.getCategory().getId()
                )).peek(p -> System.out.println(p))
                .toList();
    }
    @Override
    public List<ProductResponseDTO> getInactiveProducts() {
        List<Product> inactives = repository.findByActiveFalse();
        if (inactives.isEmpty()) {
            throw new NoProductsAvailableException("No hay productos inactivos en el catálogo");
        }
        return inactives.stream()
                .map(p -> new ProductResponseDTO(
                        p.getId(),
                        p.getName(),
                        p.getDescription(),
                        p.getPrice(),
                        p.getStock(),
                        p.getActive(),
                        p.getCategory().getId()
                ))
                .toList();
    }

    @Override
    public ProductResponseDTO saveProduct(ProductRegisterRequest dto) {

        if (dto.getName() == null || dto.getName().isBlank()){
            throw new ProductException("El nombre del producto es obligstorio");
        }

        if (dto.getPrice() == null || dto.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ProductException("El precio debe ser mayor a cero");
        }

        validarReglaStock(dto.getStock());


        String productName = dto.getName().trim();
        if (repository.existsByNameIgnoreCase(productName)) {
            throw new ProductAlreadyExistsException(
                    "Ya existe un producto registrado con el nombre " + productName);
        }

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ProductException("La categoría no existe"));

        Product product = new Product();

        product.setName(productName);
        product.setPrice(dto.getPrice());
        product.setDescription(dto.getDescription());
        product.setStock(dto.getStock());
        product.setCategory(category);


        product = repository.save(product);
        return new ProductResponseDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getActive(),
                product.getCategory().getId()
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
                p.getActive(),
                p.getCategory().getId()
        );
    }

    @Override
    public ProductResponseDTO updateProduct(Integer id, ProductRequestDTO dto) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("No se encontró el producto con el ID: " + id));

        if (dto.getName() == null || dto.getName().isBlank()){
            throw new ProductException("El nombre del producto es obligatorio");
        }

        if (dto.getPrice() == null || dto.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ProductException("El precio debe ser mayor a cero");
        }

        validarReglaStock(dto.getStock());

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ProductException("La categoría no existe"));

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setCategory(category);
        product.setStock(dto.getStock());

        product = repository.save(product);

        return new ProductResponseDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getActive(),
                product.getCategory().getId()
        );
    }

    @Override
    public List<ProductResponseDTO> filterProducts(Integer categoryId, ProductRequestDTO dto){
        List<Product> products = repository.findByActiveTrue();

        if(categoryId != null && categoryId > 0){
            categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ProductException("La categoria no existe"));

            products = products.stream().filter(product -> product.getCategory().getId().equals(categoryId)).toList();
        }

        if(dto != null && dto.getName() != null && !dto.getName().isBlank()){
            products = products.stream().filter(product -> product.getName().toLowerCase().contains(dto.getName().toLowerCase())).toList();
        }

        if(products.isEmpty()){
            throw new NoProductsAvailableException("No se encontraron productos con los filtros");
        }

        return products.stream().map(product -> new ProductResponseDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getActive(),
                product.getCategory().getId()
        )).toList();
    }
    @Override
    @Transactional
    public void deleteProduct(Integer id) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(
                        "No se encontró el producto con id " + id));
        if (Boolean.FALSE.equals(product.getActive())) {
            throw new ProductAlreadyInactiveException("El producto ya fue retirado del catálogo");
        }
        product.setActive(false);
        repository.save(product);
    }

    @Override
    public ProductResponseDTO updateStock(Integer id, Integer stock) {
        validarReglaStock(stock);

        Product product = repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("No se encontró el producto con id " + id));

        product.setStock(stock);
        product = repository.save(product);

        return new ProductResponseDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getActive(),
                product.getCategory().getId()
        );
    }
    @Override
    public Integer getStockByProductId(Integer id) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("No se encontró el producto con id " + id));

        return product.getStock();
    }

    private void validarReglaStock(Integer stock) {
        if (stock == null || stock < 0) {
            throw new ProductException("El stock no puede ser negativo");
        }

        if (stock <= 100) {
             throw new ProductException("El stock mínimo requerido es de 100 unidades");
        }

    }


    @Override
    public List<ProductResponseDTO> searchProductsByName(String name) {
        if (name == null || name.isBlank()) {
            throw new ProductException("El nombre a buscar es obligatorio");
        }

        List<Product> products = repository.findByNameContainingIgnoreCase(name.trim());

        if (products.isEmpty()) {
            throw new ProductNotFoundException("No se encontró ningún producto con el nombre: " + name.trim());
        }

        return products.stream()
                .map(p -> new ProductResponseDTO(
                        p.getId(),
                        p.getName(),
                        p.getDescription(),
                        p.getPrice(),
                        p.getStock(),
                        p.getActive(),
                        p.getCategory().getId()
                ))
                .toList();
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BulkPriceUpdateResponse bulkUpdatePrices(BulkPriceUpdateRequest request) {
        if (request == null || request.getUpdates() == null || request.getUpdates().isEmpty()) {
            throw new ProductException("El lote de actualizaciones no puede estar vacío");
        }

        List<PriceUpdateItem> updates = request.getUpdates();
        validarTamanoLote(updates);

        Set<Integer> uniqueIds = new HashSet<>();
        List<Integer> duplicateIds = new ArrayList<>();
        for (PriceUpdateItem item : updates) {
            if (item.getProductId() != null && !uniqueIds.add(item.getProductId())) {
                duplicateIds.add(item.getProductId());
            }
        }
        if (!duplicateIds.isEmpty()) {
            throw new ProductException("No se permiten IDs duplicados dentro de la misma solicitud: " + duplicateIds);
        }

        for (PriceUpdateItem item : updates) {
            if (item.getProductId() == null) {
                throw new ProductException("El ID del producto no puede ser nulo");
            }
            if (item.getNewPrice() == null || item.getNewPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ProductException("Todos los precios deben ser mayores que cero. Precio inválido para producto ID: " + item.getProductId());
            }
        }

        List<Integer> ids = updates.stream().map(PriceUpdateItem::getProductId).toList();
        List<Product> products = repository.findAllById(ids);

        Set<Integer> foundIds = products.stream().map(Product::getId).collect(Collectors.toSet());
        List<Integer> missingIds = ids.stream().filter(id -> !foundIds.contains(id)).toList();

        if (!missingIds.isEmpty()) {
            throw new ProductNotFoundException("El lote fue rechazado porque los siguientes productos no existen: " + missingIds);
        }

        Map<Integer, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        String currentAdmin = getCurrentUsername();
        List<ProductPriceUpdatedDTO> updatedDtos = new ArrayList<>();
        List<PriceHistory> historyRecords = new ArrayList<>();

        for (PriceUpdateItem item : updates) {
            Product product = productMap.get(item.getProductId());
            BigDecimal oldPrice = product.getPrice();
            BigDecimal newPrice = item.getNewPrice();

            product.setPrice(newPrice);
            repository.save(product);

            PriceHistory history = new PriceHistory(product, oldPrice, newPrice, currentAdmin);
            historyRecords.add(history);

            updatedDtos.add(new ProductPriceUpdatedDTO(
                    product.getId(),
                    product.getName(),
                    oldPrice,
                    newPrice
            ));
        }

        priceHistoryRepository.saveAll(historyRecords);

        return new BulkPriceUpdateResponse(
                "Lote de precios actualizado con éxito.",
                updatedDtos.size(),
                updatedDtos
        );
    }

    private void validarTamanoLote(List<PriceUpdateItem> updates) {
    }

    private String getCurrentUsername() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getName() != null && !auth.getName().isBlank()) {
                return auth.getName();
            }
        } catch (Exception ignored) {
        }
        return "ADMIN";
    }
}
