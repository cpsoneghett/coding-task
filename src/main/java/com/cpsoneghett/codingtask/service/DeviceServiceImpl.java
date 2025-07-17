package com.cpsoneghett.codingtask.service;

import com.cpsoneghett.codingtask.domain.Device;
import com.cpsoneghett.codingtask.domain.DeviceFilter;
import com.cpsoneghett.codingtask.domain.DeviceRequestDto;
import com.cpsoneghett.codingtask.domain.DeviceState;
import com.cpsoneghett.codingtask.exception.DeviceInUseException;
import com.cpsoneghett.codingtask.exception.DeviceNotFoundException;
import com.cpsoneghett.codingtask.repository.DeviceRepository;
import com.cpsoneghett.codingtask.utils.OperationType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepository;
    private final ObjectMapper objectMapper;

    public DeviceServiceImpl(DeviceRepository deviceRepository, ObjectMapper objectMapper) {
        this.deviceRepository = deviceRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public Page<Device> findAll(DeviceFilter filter, Pageable pageable) {
        return deviceRepository.filter(filter, pageable);
    }

    @Override
    public Device findById(Long id) {
        return deviceRepository.findById(id).orElseThrow(() -> new DeviceNotFoundException(id));
    }

    @Override
    public Device save(DeviceRequestDto device) {

        Device newDevice = new Device(device.name(), device.brand(), device.state());

        return deviceRepository.save(newDevice);
    }

    @Override
    public void delete(Long id) {
        try {
            Device deviceFound = this.findById(id);

            if (DeviceState.IN_USE.equals(deviceFound.getState()))
                throw new DeviceInUseException(deviceFound.getId(), OperationType.DELETE);

            deviceRepository.deleteById(id);
        } catch (EmptyResultDataAccessException ex) {
            throw new EntityNotFoundException();
        }
    }

    @Override
    public Device update(Long id, DeviceRequestDto deviceDto) {

        Device deviceFound = this.findById(id);

        if (deviceFound.isEqualsDto(deviceDto)) return deviceFound;

        if (DeviceState.IN_USE.equals(deviceFound.getState())) {
            boolean isChangingName = !deviceDto.name().trim().equals(deviceFound.getName());
            boolean isChangingBrand = !deviceDto.brand().trim().equals(deviceFound.getBrand());

            if (isChangingName || isChangingBrand)
                throw new DeviceInUseException(deviceFound.getId(), OperationType.UPDATE);

        }

        BeanUtils.copyProperties(deviceDto, deviceFound, "id");

        return deviceRepository.save(deviceFound);
    }

    @Override
    public Device partialUpdate(Long id, JsonPatch jsonPatch) throws JsonPatchException, IOException {

        Device deviceFound = this.findById(id);

        JsonNode patched = jsonPatch.apply(objectMapper.convertValue(deviceFound, JsonNode.class));

        return deviceRepository.save(objectMapper.treeToValue(patched, Device.class));
    }

}
