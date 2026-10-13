package org.dev.app.msproducts.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class ProductRegisterRequest {
    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    private String description;
    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "el precio debe ser mayor a Cero")
    @Digits(
            integer = 10,
            fraction = 2,
            message = "El formato del producto es incorrecto"
    )
    private BigDecimal price;



    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;
    private Integer active;
    private String date;

    @NotNull(message = "La categoría es obligatoria")
    private Integer categoryId;
    private Integer id;
    private Integer quantity;

    public Integer getQuantity(){return quantity;}

    public void setQuantity (Integer quantity){this.quantity = quantity;}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Integer getActive() {
        return active;
    }

    public void setActive(Integer active) {
        this.active = active;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }
}
