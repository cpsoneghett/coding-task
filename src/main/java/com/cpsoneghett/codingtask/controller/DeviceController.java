package com.cpsoneghett.codingtask.controller;

import com.cpsoneghett.codingtask.domain.Device;
import com.cpsoneghett.codingtask.domain.DeviceFilter;
import com.cpsoneghett.codingtask.domain.DeviceRequestDto;
import com.cpsoneghett.codingtask.service.DeviceService;
import com.cpsoneghett.codingtask.service.DeviceServiceImpl;
import com.cpsoneghett.codingtask.validation.DeviceCreateValidation;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/v1/devices")
@Tag(name = "Device Management", description = "APIs for creating, retrieving, and managing devices")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceServiceImpl deviceService) {
        this.deviceService = deviceService;
    }

    @Operation(summary = "Find all devices", description = "Retrieves a paginated list of devices, optionally filtered by brand or state.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of devices")
    })
    @GetMapping
    public ResponseEntity<Page<Device>> findAll(@Parameter(description = "Filter criteria for devices") DeviceFilter filter,
                                                @Parameter(description = "Pagination and sorting information") Pageable pageable) {
        return ResponseEntity.ok().body(deviceService.findAll(filter, pageable));
    }

    @Operation(summary = "Find device by ID", description = "Retrieves a single device by its unique ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the device"),
            @ApiResponse(responseCode = "404", description = "Device not found with the given ID", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<Device> findById(@Parameter(description = "ID of the device to be retrieved", required = true, example = "1")
                                           @PathVariable Long id) {
        return ResponseEntity.ok().body(deviceService.findById(id));
    }

    @Operation(summary = "Create a new device", description = "Adds a new device to the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Device created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body or validation error", content = @Content)
    })
    @PostMapping
    public ResponseEntity<Device> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Device object that needs to be added", required = true,
                    content = @Content(schema = @Schema(implementation = DeviceRequestDto.class)))
            @RequestBody @Validated(DeviceCreateValidation.class) DeviceRequestDto device) {
        return ResponseEntity.ok().body(deviceService.save(device));
    }


    @Operation(summary = "Delete a device", description = "Deletes a device by its ID. Cannot delete a device that is IN_USE.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Device deleted successfully", content = @Content),
            @ApiResponse(responseCode = "404", description = "Device not found with the given ID", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflict - Device is currently in use and cannot be deleted", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID of the device to be deleted", required = true, example = "1")
            @PathVariable("id") Long id) {
        deviceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update a device", description = "Updates all properties of an existing device. Name and brand cannot be updated if the device is IN_USE.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Device updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body or validation error", content = @Content),
            @ApiResponse(responseCode = "404", description = "Device not found with the given ID", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflict - Device is in use and certain fields cannot be updated", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<Device> update(
            @Parameter(description = "ID of the device to be updated", required = true, example = "1")
            @PathVariable("id") Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated device object", required = true,
                    content = @Content(schema = @Schema(implementation = DeviceRequestDto.class)))
            @RequestBody DeviceRequestDto deviceDto) {
        return ResponseEntity.ok().body(deviceService.update(id, deviceDto));
    }

    @Operation(summary = "Partially update a device", description = "Applies a partial update to a device using JSON Patch standard (RFC 6902).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Device partially updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid JSON Patch payload", content = @Content),
            @ApiResponse(responseCode = "404", description = "Device not found with the given ID", content = @Content)
    })
    @PatchMapping(path = "/{id}", consumes = "application/json-patch+json")
    public ResponseEntity<Device> partialUpdate(
            @Parameter(description = "ID of the device to be partially updated", required = true, example = "1")
            @PathVariable("id") Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "JSON Patch payload for the update", required = true,
                    content = @Content(mediaType = "application/json-patch+json",
                            schema = @Schema(example = "[{\"op\": \"replace\", \"path\": \"/state\", \"value\": \"INACTIVE\"}]")))
            @RequestBody JsonPatch jsonPatch)
            throws JsonPatchException, IOException {
        return ResponseEntity.ok().body(deviceService.partialUpdate(id, jsonPatch));
    }

}
