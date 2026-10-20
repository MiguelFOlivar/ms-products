package org.dev.app.msproducts.service;

import org.dev.app.msproducts.dto.*;
import org.dev.app.msproducts.exceptions.*;
import org.dev.app.msproducts.model.Category;
import org.dev.app.msproducts.model.PriceHistory;
import org.dev.app.msproducts.model.Product;
import org.dev.app.msproducts.repository.CategoryRepository;
import org.dev.app.msproducts.repository.PriceHistoryRepository;
import org.dev.app.msproducts.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository repository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private PriceHistoryRepository priceHistoryRepository;

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
        request.setStock(150);
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
        assertEquals(150, result.getStock());
        assertEquals(1, result.getCategoryId());
        verify(repository).save(any(Product.class));
    }

    @Test
    void saveProductRejectsDuplicateNameIgnoringCase() {
        ProductRegisterRequest request = new ProductRegisterRequest();
        request.setName("  pRODUCTO REPETIDO  ");
        request.setDescription("Descripción");
        request.setPrice(new BigDecimal("29.99"));
        request.setStock(150);
        request.setCategoryId(1);
        when(repository.existsByNameIgnoreCase("pRODUCTO REPETIDO")).thenReturn(true);

        assertThrows(ProductAlreadyExistsException.class, () -> service.saveProduct(request));

        verify(repository).existsByNameIgnoreCase("pRODUCTO REPETIDO");
        verify(categoryRepository, never()).findById(1);
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

        when(repository.findById(productId)).thenReturn(Optional.empty());

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
        request.setStock(150);
        request.setCategoryId(categoryId);

        Product productoExistente = createProduct(productId, true, 150);

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

    @Test
    @DisplayName("Catálogo: Rango de precio inválido (min > max) debe lanzar InvalidPriceRangeException")
    void catalogProducts_InvalidPriceRange_ThrowsException() {
        ProductRequestPagDTO request = new ProductRequestPagDTO();
        request.setMinPrice(new BigDecimal("1000.00"));
        request.setMaxPrice(new BigDecimal("100.00"));
        assertThrows(InvalidPriceRangeException.class, () -> service.catalogProducts(request));

        verify(repository, never()).searchCatalog(any(), any(), any(), any(), any());
    }
    @Test
    @DisplayName("Catálogo: Filtros combinados deben pasarse correctamente al repositorio")
    void catalogProducts_CombinedFilters_PassesArgumentsToRepository() {
        ProductRequestPagDTO request = new ProductRequestPagDTO();
        request.setName("mouse");
        request.setCategoryId(2);
        request.setMinPrice(new BigDecimal("100.00"));
        request.setMaxPrice(new BigDecimal("500.00"));
        request.setPage(0);
        request.setSize(3);
        Product p1 = createProduct(1, true, 150);
        Product p2 = createProduct(2, true, 150);
        Page<Product> pageMock = new PageImpl<>(List.of(p1, p2));
        when(repository.searchCatalog(
                eq("mouse"),
                eq(2),
                eq(new BigDecimal("100.00")),
                eq(new BigDecimal("500.00")),
                any(Pageable.class)
        )).thenReturn(pageMock);
        Page<ProductResponseDTO> result = service.catalogProducts(request);
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals("Producto de prueba", result.getContent().get(0).getName());
        verify(repository).searchCatalog(eq("mouse"), eq(2), eq(new BigDecimal("100.00")), eq(new BigDecimal("500.00")), any(Pageable.class));
    }
    @Test
    @DisplayName("Catálogo: Página sin resultados debe retornar página vacía sin lanzar excepción")
    void catalogProducts_EmptyPage_ReturnsEmptyPageWithoutThrowing() {
        ProductRequestPagDTO request = new ProductRequestPagDTO();
        request.setName("producto_inexistente");
        when(repository.searchCatalog(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(Page.empty());
        Page<ProductResponseDTO> result = service.catalogProducts(request);
        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());
    }
    @Test
    @DisplayName("Catálogo: Limitar la página a un máximo de 5 registros")
    void catalogProducts_LimitSizeToMax5() {
        ProductRequestPagDTO request = new ProductRequestPagDTO();
        request.setSize(50);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        when(repository.searchCatalog(any(), any(), any(), any(), pageableCaptor.capture()))
                .thenReturn(Page.empty());
        service.catalogProducts(request);

        assertEquals(5, pageableCaptor.getValue().getPageSize());
    }
    @Test
    @DisplayName("Catálogo: Ordenamiento configurado en la paginación")
    void catalogProducts_VerifySorting() {
        ProductRequestPagDTO request = new ProductRequestPagDTO();
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        when(repository.searchCatalog(any(), any(), any(), any(), pageableCaptor.capture()))
                .thenReturn(Page.empty());
        service.catalogProducts(request);

        Sort sort = pageableCaptor.getValue().getSort();
        assertNotNull(sort.getOrderFor("price"));
        assertEquals(Sort.Direction.ASC, sort.getOrderFor("price").getDirection());
    }
    @Test
    @DisplayName("Catálogo: Solo debe retornar productos activos")
    void catalogProducts_OnlyActiveProductsReturned() {
        Product activeProduct = createProduct(1, true, 150);
        Page<Product> pageMock = new PageImpl<>(List.of(activeProduct));
        when(repository.searchCatalog(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(pageMock);
        Page<ProductResponseDTO> result = service.catalogProducts(new ProductRequestPagDTO());
        assertEquals(1, result.getContent().size());
        assertTrue(result.getContent().get(0).getActive());
    }

    @Test
    @DisplayName("1. Lote válido: Actualiza precios y genera historial de precios")
    void bulkUpdatePrices_ValidBatch_Success() {
        Product p1 = createProduct(10, true, 100);
        p1.setName("Producto A");
        p1.setPrice(new BigDecimal("500.00"));

        Product p2 = createProduct(11, true, 100);
        p2.setName("Producto B");
        p2.setPrice(new BigDecimal("1000.00"));

        BulkPriceUpdateRequest request = new BulkPriceUpdateRequest(List.of(
                new PriceUpdateItem(10, new BigDecimal("899.99")),
                new PriceUpdateItem(11, new BigDecimal("1299.00"))
        ));

        when(repository.findAllById(List.of(10, 11))).thenReturn(List.of(p1, p2));
        when(repository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BulkPriceUpdateResponse response = service.bulkUpdatePrices(request);

        assertNotNull(response);
        assertEquals(2, response.getTotalUpdated());
        assertEquals(new BigDecimal("899.99"), p1.getPrice());
        assertEquals(new BigDecimal("1299.00"), p2.getPrice());

        verify(repository, times(2)).save(any(Product.class));
        verify(priceHistoryRepository, times(1)).saveAll(any());
    }

    @Test
    @DisplayName("2. Lote vacío: Rechaza con ProductException y no modifica BD")
    void bulkUpdatePrices_EmptyBatch_ThrowsException() {
        BulkPriceUpdateRequest requestVacio = new BulkPriceUpdateRequest(List.of());

        ProductException ex = assertThrows(ProductException.class,
                () -> service.bulkUpdatePrices(requestVacio));

        assertTrue(ex.getMessage().contains("no puede estar vacío"));
        verify(repository, never()).findAllById(any());
        verify(repository, never()).save(any());
        verify(priceHistoryRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("3. IDs duplicados: Rechaza con ProductException antes de consultar la BD")
    void bulkUpdatePrices_DuplicateIds_ThrowsException() {
        BulkPriceUpdateRequest requestDuplicado = new BulkPriceUpdateRequest(List.of(
                new PriceUpdateItem(10, new BigDecimal("150.00")),
                new PriceUpdateItem(10, new BigDecimal("200.00"))
        ));

        ProductException ex = assertThrows(ProductException.class,
                () -> service.bulkUpdatePrices(requestDuplicado));

        assertTrue(ex.getMessage().contains("IDs duplicados"));
        verify(repository, never()).findAllById(any());
        verify(repository, never()).save(any());
        verify(priceHistoryRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("4. Precio inválido (<= 0): Rechaza el lote completo")
    void bulkUpdatePrices_InvalidPrice_ThrowsException() {
        BulkPriceUpdateRequest requestPrecioInvalido = new BulkPriceUpdateRequest(List.of(
                new PriceUpdateItem(10, new BigDecimal("150.00")),
                new PriceUpdateItem(11, new BigDecimal("-5.00"))
        ));

        ProductException ex = assertThrows(ProductException.class,
                () -> service.bulkUpdatePrices(requestPrecioInvalido));

        assertTrue(ex.getMessage().contains("mayor"));
        verify(repository, never()).findAllById(any());
        verify(repository, never()).save(any());
        verify(priceHistoryRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("5. Producto inexistente: Rechaza el lote completo sin actualizar nada")
    void bulkUpdatePrices_NonExistentProduct_ThrowsException() {
        Product p10 = createProduct(10, true, 100);

        BulkPriceUpdateRequest request = new BulkPriceUpdateRequest(List.of(
                new PriceUpdateItem(10, new BigDecimal("899.99")),
                new PriceUpdateItem(99, new BigDecimal("1299.00"))
        ));

        when(repository.findAllById(List.of(10, 99))).thenReturn(List.of(p10));

        ProductNotFoundException ex = assertThrows(ProductNotFoundException.class,
                () -> service.bulkUpdatePrices(request));

        assertTrue(ex.getMessage().contains("99"));
        verify(repository, never()).save(any());
        verify(priceHistoryRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("6. Rollback ante fallo: Si falla el guardado o historial, se propaga la excepción para rollback")
    void bulkUpdatePrices_RollbackWhenUpdateFails() {
        Product p10 = createProduct(10, true, 100);

        BulkPriceUpdateRequest request = new BulkPriceUpdateRequest(List.of(
                new PriceUpdateItem(10, new BigDecimal("899.99"))
        ));

        when(repository.findAllById(List.of(10))).thenReturn(List.of(p10));
        when(repository.save(any(Product.class))).thenReturn(p10);
        doThrow(new RuntimeException("Error simulado de base de datos en historial"))
                .when(priceHistoryRepository).saveAll(any());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.bulkUpdatePrices(request));

        assertEquals("Error simulado de base de datos en historial", ex.getMessage());
    }

    @Test
    void rejectsInvalidProductRegistrationFieldsAndMissingCategory() {
        assertInvalidRegistration(request -> request.setName(null));
        assertInvalidRegistration(request -> request.setName("   "));
        assertInvalidRegistration(request -> request.setPrice(null));
        assertInvalidRegistration(request -> request.setPrice(BigDecimal.ZERO));
        assertInvalidRegistration(request -> request.setStock(null));
        assertInvalidRegistration(request -> request.setStock(-1));
        assertInvalidRegistration(request -> request.setStock(100));

        ProductRegisterRequest request = validRegisterRequest();
        when(repository.existsByNameIgnoreCase("Nuevo producto")).thenReturn(false);
        when(categoryRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(ProductException.class, () -> service.saveProduct(request));
    }

    @Test
    void rejectsInvalidProductUpdateFieldsAndMissingCategory() {
        Product existing = createProduct(1, true, 150);
        when(repository.findById(1)).thenReturn(Optional.of(existing));
        assertInvalidUpdate(request -> request.setName(null));
        assertInvalidUpdate(request -> request.setName("  "));
        assertInvalidUpdate(request -> request.setPrice(null));
        assertInvalidUpdate(request -> request.setPrice(BigDecimal.ZERO));
        assertInvalidUpdate(request -> request.setStock(100));
        assertInvalidUpdate(request -> request.setStock(-1));

        ProductRequestDTO request = validProductUpdateRequest();
        when(categoryRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(ProductException.class, () -> service.updateProduct(1, request));
    }

    @Test
    void filtersProductsByCategoryAndCaseInsensitiveName() {
        Product matching = createProduct(1, true, 150);
        matching.setName("Wireless Mouse");
        Product other = createProduct(2, true, 150);
        other.setName("Keyboard");
        when(repository.findByActiveTrue()).thenReturn(List.of(matching, other));
        when(categoryRepository.findById(1)).thenReturn(Optional.of(matching.getCategory()));
        ProductRequestDTO filters = new ProductRequestDTO();
        filters.setName("MOUSE");

        List<ProductResponseDTO> result = service.filterProducts(1, filters);

        assertEquals(1, result.size());
        assertEquals("Wireless Mouse", result.get(0).getName());
        verify(categoryRepository).findById(1);
    }

    @Test
    void filterWithoutPositiveCategoryOrNameReturnsAvailableProducts() {
        Product product = createProduct(1, true, 150);
        when(repository.findByActiveTrue()).thenReturn(List.of(product));
        ProductRequestDTO noName = new ProductRequestDTO();

        assertEquals(1, service.filterProducts(0, noName).size());
        noName.setName("  ");
        assertEquals(1, service.filterProducts(null, noName).size());
        verifyNoInteractions(categoryRepository);
    }

    @Test
    void rejectsMissingCategoryAndEmptyFilterResults() {
        when(repository.findByActiveTrue()).thenReturn(List.of(createProduct(1, true, 150)));
        when(categoryRepository.findById(9)).thenReturn(Optional.empty());
        assertThrows(ProductException.class, () -> service.filterProducts(9, null));

        ProductRequestDTO filters = new ProductRequestDTO();
        filters.setName("does not exist");
        assertThrows(NoProductsAvailableException.class, () -> service.filterProducts(null, filters));
        when(repository.findByActiveTrue()).thenReturn(List.of());
        assertThrows(NoProductsAvailableException.class, () -> service.filterProducts(null, null));
    }

    @Test
    void returnsInactiveProductsAndRejectsAnEmptyInactiveCatalog() {
        Product inactive = createProduct(2, false, 150);
        when(repository.findByActiveFalse()).thenReturn(List.of(inactive));
        assertEquals(2, service.getInactiveProducts().get(0).getId());

        when(repository.findByActiveFalse()).thenReturn(List.of());
        assertThrows(NoProductsAvailableException.class, () -> service.getInactiveProducts());
    }

    @Test
    void rejectsBlankSearchAndReportsWhenNoNamesMatch() {
        assertThrows(ProductException.class, () -> service.searchProductsByName(null));
        assertThrows(ProductException.class, () -> service.searchProductsByName("  "));
        when(repository.findByNameContainingIgnoreCase("Missing")).thenReturn(List.of());
        assertThrows(ProductNotFoundException.class, () -> service.searchProductsByName(" Missing "));
    }

    @Test
    void searchesProductsByTrimmedName() {
        Product product = createProduct(1, true, 150);
        when(repository.findByNameContainingIgnoreCase("Mouse")).thenReturn(List.of(product));

        List<ProductResponseDTO> result = service.searchProductsByName(" Mouse ");

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getId());
        verify(repository).findByNameContainingIgnoreCase("Mouse");
    }

    @Test
    void getsStockByProductIdOrReportsMissingProduct() {
        when(repository.findById(1)).thenReturn(Optional.of(createProduct(1, true, 150)));
        assertEquals(150, service.getStockByProductId(1));
        when(repository.findById(2)).thenReturn(Optional.empty());
        assertThrows(ProductNotFoundException.class, () -> service.getStockByProductId(2));
    }

    @Test
    void catalogUsesDefaultsAndRejectsInvalidPageAndSize() {
        when(repository.searchCatalog(isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(Page.empty());
        assertTrue(service.catalogProducts(null).isEmpty());

        ProductRequestPagDTO invalidPage = new ProductRequestPagDTO();
        invalidPage.setPage(-1);
        assertThrows(ProductException.class, () -> service.catalogProducts(invalidPage));

        ProductRequestPagDTO invalidSize = new ProductRequestPagDTO();
        invalidSize.setSize(0);
        assertThrows(ProductException.class, () -> service.catalogProducts(invalidSize));

        ProductRequestPagDTO blankName = new ProductRequestPagDTO();
        blankName.setName("  ");
        blankName.setSize(2);
        service.catalogProducts(blankName);
        verify(repository, times(2)).searchCatalog(isNull(), isNull(), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    void catalogAcceptsPriceBoundsWhenOnlyOneBoundIsProvided() {
        when(repository.searchCatalog(any(), any(), any(), any(), any(Pageable.class))).thenReturn(Page.empty());
        ProductRequestPagDTO minimumOnly = new ProductRequestPagDTO();
        minimumOnly.setMinPrice(new BigDecimal("10.00"));
        service.catalogProducts(minimumOnly);
        ProductRequestPagDTO maximumOnly = new ProductRequestPagDTO();
        maximumOnly.setMaxPrice(new BigDecimal("20.00"));
        service.catalogProducts(maximumOnly);

        verify(repository).searchCatalog(isNull(), isNull(), eq(new BigDecimal("10.00")), isNull(), any(Pageable.class));
        verify(repository).searchCatalog(isNull(), isNull(), isNull(), eq(new BigDecimal("20.00")), any(Pageable.class));
    }

    @Test
    void bulkUpdateRejectsNullRequestAndMissingItemFields() {
        assertThrows(ProductException.class, () -> service.bulkUpdatePrices(null));
        assertThrows(ProductException.class,
                () -> service.bulkUpdatePrices(new BulkPriceUpdateRequest(null)));
        assertThrows(ProductException.class, () -> service.bulkUpdatePrices(
                new BulkPriceUpdateRequest(Arrays.asList(new PriceUpdateItem(null, BigDecimal.ONE)))));
        assertThrows(ProductException.class, () -> service.bulkUpdatePrices(
                new BulkPriceUpdateRequest(List.of(new PriceUpdateItem(1, null)))));
        verify(repository, never()).findAllById(any());
    }

    @Test
    void usesAuthenticatedUsernameWhenRecordingPriceHistory() {
        Product product = createProduct(10, true, 150);
        product.setPrice(new BigDecimal("10.00"));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("catalog-admin", "password"));
        try {
            when(repository.findAllById(List.of(10))).thenReturn(List.of(product));
            when(repository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(priceHistoryRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

            service.bulkUpdatePrices(new BulkPriceUpdateRequest(
                    List.of(new PriceUpdateItem(10, new BigDecimal("12.00")))));

            ArgumentCaptor<List<PriceHistory>> historyCaptor = ArgumentCaptor.forClass(List.class);
            verify(priceHistoryRepository).saveAll(historyCaptor.capture());
            assertEquals("catalog-admin", historyCaptor.getValue().get(0).getChangedBy());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void usesAdminFallbackForNullBlankOrFailingAuthenticationNames() {
        Authentication nullName = mock(Authentication.class);
        when(nullName.getName()).thenReturn(null);
        Authentication blankName = mock(Authentication.class);
        when(blankName.getName()).thenReturn("  ");
        Authentication failingName = mock(Authentication.class);
        when(failingName.getName()).thenThrow(new IllegalStateException("Authentication unavailable"));

        List<Authentication> authentications = List.of(nullName, blankName, failingName);
        when(repository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        for (int i = 0; i < authentications.size(); i++) {
            Product product = createProduct(20 + i, true, 150);
            when(repository.findAllById(List.of(20 + i))).thenReturn(List.of(product));
        }
        when(priceHistoryRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        try {
            for (int i = 0; i < authentications.size(); i++) {
                SecurityContextHolder.getContext().setAuthentication(authentications.get(i));
                service.bulkUpdatePrices(new BulkPriceUpdateRequest(List.of(
                        new PriceUpdateItem(20 + i, new BigDecimal("25.00")))));
            }
            ArgumentCaptor<List<PriceHistory>> historyCaptor = ArgumentCaptor.forClass(List.class);
            verify(priceHistoryRepository, times(3)).saveAll(historyCaptor.capture());
            assertEquals("ADMIN", historyCaptor.getAllValues().get(0).get(0).getChangedBy());
            assertEquals("ADMIN", historyCaptor.getAllValues().get(1).get(0).getChangedBy());
            assertEquals("ADMIN", historyCaptor.getAllValues().get(2).get(0).getChangedBy());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private ProductRegisterRequest validRegisterRequest() {
        ProductRegisterRequest request = new ProductRegisterRequest();
        request.setName("Nuevo producto");
        request.setPrice(new BigDecimal("20.00"));
        request.setStock(150);
        request.setCategoryId(1);
        return request;
    }

    private ProductRequestDTO validProductUpdateRequest() {
        ProductRequestDTO request = new ProductRequestDTO();
        request.setName("Producto actualizado");
        request.setPrice(new BigDecimal("20.00"));
        request.setStock(150);
        request.setCategoryId(1);
        return request;
    }

    private void assertInvalidRegistration(Consumer<ProductRegisterRequest> mutation) {
        ProductRegisterRequest request = validRegisterRequest();
        mutation.accept(request);
        assertThrows(ProductException.class, () -> service.saveProduct(request));
    }

    private void assertInvalidUpdate(Consumer<ProductRequestDTO> mutation) {
        ProductRequestDTO request = validProductUpdateRequest();
        mutation.accept(request);
        assertThrows(ProductException.class, () -> service.updateProduct(1, request));
    }
}
