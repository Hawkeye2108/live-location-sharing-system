package com.org.livelocationsharing.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * Request body for POST /session/{sessionId}/location
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
public class LocationUpdateRequest {

    @NotNull(message = "latitude is required")
    @DecimalMin(value = "-90.0",  message = "latitude must be >= -90")
    @DecimalMax(value = "90.0",   message = "latitude must be <= 90")
    private BigDecimal latitude;

    @NotNull(message = "longitude is required")
    @DecimalMin(value = "-180.0", message = "longitude must be >= -180")
    @DecimalMax(value = "180.0",  message = "longitude must be <= 180")
    private BigDecimal longitude;
}