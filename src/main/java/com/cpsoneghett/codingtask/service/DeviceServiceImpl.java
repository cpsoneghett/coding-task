package com.cpsoneghett.codingtask.service;

import com.cpsoneghett.codingtask.domain.Device;
import com.cpsoneghett.codingtask.domain.DeviceRequestDto;
import com.cpsoneghett.codingtask.repository.DeviceRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepository;

    public DeviceServiceImpl(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    public List<Device> findAll() {
        return deviceRepository.findAll();
    }

    public Device findById(Long id) {
        return deviceRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Device with ID " + id + " not found."));
    }

    public Device save(DeviceRequestDto device) {

        Device newDevice = new Device(device.name(), device.brand(), device.state());

        return deviceRepository.save(newDevice);
    }

    public void delete(Long id) {
        try {
            deviceRepository.deleteById(id);
        } catch (EmptyResultDataAccessException ex) {
            throw new EntityNotFoundException();
        }
    }

    public Device update(Device device) {
        if (device == null) {
            throw new IllegalArgumentException("Device cannot be null.");
        }
        // Validate ID for update
        if (device.getId() == null || device.getId() <= 0) {
            throw new IllegalArgumentException("Device ID must not be null or negative for update operation.");
        }

        return deviceRepository.save(device);
    }
}
