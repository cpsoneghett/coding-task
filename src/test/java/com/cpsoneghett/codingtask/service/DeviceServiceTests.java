package com.cpsoneghett.codingtask.service;

import com.cpsoneghett.codingtask.domain.Device;
import com.cpsoneghett.codingtask.domain.DeviceFilter;
import com.cpsoneghett.codingtask.domain.DeviceRequestDto;
import com.cpsoneghett.codingtask.domain.DeviceState;
import com.cpsoneghett.codingtask.exception.BusinessException;
import com.cpsoneghett.codingtask.exception.DeviceNotFoundException;
import com.cpsoneghett.codingtask.repository.DeviceRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceServiceTests {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private DeviceServiceImpl deviceService;

    private Device device;
    private DeviceRequestDto deviceRequestDto;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        device = new Device("Test Device", "Brand A", DeviceState.IN_USE);
        device.setId(1L);
        deviceRequestDto = new DeviceRequestDto("Updated Device", "Brand B", DeviceState.IN_USE);
        pageable = PageRequest.of(0, 10);
    }

    @Test
    void findAll_ShouldReturnPageOfDevices() {
        DeviceFilter filter = new DeviceFilter("Brand A", null);
        Page<Device> expectedPage = new PageImpl<>(Collections.singletonList(device), pageable, 1);
        when(deviceRepository.filter(filter, pageable)).thenReturn(expectedPage);

        Page<Device> result = deviceService.findAll(filter, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(device.getName(), result.getContent().getFirst().getName());
        verify(deviceRepository, times(1)).filter(filter, pageable);
    }

    @Test
    void findById_ShouldReturnDeviceWhenFound() {
        when(deviceRepository.findById(1L)).thenReturn(Optional.of(device));

        Device result = deviceService.findById(1L);

        assertNotNull(result);
        assertEquals(device.getName(), result.getName());
        verify(deviceRepository, times(1)).findById(1L);
    }

    @Test
    void findById_ShouldThrowDeviceNotFoundExceptionWhenNotFound() {
        when(deviceRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(DeviceNotFoundException.class, () -> deviceService.findById(99L));
        verify(deviceRepository, times(1)).findById(99L);
    }

    @Test
    void save_ShouldReturnSavedDevice() {
        Device newDevice = new Device("New Device", "New Brand", DeviceState.AVAILABLE);
        when(deviceRepository.save(any(Device.class))).thenReturn(newDevice);

        DeviceRequestDto newDeviceDto = new DeviceRequestDto("New Device", "New Brand", DeviceState.AVAILABLE);
        Device result = deviceService.save(newDeviceDto);

        assertNotNull(result);
        assertEquals(newDevice.getName(), result.getName());
        assertEquals(newDevice.getBrand(), result.getBrand());
        assertEquals(newDevice.getState(), result.getState());
        verify(deviceRepository, times(1)).save(any(Device.class));
    }

    @Test
    void delete_ShouldDeleteDeviceWhenAvailable() {
        // Given
        device.setState(DeviceState.AVAILABLE);

        when(deviceRepository.findById(1L)).thenReturn(Optional.of(device));
        doNothing().when(deviceRepository).deleteById(1L);

        deviceService.delete(1L);

        verify(deviceRepository, times(1)).findById(1L);
        verify(deviceRepository, times(1)).deleteById(1L);
    }

    @Test
    void delete_ShouldThrowBusinessExceptionWhenDeviceInUse() {
        // Given
        device.setState(DeviceState.IN_USE);
        when(deviceRepository.findById(1L)).thenReturn(Optional.of(device));

        assertThrows(BusinessException.class, () -> deviceService.delete(1L));
        verify(deviceRepository, times(1)).findById(1L);
        verify(deviceRepository, never()).deleteById(anyLong());
    }

    @Test
    void delete_ShouldThrowDeviceNotFoundExceptionWhenDeviceNotFound() {
        when(deviceRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(DeviceNotFoundException.class, () -> deviceService.delete(99L));
        verify(deviceRepository, times(1)).findById(99L);
        verify(deviceRepository, never()).deleteById(anyLong());
    }

    @Test
    void delete_ShouldThrowEntityNotFoundExceptionWhenEmptyResultDataAccessException() {
        doThrow(EmptyResultDataAccessException.class).when(deviceRepository).findById(anyLong());

        assertThrows(EntityNotFoundException.class, () -> deviceService.delete(1L));
        verify(deviceRepository, times(1)).findById(1L);
        verify(deviceRepository, never()).deleteById(anyLong());
    }


    @Test
    void update_ShouldReturnUpdatedDevice() {
        when(deviceRepository.findById(1L)).thenReturn(Optional.of(device));
        when(deviceRepository.save(any(Device.class))).thenReturn(device);

        Device result = deviceService.update(1L, deviceRequestDto);

        assertNotNull(result);
        assertEquals(deviceRequestDto.name(), result.getName());
        assertEquals(deviceRequestDto.brand(), result.getBrand());
        assertEquals(deviceRequestDto.state(), result.getState());
        verify(deviceRepository, times(1)).findById(1L);
        verify(deviceRepository, times(1)).save(device);
    }

    @Test
    void update_ShouldThrowDeviceNotFoundExceptionWhenDeviceNotFound() {
        when(deviceRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(DeviceNotFoundException.class, () -> deviceService.update(99L, deviceRequestDto));
        verify(deviceRepository, times(1)).findById(99L);
        verify(deviceRepository, never()).save(any(Device.class));
    }

    @Test
    void partialUpdate_ShouldReturnPartiallyUpdatedDevice() throws JsonPatchException, JsonProcessingException {
        JsonPatch jsonPatch = mock(JsonPatch.class);
        JsonNode deviceJsonNode = mock(JsonNode.class);
        JsonNode patchedJsonNode = mock(JsonNode.class);

        when(deviceRepository.findById(1L)).thenReturn(Optional.of(device));
        when(objectMapper.convertValue(eq(device), eq(JsonNode.class))).thenReturn(deviceJsonNode);
        when(jsonPatch.apply(deviceJsonNode)).thenReturn(patchedJsonNode);
        when(objectMapper.treeToValue(eq(patchedJsonNode), eq(Device.class))).thenReturn(device);
        when(deviceRepository.save(any(Device.class))).thenReturn(device);

        Device result = deviceService.partialUpdate(1L, jsonPatch);

        assertNotNull(result);
        verify(deviceRepository, times(1)).findById(1L);
        verify(objectMapper, times(1)).convertValue(eq(device), eq(JsonNode.class));
        verify(jsonPatch, times(1)).apply(deviceJsonNode);
        verify(objectMapper, times(1)).treeToValue(eq(patchedJsonNode), eq(Device.class));
        verify(deviceRepository, times(1)).save(device);
    }

    @Test
    void partialUpdate_ShouldThrowDeviceNotFoundExceptionWhenDeviceNotFound() throws JsonPatchException, JsonProcessingException {
        JsonPatch jsonPatch = mock(JsonPatch.class);
        when(deviceRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(DeviceNotFoundException.class, () -> deviceService.partialUpdate(99L, jsonPatch));
        verify(deviceRepository, times(1)).findById(99L);
        verify(objectMapper, never()).convertValue(any(), (JavaType) any());
        verify(jsonPatch, never()).apply(any());
        verify(objectMapper, never()).treeToValue(any(), (JavaType) any());
        verify(deviceRepository, never()).save(any(Device.class));
    }


}
