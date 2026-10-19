package org.dev.app.msproducts.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class PromotionRequest {

    @NotBlank(message = "El nombre de la promoción es obligatorio")
    @Size(max = 150, message = "El nombre no puede superar 150 caracteres")
    private String name;

    @NotNull(message = "El porcentaje de descuento es obligatorio")
    @DecimalMin(value = "0", inclusive = false, message = "El descuento debe ser mayor que 0")
    @DecimalMax(value = "50", message = "El descuento no puede superar el 50%")
    @Digits(integer = 3, fraction = 2, message = "El descuento admite hasta dos decimales")
    private BigDecimal discountPercentage;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDateTime startsAt;

    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDateTime endsAt;

    @NotEmpty(message = "La promoción debe incluir al menos un producto")
    private List<@NotNull @Positive Integer> productIds;

    private Boolean active;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(BigDecimal discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public LocalDateTime getStartsAt() {
        return startsAt;
    }

    public void setStartsAt(LocalDateTime startsAt) {
        this.startsAt = startsAt;
    }

    public LocalDateTime getEndsAt() {
        return endsAt;
    }

    public void setEndsAt(LocalDateTime endsAt) {
        this.endsAt = endsAt;
    }

    public List<Integer> getProductIds() {
        return productIds;
    }

    public void setProductIds(List<Integer> productIds) {
        this.productIds = productIds;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
