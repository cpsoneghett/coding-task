package com.cpsoneghett.codingtask.service;

import com.cpsoneghett.codingtask.domain.Device;
import com.cpsoneghett.codingtask.domain.DeviceFilter;
import com.cpsoneghett.codingtask.domain.DeviceRequestDto;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;

public interface DeviceService {

    Page<Device> findAll(DeviceFilter filter, Pageable pageable);

    Device findById(Long id);

    Device save(DeviceRequestDto device);

    void delete(Long id);

    Device update(Long id, DeviceRequestDto deviceDto);

    Device partialUpdate(Long id, JsonPatch jsonPatch) throws JsonPatchException, IOException;
}
