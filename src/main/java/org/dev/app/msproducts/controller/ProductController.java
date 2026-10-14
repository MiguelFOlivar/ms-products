package org.dev.app.msproducts.controller;

import org.dev.app.msproducts.dto.ProductRegisterRequest;
import org.dev.app.msproducts.dto.ProductRegisterResponse;
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
    /**
     * Endpoint para dar de baja lógica a un producto del catálogo.
     * 
     * @DeleteMapping: Mapea solicitudes HTTP DELETE a la ruta base "/api/products" (sin parámetro en la URL).
     * @PreAuthorize: Restringe el acceso únicamente a usuarios autenticados con el rol 'ADMIN'.
     * @RequestHeader("id"): Extrae el identificador del producto desde la cabecera HTTP 'id' en lugar de la URL.
     * 
     * @param id Identificador numérico del producto a dar de baja (recibido por Header).
     * @return ResponseEntity con código 200 OK y mensaje de confirmación en formato JSON.
     */
    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteProduct(@RequestHeader("id") Integer id) {
        // Delegamos la lógica de negocio a la capa de servicio para cambiar el estado a inactivo
        service.deleteProduct(id);

        // Retornamos una respuesta HTTP 200 OK con un mensaje JSON confirmando la baja exitosa
        return ResponseEntity.ok(Map.of("message", "Producto retirado del catálogo con éxito."));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENT','ADMIN')")
    public ResponseEntity<ProductResponseDTO> saveProduct(@RequestBody ProductRequestDTO request){
        return ResponseEntity.ok(service.saveProduct(request));
    }

}
