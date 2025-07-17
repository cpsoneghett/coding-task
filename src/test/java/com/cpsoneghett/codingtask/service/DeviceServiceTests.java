package com.cpsoneghett.codingtask.service;

import com.cpsoneghett.codingtask.domain.Device;
import com.cpsoneghett.codingtask.domain.DeviceRequestDto;
import com.cpsoneghett.codingtask.domain.DeviceState;
import com.cpsoneghett.codingtask.exception.DeviceInUseException;
import com.cpsoneghett.codingtask.exception.DeviceNotFoundException;
import com.cpsoneghett.codingtask.repository.DeviceRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class DeviceServiceTests {

    private final Long deviceId = 1L;
    @Mock
    private DeviceRepository deviceRepository;
    @Mock
    private ObjectMapper objectMapper;
    @InjectMocks
    private DeviceServiceImpl deviceService;
    private Device device;
    private DeviceRequestDto deviceRequestDto;

    @BeforeEach
    void setUp() {
        device = new Device("iPhone 15 Pro", "Apple", DeviceState.AVAILABLE);
        device.setId(deviceId);

        deviceRequestDto = new DeviceRequestDto(
                "iPhone 15 Pro",
                "Apple",
                DeviceState.AVAILABLE
        );
    }


    @Nested
    @DisplayName("FindById Tests")
    class FindByIdTests {

        @Test
        @DisplayName("Should return device when found")
        void findById_WhenDeviceExists_ShouldReturnDevice() {
            // Given
            given(deviceRepository.findById(deviceId)).willReturn(Optional.of(device));

            // When
            Device foundDevice = deviceService.findById(deviceId);

            // Then
            assertThat(foundDevice).isNotNull();
            assertThat(foundDevice.getId()).isEqualTo(deviceId);
            then(deviceRepository).should().findById(deviceId);
        }

        @Test
        @DisplayName("Should throw DeviceNotFoundException when device not found")
        void findById_WhenDeviceDoesNotExist_ShouldThrowDeviceNotFoundException() {
            // Given
            given(deviceRepository.findById(deviceId)).willReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> deviceService.findById(deviceId))
                    .isInstanceOf(DeviceNotFoundException.class)
                    .hasMessage(String.format("Device with id %s not found", deviceId));

            then(deviceRepository).should().findById(deviceId);
        }
    }

    @Nested
    @DisplayName("Save Tests")
    class SaveTests {
        @Test
        @DisplayName("Should save a new device successfully")
        void save_WithValidDto_ShouldReturnSavedDevice() {
            // Given
            Device savedDevice = new Device(deviceRequestDto.name(), deviceRequestDto.brand(), deviceRequestDto.state());
            BeanUtils.copyProperties(deviceRequestDto, savedDevice);
            savedDevice.setId(deviceId);
            given(deviceRepository.save(any(Device.class))).willReturn(savedDevice);

            // When
            Device result = deviceService.save(deviceRequestDto);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(deviceId);
            assertThat(result.getName()).isEqualTo(deviceRequestDto.name());
            then(deviceRepository).should().save(any(Device.class));
        }
    }

    @Nested
    @DisplayName("Delete Tests")
    class DeleteTests {

        @Test
        @DisplayName("Should delete device when it is not in use")
        void delete_WhenDeviceIsNotInUse_ShouldDeleteSuccessfully() {
            // Given
            device.setState(DeviceState.AVAILABLE);
            given(deviceRepository.findById(deviceId)).willReturn(Optional.of(device));

            // When
            deviceService.delete(deviceId);

            // Then
            then(deviceRepository).should().findById(deviceId);
            then(deviceRepository).should().deleteById(deviceId);
        }

        @Test
        @DisplayName("Should throw DeviceInUseException when trying to delete a device in use")
        void delete_WhenDeviceIsInUse_ShouldThrowDeviceInUseException() {
            // Given
            device.setState(DeviceState.IN_USE);
            given(deviceRepository.findById(deviceId)).willReturn(Optional.of(device));

            // When / Then
            assertThatThrownBy(() -> deviceService.delete(deviceId))
                    .isInstanceOf(DeviceInUseException.class)
                    .hasMessageContaining(String.format("Device with id %s is being used. The device cannot be deleted. Change the current state of the device.", deviceId));

            then(deviceRepository).should().findById(deviceId);
            then(deviceRepository).should(never()).deleteById(any(Long.class));
        }
    }

    @Nested
    @DisplayName("Update Tests")
    class UpdateTests {

        @Test
        @DisplayName("Should update all properties when device is not in use")
        void update_WhenDeviceIsNotInUse_ShouldUpdateSuccessfully() {
            // Given
            device.setState(DeviceState.AVAILABLE);
            DeviceRequestDto updateDto = new DeviceRequestDto("Galaxy S25", "Samsung", DeviceState.INACTIVE);
            given(deviceRepository.findById(deviceId)).willReturn(Optional.of(device));
            given(deviceRepository.save(any(Device.class))).willAnswer(invocation -> invocation.getArgument(0));

            // When
            Device updatedDevice = deviceService.update(deviceId, updateDto);

            // Then
            assertThat(updatedDevice.getName()).isEqualTo(updateDto.name());
            assertThat(updatedDevice.getBrand()).isEqualTo(updateDto.brand());
            assertThat(updatedDevice.getState()).isEqualTo(updateDto.state());
            then(deviceRepository).should().findById(deviceId);
            then(deviceRepository).should().save(device);
        }

        @Test
        @DisplayName("Should throw DeviceInUseException when updating name of a device in use")
        void update_WhenDeviceIsInUseAndNameChanges_ShouldThrowDeviceInUseException() {
            // Given
            device.setState(DeviceState.IN_USE);
            DeviceRequestDto updateDto = new DeviceRequestDto("Different Name", device.getBrand(), device.getState());
            given(deviceRepository.findById(deviceId)).willReturn(Optional.of(device));

            // When / Then
            assertThatThrownBy(() -> deviceService.update(deviceId, updateDto))
                    .isInstanceOf(DeviceInUseException.class)
                    .hasMessageContaining(String.format("Device with id %s is being used. These field(s) cannot be updated. Change the current state of the device.", deviceId));

            then(deviceRepository).should().findById(deviceId);
            then(deviceRepository).should(never()).save(any(Device.class));
        }

        @Test
        @DisplayName("Should throw DeviceInUseException when updating brand of a device in use")
        void update_WhenDeviceIsInUseAndBrandChanges_ShouldThrowDeviceInUseException() {
            // Given
            device.setState(DeviceState.IN_USE);
            DeviceRequestDto updateDto = new DeviceRequestDto(device.getName(), "Different Brand", device.getState());
            given(deviceRepository.findById(deviceId)).willReturn(Optional.of(device));

            // When / Then
            assertThatThrownBy(() -> deviceService.update(deviceId, updateDto))
                    .isInstanceOf(DeviceInUseException.class)
                    .hasMessageContaining(String.format("Device with id %s is being used. These field(s) cannot be updated. Change the current state of the device.", deviceId));

            then(deviceRepository).should().findById(deviceId);
            then(deviceRepository).should(never()).save(any(Device.class));
        }

        @Test
        @DisplayName("Should update state only when device is in use")
        void update_WhenDeviceIsInUseAndOnlyStateChanges_ShouldUpdateSuccessfully() {
            // Given
            device.setState(DeviceState.IN_USE);
            DeviceRequestDto updateDto = new DeviceRequestDto(device.getName(), device.getBrand(), DeviceState.AVAILABLE);
            given(deviceRepository.findById(deviceId)).willReturn(Optional.of(device));
            given(deviceRepository.save(any(Device.class))).willAnswer(invocation -> invocation.getArgument(0));

            // When
            Device updatedDevice = deviceService.update(deviceId, updateDto);

            // Then
            assertThat(updatedDevice.getState()).isEqualTo(updateDto.state());
            assertThat(updatedDevice.getName()).isEqualTo(device.getName());
            assertThat(updatedDevice.getBrand()).isEqualTo(device.getBrand());
            then(deviceRepository).should().findById(deviceId);
            then(deviceRepository).should().save(device);
        }

        @Test
        @DisplayName("Should not call save when there are no changes")
        void update_WhenNoChanges_ShouldNotSave() {
            // Given
            given(deviceRepository.findById(deviceId)).willReturn(Optional.of(device));

            // When
            Device result = deviceService.update(deviceId, deviceRequestDto);

            // Then
            assertThat(result).isEqualTo(device);
            then(deviceRepository).should().findById(deviceId);
            then(deviceRepository).should(never()).save(any(Device.class));
        }
    }

    @Nested
    @DisplayName("Partial Update (JSON Patch) Tests")
    class PartialUpdateTests {

        @Mock
        private JsonPatch jsonPatch;

        @Mock
        private JsonNode deviceNode, patchedNode;

        @Test
        @DisplayName("Should apply patch and save when device exists")
        void partialUpdate_WhenDeviceExists_ShouldApplyPatchAndSave() throws JsonPatchException, IOException {
            // Given
            Device patchedDevice = new Device("Patched Name", "Patched Brand", DeviceState.IN_USE);

            given(deviceRepository.findById(deviceId)).willReturn(Optional.of(device));
            given(objectMapper.convertValue(device, JsonNode.class)).willReturn(deviceNode);
            given(jsonPatch.apply(deviceNode)).willReturn(patchedNode);
            given(objectMapper.treeToValue(patchedNode, Device.class)).willReturn(patchedDevice);
            given(deviceRepository.save(patchedDevice)).willReturn(patchedDevice);

            // When
            Device result = deviceService.partialUpdate(deviceId, jsonPatch);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Patched Name");
            then(deviceRepository).should().findById(deviceId);
            then(deviceRepository).should().save(patchedDevice);
        }

        @Test
        @DisplayName("Should throw DeviceNotFoundException when device not found")
        void partialUpdate_WhenDeviceNotFound_ShouldThrowDeviceNotFoundException() {
            // Given
            given(deviceRepository.findById(deviceId)).willReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> deviceService.partialUpdate(deviceId, jsonPatch))
                    .isInstanceOf(DeviceNotFoundException.class);

            then(deviceRepository).should().findById(deviceId);
            then(deviceRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("Should propagate JsonPatchException on patch failure")
        void partialUpdate_WhenPatchFails_ShouldPropagateException() throws JsonPatchException {
            // Given
            given(deviceRepository.findById(deviceId)).willReturn(Optional.of(device));
            given(objectMapper.convertValue(device, JsonNode.class)).willReturn(deviceNode);
            given(jsonPatch.apply(deviceNode)).willThrow(JsonPatchException.class);

            // When / Then
            assertThatThrownBy(() -> deviceService.partialUpdate(deviceId, jsonPatch))
                    .isInstanceOf(JsonPatchException.class);
        }
    }
}
