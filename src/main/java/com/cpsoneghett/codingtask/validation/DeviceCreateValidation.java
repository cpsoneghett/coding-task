package com.cpsoneghett.codingtask.validation;

import jakarta.validation.GroupSequence;
import jakarta.validation.groups.Default;

@GroupSequence({Default.class, DeviceCreateValidation.Second.class})
public interface DeviceCreateValidation {

    interface Second {
    }
}
