package com.cpsoneghett.codingtask.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DeviceRequestDto(
        @NotBlank(message = "Device name cannot be null or empty.")
        @Size(min = 2, max = 100, message = "Device name must be between 2 and 100 characters.")
        String name,

        @NotBlank(message = "Device brand cannot be null or empty.")
        @Size(min = 2, max = 100, message = "Device brand must be between 2 and 100 characters.")
        String brand,

        @NotNull(message = "Device state cannot be null.")
        DeviceState state) {
}
