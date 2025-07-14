package com.cpsoneghett.codingtask.controller;

import com.cpsoneghett.codingtask.domain.Device;
import com.cpsoneghett.codingtask.service.DeviceServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/devices")
public class DeviceController {

    private final DeviceServiceImpl deviceServiceImpl;

    public DeviceController(DeviceServiceImpl deviceServiceImpl) {
        this.deviceServiceImpl = deviceServiceImpl;
    }

    @GetMapping
    public ResponseEntity<List<Device>> findAll()  {
        return ResponseEntity.ok().body(deviceServiceImpl.findAll());
    }


}
