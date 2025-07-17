package com.cpsoneghett.codingtask.controller;

import com.cpsoneghett.codingtask.domain.Device;
import com.cpsoneghett.codingtask.domain.DeviceRequestDto;
import com.cpsoneghett.codingtask.domain.DeviceState;
import com.cpsoneghett.codingtask.exception.DeviceInUseException;
import com.cpsoneghett.codingtask.exception.DeviceNotFoundException;
import com.cpsoneghett.codingtask.service.DeviceServiceImpl;
import com.cpsoneghett.codingtask.utils.OperationType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(DeviceController.class)
class DeviceControllerTest {

    private final Long deviceId = 1L;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
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
    @DisplayName("GET /v1/devices/{id}")
    class GetById {
        @Test
        @DisplayName("Should return 200 OK and Device when found")
        void findById_WhenDeviceExists_ShouldReturnDevice() throws Exception {
            // Given
            given(deviceService.findById(deviceId)).willReturn(device);

            // When / Then
            mockMvc.perform(get("/v1/devices/{id}", deviceId))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(deviceId.intValue())))
                    .andExpect(jsonPath("$.name", is(device.getName())));
        }

        @Test
        @DisplayName("Should return 404 Not Found when device does not exist")
        void findById_WhenDeviceNotExists_ShouldReturnNotFound() throws Exception {
            // Given
            given(deviceService.findById(deviceId)).willThrow(new DeviceNotFoundException(deviceId));

            // When / Then
            mockMvc.perform(get("/v1/devices/{id}", deviceId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title", is("Business rule violation.")))
                    .andExpect(jsonPath("$.status", is(404)));
        }
    }

    @Nested
    @DisplayName("POST /v1/devices")
    class CreateDevice {
        @Test
        @DisplayName("Should return 200 OK and created Device for valid request")
        void create_WithValidBody_ShouldReturnCreatedDevice() throws Exception {
            // Given
            given(deviceService.save(any(DeviceRequestDto.class))).willReturn(device);

            // When / Then
            mockMvc.perform(post("/v1/devices")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(deviceRequestDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(deviceId.intValue())))
                    .andExpect(jsonPath("$.name", is(deviceRequestDto.name())));
        }

        @Test
        @DisplayName("Should return 400 Bad Request for invalid request body")
        void create_WithInvalidBody_ShouldReturnBadRequest() throws Exception {
            // Given
            DeviceRequestDto invalidDto = new DeviceRequestDto("", "", null);

            // When / Then
            mockMvc.perform(post("/v1/devices")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title", is("Business rule violation.")))
                    .andExpect(jsonPath("$.errors.length()", is(3)));
        }
    }

    @Nested
    @DisplayName("DELETE /v1/devices/{id}")
    class DeleteDevice {
        @Test
        @DisplayName("Should return 204 No Content for successful deletion")
        void delete_WhenDeviceCanBeDeleted_ShouldReturnNoContent() throws Exception {
            // Given
            willDoNothing().given(deviceService).delete(deviceId);

            // When / Then
            mockMvc.perform(delete("/v1/devices/{id}", deviceId))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Should return 409 Conflict when deleting a device in use")
        void delete_WhenDeviceInUse_ShouldReturnConflict() throws Exception {
            // Given
            willThrow(new DeviceInUseException(deviceId, OperationType.DELETE))
                    .given(deviceService)
                    .delete(deviceId);

            // When / Then
            mockMvc.perform(delete("/v1/devices/{id}", deviceId))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.title", is("Business rule violation.")))
                    .andExpect(jsonPath("$.status", is(409)));
        }
    }

    @Nested
    @DisplayName("PATCH /v1/devices/{id}")
    class PatchDevice {
        @Test
        @DisplayName("Should return 400 Bad Request for invalid patch operation")
        void partialUpdate_WithInvalidPatch_ShouldReturnBadRequest() throws Exception {
            // Given
            String invalidPatchPayload = "[{\"op\": \"test\", \"path\": \"/name\", \"value\": \"invalid\"}]";
            given(deviceService.partialUpdate(eq(deviceId), any()))
                    .willThrow(new com.github.fge.jsonpatch.JsonPatchException("Invalid patch"));

            // When / Then
            mockMvc.perform(patch("/v1/devices/{id}", deviceId)
                            .contentType("application/json-patch+json")
                            .content(invalidPatchPayload))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title", is("Incomprehensive message.")));
        }
    }

}


