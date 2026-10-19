package org.dev.app.msproducts.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import java.util.ArrayList;
import java.util.List;

import org.dev.app.msproducts.dto.ProductRegisterRequest;
import java.math.BigDecimal;

import org.dev.app.msproducts.dto.ImportErrorDTO;
import org.dev.app.msproducts.dto.ImportResultDTO;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import java.util.Locale;
import java.util.Set;

import org.dev.app.msproducts.exceptions.ProductException;
import org.dev.app.msproducts.exceptions.ProductAlreadyExistsException;

import org.dev.app.msproducts.exceptions.InvalidCsvException;
import java.io.UncheckedIOException;


@Service
public class ProductImportService {
    public int contarLineas(MultipartFile file) throws IOException {

        int contador = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        file.getInputStream(),
                        StandardCharsets.UTF_8))) {

            String linea;

            while ((linea = reader.readLine()) != null) {
                contador++;
            }
        }

        return contador;
    }

    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;

    private final Validator validator;
    private final IProductService productService;

    public ProductImportService(Validator validator, IProductService productService) {
        this.validator = validator;
        this.productService = productService;
    }


    public void validarArchivo(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new InvalidCsvException(
                    "El archivo CSV está vacío o no fue enviado"
            );
        }

        String nombreArchivo = file.getOriginalFilename();

        if (nombreArchivo == null ||
                !nombreArchivo.toLowerCase(Locale.ROOT).endsWith(".csv")) {

            throw new InvalidCsvException(
                    "El archivo debe tener extensión .csv"
            );
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidCsvException(
                    "El archivo supera el tamaño máximo de 5 MB"
            );
        }
    }



    public void validarEncabezados(MultipartFile file) throws IOException {

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        file.getInputStream(),
                        StandardCharsets.UTF_8))) {

            String encabezado = reader.readLine();

            String encabezadoEsperado =
                    "name,description,price,stock,categoryId";

            if (encabezado == null ||
                    !encabezadoEsperado.equals(encabezado)) {

                throw new IllegalArgumentException(
                        "Los encabezados del CSV no son válidos. " +
                                "Se esperan: " + encabezadoEsperado
                );
            }
        }
    }

    public List<ProductRegisterRequest> leerRegistrosCSV(
            MultipartFile file) throws IOException {

        List<ProductRegisterRequest> productos = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        file.getInputStream(),
                        StandardCharsets.UTF_8));
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .get()
                     .parse(reader)) {

            for (CSVRecord record : parser) {

                ProductRegisterRequest producto =
                        convertirRegistro(record);

                productos.add(producto);
            }
        }

        return productos;
    }



    private ProductRegisterRequest convertirRegistro(CSVRecord record) {

        if (!record.isConsistent()) {
            throw new IllegalArgumentException(
                    "La fila tiene columnas incompletas o adicionales"
            );
        }

        ProductRegisterRequest producto = new ProductRegisterRequest();

        producto.setName(record.get("name").trim());

        producto.setDescription(
                record.get("description").trim()
        );

        producto.setPrice(
                new BigDecimal(record.get("price").trim())
        );

        producto.setStock(
                Integer.parseInt(record.get("stock").trim())
        );

        producto.setCategoryId(
                Integer.parseInt(record.get("categoryId").trim())
        );

        return producto;
    }


    public ImportResultDTO procesarCSV(MultipartFile file)
            throws IOException {

        ImportResultDTO resultado = new ImportResultDTO();

        List<CSVRecord> registros;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        file.getInputStream(),
                        StandardCharsets.UTF_8));

             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .get()
                     .parse(reader)) {

            // Analizar completamente el CSV antes de guardar
            registros = parser.getRecords();

        } catch (IOException | UncheckedIOException ex) {

            throw new InvalidCsvException(
                    "El archivo CSV tiene un formato incorrecto"
            );
        }


        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        file.getInputStream(),
                        StandardCharsets.UTF_8));

             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .get()
                     .parse(reader)) {

            for (CSVRecord record : registros) {

                resultado.setProcesados(
                        resultado.getProcesados() + 1
                );

                try {
                    // 1. Convertir la fila CSV a un objeto Java
                    ProductRegisterRequest producto =
                            convertirRegistro(record);

                    // 2. Ejecutar validaciones Jakarta
                    validarProducto(producto);

                    // 3. Aplicar reglas de negocio y guardar en MySQL
                    productService.saveProduct(producto);

                    // 4. Contar solamente productos guardados
                    resultado.setAceptados(
                            resultado.getAceptados() + 1
                    );
                } catch (NumberFormatException ex) {
                    registrarError(
                            resultado,
                            record.getRecordNumber() + 1,
                            "Precio, stock o categoría deben ser numéricos"
                    );
                } catch (ProductAlreadyExistsException ex) {

                    registrarError(
                            resultado,
                            record.getRecordNumber() + 1,
                            ex.getMessage()
                    );
                } catch (ProductException ex) {

                    registrarError(
                            resultado,
                            record.getRecordNumber() + 1,
                            ex.getMessage()
                    );

                } catch (IllegalArgumentException ex) {
                    registrarError(
                            resultado,
                            record.getRecordNumber() + 1,
                            ex.getMessage()
                    );
                }
            }
        }


        if (registros.isEmpty()) {
            throw new IllegalArgumentException(
                    "El archivo CSV no contiene productos para importar"
            );
        }

        return resultado;
    }


    private void validarProducto(ProductRegisterRequest producto) {

        Set<ConstraintViolation<ProductRegisterRequest>> errores =
                validator.validate(producto);

        if (!errores.isEmpty()) {

            String mensaje = errores.stream()
                    .map(ConstraintViolation::getMessage)
                    .sorted()
                    .collect(java.util.stream.Collectors.joining("; "));

            throw new IllegalArgumentException(mensaje);
        }
    }


    private void registrarError(
            ImportResultDTO resultado,
            long fila,
            String mensaje) {

        resultado.setRechazados(
                resultado.getRechazados() + 1
        );

        resultado.getErrores().add(
                new ImportErrorDTO(fila, mensaje)
        );
    }



}
