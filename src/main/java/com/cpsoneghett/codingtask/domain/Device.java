package com.cpsoneghett.codingtask.domain;

import com.cpsoneghett.codingtask.validation.DeviceCreateValidation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "device")
@GroupSequence({Device.class, DeviceCreateValidation.Second.class})
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Device name cannot be null or empty.")
    @Size(min = 2, max = 100, message = "Device name must be between 2 and 100 characters.", groups = {DeviceCreateValidation.Second.class})
    private String name;

    @NotBlank(message = "Device brand cannot be null or empty.")
    @Size(min = 2, max = 100, message = "Device brand must be between 2 and 100 characters.", groups = {DeviceCreateValidation.Second.class})
    private String brand;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Device state cannot be null.")
    private DeviceState state;

    @CreationTimestamp
    @Column(name = "dt_created")
    private LocalDateTime createdAt;

    public Device(String name, String brand, DeviceState state) {
        this.name = name;
        this.brand = brand;
        this.state = state;
        createdAt = LocalDateTime.now();
    }

    public Device() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public DeviceState getState() {
        return state;
    }

    public void setState(DeviceState state) {
        this.state = state;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isEqualsDto(DeviceRequestDto dto) {
        return this.name.equals(dto.name().trim()) && this.brand.equals(dto.brand().trim()) && this.state.equals(dto.state());
    }

}
