package com.cpsoneghett.codingtask.service;

import com.cpsoneghett.codingtask.domain.Device;
import com.cpsoneghett.codingtask.domain.DeviceRequestDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;

import java.util.List;

public interface DeviceService {

    List<Device> findAll();

    Device findById(Long id);

    Device save(DeviceRequestDto device);

    void delete(Long id);

    Device update(Long id, DeviceRequestDto deviceDto);

    Device partialUpdate(Long id, JsonPatch jsonPatch) throws JsonPatchException, JsonProcessingException;
}
