package com.cpsoneghett.codingtask.controller;

import com.cpsoneghett.codingtask.domain.Device;
import com.cpsoneghett.codingtask.domain.DeviceRequestDto;
import com.cpsoneghett.codingtask.service.DeviceService;
import com.cpsoneghett.codingtask.service.DeviceServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/devices")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceServiceImpl deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping
    public ResponseEntity<List<Device>> findAll() {
        return ResponseEntity.ok().body(deviceService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Device> findById(@PathVariable Long id) {
        return ResponseEntity.ok().body(deviceService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Device> create(@RequestBody @Valid DeviceRequestDto device) {
        return ResponseEntity.ok().body(deviceService.save(device));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        deviceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Device> update(@PathVariable("id") Long id, @RequestBody @Valid DeviceRequestDto deviceDto) {
        return ResponseEntity.ok().body(deviceService.update(id, deviceDto));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Device> partialUpdate(@PathVariable("id") Long id, @RequestBody @Valid JsonPatch jsonPatch) throws JsonPatchException, JsonProcessingException {
        return ResponseEntity.ok().body(deviceService.partialUpdate(id, jsonPatch));
    }

}
