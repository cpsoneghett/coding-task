package com.cpsoneghett.codingtask.repository;

import com.cpsoneghett.codingtask.domain.Device;
import com.cpsoneghett.codingtask.domain.DeviceFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DeviceRepositoryQuery {

    Page<Device> filter(DeviceFilter filter, Pageable pageable);
}
