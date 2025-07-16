package com.cpsoneghett.codingtask.controller;

import com.cpsoneghett.codingtask.domain.Device;
import com.cpsoneghett.codingtask.domain.DeviceFilter;
import com.cpsoneghett.codingtask.domain.DeviceRequestDto;
import com.cpsoneghett.codingtask.domain.DeviceState;
import com.cpsoneghett.codingtask.exception.DeviceNotFoundException;
import com.cpsoneghett.codingtask.service.DeviceServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DeviceController.class)
class DeviceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DeviceServiceImpl deviceService;

    @Autowired
    private ObjectMapper objectMapper;

    private Device device;
    private DeviceRequestDto deviceRequestDto;
    private Page<Device> devicePage;

    @BeforeEach
    void setUp() {
        device = new Device("Test Device", "Brand A", DeviceState.AVAILABLE);
        device.setId(1L);
        deviceRequestDto = new DeviceRequestDto("New Device", "Brand B", DeviceState.IN_USE);
        devicePage = new PageImpl<>(Collections.singletonList(device), PageRequest.of(0, 10), 1);
    }

    @Test
    void findAll_ShouldReturnPageOfDevices() throws Exception {
        when(deviceService.findAll(any(DeviceFilter.class), any(Pageable.class))).thenReturn(devicePage);

        mockMvc.perform(get("/v1/devices")
                        .param("page", "0")
                        .param("size", "10")
                        .param("brand", "Brand A"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value(device.getName()))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(deviceService, times(1)).findAll(any(DeviceFilter.class), any(Pageable.class));
    }

    @Test
    void findById_ShouldReturnNotFoundWhenDeviceNotFound() throws Exception {
        when(deviceService.findById(anyLong())).thenThrow(new DeviceNotFoundException(99L));

        mockMvc.perform(get("/v1/devices/{id}", 99L))
                .andExpect(status().isNotFound());

        verify(deviceService, times(1)).findById(99L);
    }

    @Test
    void create_ShouldReturnCreatedDevice() throws Exception {
        when(deviceService.save(any(DeviceRequestDto.class))).thenReturn(device);

        mockMvc.perform(post("/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deviceRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(device.getName()));

        verify(deviceService, times(1)).save(any(DeviceRequestDto.class));
    }

    @Test
    void create_ShouldReturnBadRequestWhenInvalidBody() throws Exception {
        // Missing 'name' which is @Valid
        String invalidJson = "{\"brand\":\"Brand X\", \"state\":\"AVAILABLE\"}";
        mockMvc.perform(post("/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(deviceService, times(0)).save(any(DeviceRequestDto.class));
    }

    @Test
    void delete_ShouldReturnNoContentOnSuccess() throws Exception {
        doNothing().when(deviceService).delete(1L);

        mockMvc.perform(delete("/v1/devices/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(deviceService, times(1)).delete(1L);
    }

    @Test
    void delete_ShouldReturnNotFoundWhenDeviceNotFound() throws Exception {
        doThrow(new DeviceNotFoundException(99L)).when(deviceService).delete(anyLong());

        mockMvc.perform(delete("/v1/devices/{id}", 99L))
                .andExpect(status().isNotFound());

        verify(deviceService, times(1)).delete(99L);
    }


    @Test
    void update_ShouldReturnUpdatedDevice() throws Exception {
        Device updatedDevice = new Device("Updated Name", "Updated Brand", DeviceState.IN_USE);
        updatedDevice.setId(1L);
        when(deviceService.update(eq(1L), any(DeviceRequestDto.class))).thenReturn(updatedDevice);

        mockMvc.perform(put("/v1/devices/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deviceRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(updatedDevice.getName()))
                .andExpect(jsonPath("$.brand").value(updatedDevice.getBrand()));

        verify(deviceService, times(1)).update(eq(1L), any(DeviceRequestDto.class));
    }

    @Test
    void update_ShouldReturnNotFoundWhenDeviceNotFound() throws Exception {
        when(deviceService.update(anyLong(), any(DeviceRequestDto.class))).thenThrow(new DeviceNotFoundException(99L));

        mockMvc.perform(put("/v1/devices/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deviceRequestDto)))
                .andExpect(status().isNotFound());

        verify(deviceService, times(1)).update(eq(99L), any(DeviceRequestDto.class));
    }

    @Test
    void partialUpdate_ShouldReturnPartiallyUpdatedDevice() throws Exception {
        Device partiallyUpdatedDevice = new Device("Patched Name", "Brand A", DeviceState.AVAILABLE);
        partiallyUpdatedDevice.setId(1L);
        JsonPatch jsonPatch = JsonPatch.fromJson(objectMapper.readTree("[{\"op\": \"replace\", \"path\": \"/name\", \"value\": \"Patched Name\"}]"));

        when(deviceService.partialUpdate(eq(1L), any(JsonPatch.class))).thenReturn(partiallyUpdatedDevice);

        mockMvc.perform(patch("/v1/devices/{id}", 1L)
                        .contentType("application/json-patch+json") // Correct content type for JSON Patch
                        .content(objectMapper.writeValueAsString(jsonPatch)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(partiallyUpdatedDevice.getName()));

        verify(deviceService, times(1)).partialUpdate(eq(1L), any(JsonPatch.class));
    }

    @Test
    void partialUpdate_ShouldReturnNotFoundWhenDeviceNotFound() throws Exception {
        JsonPatch jsonPatch = JsonPatch.fromJson(objectMapper.readTree("[]"));
        when(deviceService.partialUpdate(anyLong(), any(JsonPatch.class))).thenThrow(new DeviceNotFoundException(99L));

        mockMvc.perform(patch("/v1/devices/{id}", 99L)
                        .contentType("application/json-patch+json")
                        .content(objectMapper.writeValueAsString(jsonPatch)))
                .andExpect(status().isNotFound());

        verify(deviceService, times(1)).partialUpdate(eq(99L), any(JsonPatch.class));
    }

    @Test
    void partialUpdate_ShouldReturnBadRequestWhenJsonPatchException() throws Exception {
        JsonPatch jsonPatch = JsonPatch.fromJson(objectMapper.readTree("[]"));
        when(deviceService.partialUpdate(anyLong(), any(JsonPatch.class))).thenThrow(JsonPatchException.class);

        mockMvc.perform(patch("/v1/devices/{id}", 1L)
                        .contentType("application/json-patch+json")
                        .content(objectMapper.writeValueAsString(jsonPatch)))
                .andExpect(status().isBadRequest()); // Or 500 Internal Server Error, depending on global exception handling

        verify(deviceService, times(1)).partialUpdate(eq(1L), any(JsonPatch.class));
    }

}
