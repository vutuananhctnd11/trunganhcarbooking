package com.example.trunganhcar.service;

import com.example.trunganhcar.dto.DriverResponse;
import com.example.trunganhcar.entity.Driver;
import com.example.trunganhcar.exception.CustomException;
import com.example.trunganhcar.repository.DriverRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DriverServiceTest {

    @Mock
    private DriverRepository driverRepository;

    @InjectMocks
    private DriverService driverService;

    private Driver pendingDriver;

    @BeforeEach
    void setUp() {
        pendingDriver = Driver.builder()
                .id(2L)
                .fullName("Tài xế Mới")
                .phone("0981112222")
                .approvalStatus("PENDING")
                .isActive(false)
                .build();
    }

    @Test
    @DisplayName("Admin phê duyệt tài xế thành công")
    void approveDriver_success() {
        when(driverRepository.findById(2L)).thenReturn(Optional.of(pendingDriver));
        when(driverRepository.save(any(Driver.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DriverResponse response = driverService.approveDriver(2L);

        assertNotNull(response);
        assertEquals("APPROVED", response.getApprovalStatus());
        assertTrue(response.getIsActive());
        assertNotNull(response.getApprovedAt());
        verify(driverRepository, times(1)).save(pendingDriver);
    }

    @Test
    @DisplayName("Admin từ chối tài xế thành công với lý do")
    void rejectDriver_success() {
        when(driverRepository.findById(2L)).thenReturn(Optional.of(pendingDriver));
        when(driverRepository.save(any(Driver.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DriverResponse response = driverService.rejectDriver(2L, "Hồ sơ giấy tờ chưa hợp lệ");

        assertNotNull(response);
        assertEquals("REJECTED", response.getApprovalStatus());
        assertFalse(response.getIsActive());
        assertEquals("Hồ sơ giấy tờ chưa hợp lệ", response.getRejectionReason());
        verify(driverRepository, times(1)).save(pendingDriver);
    }
}

