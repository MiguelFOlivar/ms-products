package org.dev.app.msproducts.service;

import org.dev.app.msproducts.dto.ProductRegisterRequest;
import org.dev.app.msproducts.dto.ProductRequestDTO;
import org.dev.app.msproducts.dto.ProductResponseDTO;
import org.dev.app.msproducts.exceptions.NoProductsAvailableException;
import org.dev.app.msproducts.exceptions.ProductAlreadyInactiveException;
import org.dev.app.msproducts.exceptions.ProductException;
import org.dev.app.msproducts.exceptions.ProductNotFoundException;
import org.dev.app.msproducts.model.Category;
import org.dev.app.msproducts.model.Product;
import org.dev.app.msproducts.repository.CategoryRepository;
import org.dev.app.msproducts.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository repository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductServiceImpl service;

    @Test
    void deleteProductDeactivatesAndSavesProduct() {
        Product product = createProduct(1, true, 5);
        when(repository.findById(1)).thenReturn(Optional.of(product));

        service.deleteProduct(1);

        assertFalse(product.getActive());
        verify(repository).save(product);
    }

    @Test
    void deleteProductThrowsWhenProductDoesNotExist() {
        when(repository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> service.deleteProduct(1));
        verify(repository, never()).save(any(Product.class));
    }

    @Test
    void deleteProductThrowsWhenProductIsAlreadyInactive() {
        Product product = createProduct(1, false, 5);
        when(repository.findById(1)).thenReturn(Optional.of(product));

        assertThrows(ProductAlreadyInactiveException.class, () -> service.deleteProduct(1));
        verify(repository, never()).save(any(Product.class));
    }

    @Test
    void getAllProductsReturnsActiveProducts() {
        Product product = createProduct(1, true, 5);
        when(repository.findByActiveTrue()).thenReturn(List.of(product));

        List<ProductResponseDTO> result = service.getAllProducts();

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getId());
        assertEquals("Producto de prueba", result.get(0).getName());
        assertEquals(5, result.get(0).getStock());
        assertTrue(result.get(0).getActive());
    }

    @Test
    void getAllProductsThrowsWhenThereAreNoActiveProducts() {
        when(repository.findByActiveTrue()).thenReturn(List.of());

        assertThrows(NoProductsAvailableException.class, () -> service.getAllProducts());
    }

    @Test
    void getProductByIdReturnsProduct() {
        Product product = createProduct(1, true, 5);
        when(repository.findById(1)).thenReturn(Optional.of(product));

        ProductResponseDTO result = service.getProductById(1);

        assertEquals(1, result.getId());
        assertEquals("Producto de prueba", result.getName());
        assertEquals(new BigDecimal("19.99"), result.getPrice());
    }

    @Test
    void getProductByIdThrowsWhenProductDoesNotExist() {
        when(repository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> service.getProductById(1));
    }

    @Test
    void saveProductPersistsRequestAndReturnsSavedProduct() {
        Category category = new Category();
        category.setId(1);
        category.setName("Electrónica");
        category.setActive(true);

        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));

        ProductRegisterRequest request = new ProductRegisterRequest();
        request.setName("Nuevo producto");
        request.setDescription("Descripción");
        request.setPrice(new BigDecimal("29.99"));
        request.setStock(8);
        request.setCategoryId(1);

        when(repository.save(any(Product.class))).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            product.setId(2);
            return product;
        });

        ProductResponseDTO result = service.saveProduct(request);

        assertEquals(2, result.getId());
        assertEquals("Nuevo producto", result.getName());
        assertEquals("Descripción", result.getDescription());
        assertEquals(new BigDecimal("29.99"), result.getPrice());
        assertEquals(8, result.getStock());
        assertEquals(1, result.getCategoryId());
        verify(repository).save(any(Product.class));
    }

    @Test
    void updateStockPersistsAndReturnsUpdatedStock() {
        Product product = createProduct(1, true, 5);
        when(repository.findById(1)).thenReturn(Optional.of(product));
        when(repository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponseDTO result = service.updateStock(1, 10);

        assertEquals(10, result.getStock());
        verify(repository).save(product);
    }

    @Test
    void updateStockThrowsForNegativeStock() {
        assertThrows(IllegalArgumentException.class, () -> service.updateStock(1, -1));
        verify(repository, never()).findById(1);
        verify(repository, never()).save(any(Product.class));
    }

    @Test
    void updateStockThrowsForNullStock() {
        assertThrows(IllegalArgumentException.class, () -> service.updateStock(1, null));
        verify(repository, never()).findById(1);
        verify(repository, never()).save(any(Product.class));
    }

    @Test
    void updateStockThrowsWhenProductDoesNotExist() {
        when(repository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> service.updateStock(1, 10));
        verify(repository, never()).save(any(Product.class));
    }

    private Product createProduct(Integer id, boolean active, int stock) {
        Category category = new Category();
        category.setId(1);
        category.setName("Electrónica");
        category.setActive(true);

        Product product = new Product();
        product.setId(id);
        product.setName("Producto de prueba");
        product.setDescription("Descripción");
        product.setPrice(new BigDecimal("19.99"));
        product.setStock(stock);
        product.setActive(active);
        product.setCategory(category);
        return product;
    }

    @Test
    void updateProductThrowsWhenProductDoesNotExist(){
        Integer productId = 999;
        ProductRequestDTO request = new ProductRequestDTO();

        //Simulamos que la base de datos regresa vacío
        when(repository.findById(productId)).thenReturn(Optional.empty());

        //Verificamos que al ejecutar, lanze ProductNotFoundException
        assertThrows(ProductNotFoundException.class, () -> service.updateProduct(productId, request));

        verify(repository, never()).save(any(Product.class));
    }


    @Test
    void updateProductSavesAndReturnsUpdatedProduc() {
        Integer productId = 1;
        Integer categoryId = 10;

        ProductRequestDTO request = new ProductRequestDTO();
        request.setName("Laptop Gamer Actualizada");
        request.setDescription("Nueva descripción");
        request.setPrice(new BigDecimal("25000.00"));
        request.setStock(15);
        request.setCategoryId(categoryId);

        Product productoExistente = createProduct(productId, true, 5);

        Category categoriaExistente = new Category();
        categoriaExistente.setId(categoryId);


        when(repository.findById(productId)).thenReturn(Optional.of(productoExistente));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(categoriaExistente));
        when(repository.save(any(Product.class))).thenReturn(productoExistente);

        ProductResponseDTO resultado = service.updateProduct(productId, request);
        assertNotNull(resultado);
        assertEquals("Laptop Gamer Actualizada", resultado.getName());
        verify(repository, times(1)).save(any(Product.class));
    }
}
