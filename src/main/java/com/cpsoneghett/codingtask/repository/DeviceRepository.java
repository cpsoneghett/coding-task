package com.cpsoneghett.codingtask.repository;

import com.cpsoneghett.codingtask.domain.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long>, DeviceRepositoryQuery {
}
