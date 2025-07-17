package com.cpsoneghett.codingtask.exception;

import com.cpsoneghett.codingtask.utils.OperationType;

public class DeviceInUseException extends BusinessException {

    public DeviceInUseException(Long id, OperationType operationType) {
        super(buildMessage(id, operationType));
    }

    private static String buildMessage(Long id, OperationType operationType) {
        String baseMessage = String.format("Device with id %s is being used.", id);
        return switch (operationType) {
            case DELETE -> baseMessage + " The device cannot be deleted. Change the current state of the device.";
            case UPDATE -> baseMessage + " These field(s) cannot be updated. Change the current state of the device.";
        };
    }
}
