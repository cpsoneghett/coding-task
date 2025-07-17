package com.cpsoneghett.codingtask.exception;

public class DeviceNotFoundException extends BusinessException {

    public DeviceNotFoundException(Long id) {
        super(String.format("Device with id %s not found", id));
    }
}
