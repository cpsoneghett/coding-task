package com.cpsoneghett.codingtask.repository;

import com.cpsoneghett.codingtask.domain.Device;
import com.cpsoneghett.codingtask.domain.DeviceFilter;
import com.cpsoneghett.codingtask.repository.impl.DeviceRepositoryQueryImpl;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceRepositoryQueryImplTest {

    @InjectMocks
    private DeviceRepositoryQueryImpl deviceRepositoryQuery;

    @Mock
    private EntityManager em;

    @Mock
    private CriteriaBuilder builder;

    @Mock
    private CriteriaQuery<Device> criteriaDevice;

    @Mock
    private CriteriaQuery<Long> criteriaLong;

    @Mock
    private Root<Device> root;

    @Mock
    private TypedQuery<Device> typedQueryDevice;

    @Mock
    private TypedQuery<Long> typedQueryLong;

    private DeviceFilter filter;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        filter = new DeviceFilter("brand", "state");
        pageable = PageRequest.of(0, 10);

        when(em.getCriteriaBuilder()).thenReturn(builder);
        when(builder.createQuery(Device.class)).thenReturn(criteriaDevice);
        when(builder.createQuery(Long.class)).thenReturn(criteriaLong);
        when(criteriaDevice.from(Device.class)).thenReturn(root);
        when(criteriaLong.from(Device.class)).thenReturn(root);

        when(em.createQuery(criteriaDevice)).thenReturn(typedQueryDevice);
        when(em.createQuery(criteriaLong)).thenReturn(typedQueryLong);

        when(typedQueryDevice.getResultList()).thenReturn(Collections.singletonList(new Device()));
        when(typedQueryLong.getSingleResult()).thenReturn(1L);
    }

    @Test
    @DisplayName("Should return page of devices")
    void filter_ShouldReturnPageOfDevices() {
        Page<Device> result = deviceRepositoryQuery.filter(filter, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        verify(em, times(2)).getCriteriaBuilder();
        verify(builder, times(1)).createQuery(Device.class);
        verify(builder, times(1)).createQuery(Long.class);
        verify(criteriaDevice, times(1)).from(Device.class);
        verify(criteriaLong, times(1)).from(Device.class);
        verify(criteriaDevice, times(1)).where(any(Predicate[].class));
        verify(criteriaLong, times(1)).where(any(Predicate[].class));
        verify(em, times(1)).createQuery(criteriaDevice);
        verify(typedQueryDevice, times(1)).setFirstResult(0);
        verify(typedQueryDevice, times(1)).setMaxResults(10);
        verify(typedQueryDevice, times(1)).getResultList();
        verify(typedQueryLong, times(1)).getSingleResult();
    }

    @Test
    @DisplayName("Should return page of devices")
    void filter_ShouldHandleEmptyFilter() {
        DeviceFilter emptyFilter = new DeviceFilter(null, null);
        Page<Device> result = deviceRepositoryQuery.filter(emptyFilter, pageable);

        assertNotNull(result);
    }
}
