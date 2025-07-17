package com.cpsoneghett.codingtask.domain;

import com.cpsoneghett.codingtask.validation.DeviceCreateValidation;
import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@GroupSequence({DeviceRequestDto.class, DeviceCreateValidation.Second.class})
public record DeviceRequestDto(
        @NotBlank(message = "Device name cannot be null or empty.")
        @Size(min = 2, max = 100, message = "Device name must be between 2 and 100 characters.", groups = DeviceCreateValidation.Second.class)
        String name,
        @NotBlank(message = "Device name cannot be null or empty.")
        @Size(min = 2, max = 100, message = "Device brand must be between 2 and 100 characters.", groups = DeviceCreateValidation.Second.class)
        String brand,
        @NotNull(message = "Device state cannot be null.")
        DeviceState state) {
}
