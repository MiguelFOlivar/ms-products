package org.dev.app.msproducts.service;

import org.dev.app.msproducts.dto.ProductRequestDTO;
import org.dev.app.msproducts.dto.ProductResponseDTO;
import org.dev.app.msproducts.exceptions.NoProductsAvailableException;
import org.dev.app.msproducts.exceptions.ProductAlreadyInactiveException;
import org.dev.app.msproducts.exceptions.ProductNotFoundException;
import org.dev.app.msproducts.model.Product;
import org.dev.app.msproducts.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    // Simula el acceso a la base de datos para probar el servicio aisladamente.
    @Mock
    private ProductRepository repository;

    // Crea el servicio e inyecta el repositorio simulado.
    @InjectMocks
    private ProductServiceImpl service;

    @Test
    void deleteProductDeactivatesAndSavesProduct() {
        // Arrange: prepara un producto activo y simula que existe.
        Product product = createProduct(1, true, 5);
        when(repository.findById(1)).thenReturn(Optional.of(product));

        // Act: solicita la eliminación lógica.
        service.deleteProduct(1);

        // Assert: el producto queda inactivo y se guarda.
        assertFalse(product.getActive());
        verify(repository).save(product);
    }

    @Test
    void deleteProductThrowsWhenProductDoesNotExist() {
        // Arrange: simula que no existe un producto con ese ID.
        when(repository.findById(1)).thenReturn(Optional.empty());

        // Assert: se informa que no se encontró y no se intenta guardar.
        assertThrows(ProductNotFoundException.class, () -> service.deleteProduct(1));
        verify(repository, never()).save(any(Product.class));
    }

    @Test
    void deleteProductThrowsWhenProductIsAlreadyInactive() {
        // Arrange: el producto ya fue retirado del catálogo.
        Product product = createProduct(1, false, 5);
        when(repository.findById(1)).thenReturn(Optional.of(product));

        // Assert: se rechaza la operación y no se guarda de nuevo.
        assertThrows(ProductAlreadyInactiveException.class, () -> service.deleteProduct(1));
        verify(repository, never()).save(any(Product.class));
    }

    @Test
    void getAllProductsReturnsActiveProducts() {
        // Arrange: el repositorio devuelve un producto activo.
        Product product = createProduct(1, true, 5);
        when(repository.findByActiveTrue()).thenReturn(List.of(product));

        // Act: consulta el catálogo.
        List<ProductResponseDTO> result = service.getAllProducts();

        // Assert: el DTO contiene los datos esperados del producto.
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getId());
        assertEquals("Producto de prueba", result.get(0).getName());
        assertEquals(5, result.get(0).getStock());
        assertTrue(result.get(0).getActive());
    }

    @Test
    void getAllProductsThrowsWhenThereAreNoActiveProducts() {
        // Arrange: no hay productos activos en el repositorio.
        when(repository.findByActiveTrue()).thenReturn(List.of());

        // Assert: el servicio informa que el catálogo está vacío.
        assertThrows(NoProductsAvailableException.class, () -> service.getAllProducts());
    }

    @Test
    void getProductByIdReturnsProduct() {
        // Arrange: simula que el producto solicitado existe.
        Product product = createProduct(1, true, 5);
        when(repository.findById(1)).thenReturn(Optional.of(product));

        // Act: consulta el producto por su ID.
        ProductResponseDTO result = service.getProductById(1);

        // Assert: la respuesta incluye los datos del producto.
        assertEquals(1, result.getId());
        assertEquals("Producto de prueba", result.getName());
        assertEquals(new BigDecimal("19.99"), result.getPrice());
    }

    @Test
    void getProductByIdThrowsWhenProductDoesNotExist() {
        // Arrange: indica que el repositorio no encuentra el ID solicitado.
        when(repository.findById(1)).thenReturn(Optional.empty());

        // Assert: se lanza la excepción correspondiente.
        assertThrows(ProductNotFoundException.class, () -> service.getProductById(1));
    }

    @Test
    void saveProductPersistsRequestAndReturnsSavedProduct() {
        // Arrange: define los datos de entrada y simula el ID generado al guardar.
        ProductRequestDTO request = new ProductRequestDTO(
                "Nuevo producto", "Descripción", new BigDecimal("29.99"), 8);
        when(repository.save(any(Product.class))).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            product.setId(2);
            return product;
        });

        // Act: registra el producto.
        ProductResponseDTO result = service.saveProduct(request);

        // Assert: la respuesta refleja lo enviado y confirma que se guardó.
        assertEquals(2, result.getId());
        assertEquals("Nuevo producto", result.getName());
        assertEquals("Descripción", result.getDescription());
        assertEquals(new BigDecimal("29.99"), result.getPrice());
        assertEquals(8, result.getStock());
        verify(repository).save(any(Product.class));
    }

    @Test
    void updateStockPersistsAndReturnsUpdatedStock() {
        // Arrange: prepara un producto con stock inicial y simula su búsqueda.
        Product product = createProduct(1, true, 5);
        when(repository.findById(1)).thenReturn(Optional.of(product));
        when(repository.save(product)).thenReturn(product);

        // Act: actualiza el stock a un valor válido.
        ProductResponseDTO result = service.updateStock(1, 10);

        // Assert: el producto y la respuesta reflejan el nuevo stock.
        assertEquals(10, result.getStock());
        assertEquals(10, product.getStock());
        verify(repository).save(product);
    }

    @Test
    void updateStockThrowsForNegativeStock() {
        // Assert: un stock negativo se rechaza antes de consultar o guardar.
        assertThrows(IllegalArgumentException.class, () -> service.updateStock(1, -1));
        verify(repository, never()).findById(1);
        verify(repository, never()).save(any(Product.class));
    }

    @Test
    void updateStockThrowsForNullStock() {
        // Assert: un stock nulo se rechaza antes de consultar o guardar.
        assertThrows(IllegalArgumentException.class, () -> service.updateStock(1, null));
        verify(repository, never()).findById(1);
        verify(repository, never()).save(any(Product.class));
    }

    @Test
    void updateStockThrowsWhenProductDoesNotExist() {
        // Arrange: el repositorio no encuentra el producto solicitado.
        when(repository.findById(1)).thenReturn(Optional.empty());

        // Assert: se informa el error y no se guarda ningún producto.
        assertThrows(ProductNotFoundException.class, () -> service.updateStock(1, 10));
        verify(repository, never()).save(any(Product.class));
    }

    private Product createProduct(Integer id, boolean active, int stock) {
        // Crea un producto base reutilizable para los distintos escenarios.
        Product product = new Product();
        product.setId(id);
        product.setName("Producto de prueba");
        product.setDescription("Descripción");
        product.setPrice(new BigDecimal("19.99"));
        product.setStock(stock);
        product.setActive(active);
        return product;
    }
}
