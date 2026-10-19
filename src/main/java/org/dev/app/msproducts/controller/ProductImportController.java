package org.dev.app.msproducts.controller;


import org.dev.app.msproducts.service.ProductImportService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


import org.dev.app.msproducts.dto.ImportResultDTO;

@RestController
@RequestMapping("/api/admin/products")
public class ProductImportController {
    private final ProductImportService importService;

    public ProductImportController(ProductImportService importService) {
        this.importService = importService;
    }

    @PostMapping(
            value = "/import",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ImportResultDTO> importProducts(
            @RequestParam("file") MultipartFile file)
            throws IOException {

        importService.validarArchivo(file);
        importService.validarEncabezados(file);
        ImportResultDTO resultado = importService.procesarCSV(file);

        return ResponseEntity.ok(resultado);
    }

}
