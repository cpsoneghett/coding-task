package com.cpsoneghett.codingtask.exception;

public class DeviceInUseException extends BusinessException {

    public DeviceInUseException(String message) {
        super(message);
    }

    public DeviceInUseException(Long id) {
        super(String.format("Device with id %s is being used", id));
    }
}
