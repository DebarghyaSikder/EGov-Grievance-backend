package com.grievance.grievance_service.service;

import com.grievance.grievance_service.entity.Grievance;
import com.grievance.grievance_service.entity.OfficerDepartment;
import com.grievance.grievance_service.repository.OfficerDepartmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutoAssignmentServiceTest {

    @Mock
    private OfficerDepartmentRepository officerDepartmentRepository;

    @InjectMocks
    private AutoAssignmentService autoAssignmentService;

    private Grievance testGrievance;
    private OfficerDepartment testOfficerDepartment;

    @BeforeEach
    void setUp() {
        testGrievance = Grievance.builder()
                .id(1L)
                .grievanceNumber("GRV-20260103-0001")
                .departmentId(1L)
                .build();

        testOfficerDepartment = OfficerDepartment.builder()
                .id(1L)
                .officerId(5L)
                .departmentId(1L)
                .isActive(true)
                .currentLoad(0)
                .build();
    }

    @Test
    void assignOfficer_Success() {
        when(officerDepartmentRepository.findByDepartmentIdOrderByLoadAsc(1L))
                .thenReturn(Arrays.asList(testOfficerDepartment));
        when(officerDepartmentRepository.save(any(OfficerDepartment.class)))
                .thenReturn(testOfficerDepartment);

        Long assignedOfficerId = autoAssignmentService.assignOfficer(testGrievance);

        assertNotNull(assignedOfficerId);
        assertEquals(5L, assignedOfficerId);
        verify(officerDepartmentRepository, times(1)).save(any(OfficerDepartment.class));
    }

    @Test
    void assignOfficer_NoOfficersAvailable_ReturnsNull() {
        when(officerDepartmentRepository.findByDepartmentIdOrderByLoadAsc(1L))
                .thenReturn(Collections.emptyList());

        Long assignedOfficerId = autoAssignmentService.assignOfficer(testGrievance);

        assertNull(assignedOfficerId);
    }

    @Test
    void assignOfficer_NoDepartmentSet_ReturnsNull() {
        testGrievance.setDepartmentId(null);

        Long assignedOfficerId = autoAssignmentService.assignOfficer(testGrievance);

        assertNull(assignedOfficerId);
    }

    @Test
    void decrementOfficerLoad_Success() {
        testOfficerDepartment.setCurrentLoad(2);
        when(officerDepartmentRepository.findByOfficerId(5L))
                .thenReturn(Optional.of(testOfficerDepartment));
        when(officerDepartmentRepository.save(any(OfficerDepartment.class)))
                .thenReturn(testOfficerDepartment);

        autoAssignmentService.decrementOfficerLoad(5L);

        assertEquals(1, testOfficerDepartment.getCurrentLoad());
        verify(officerDepartmentRepository, times(1)).save(any(OfficerDepartment.class));
    }

    @Test
    void decrementOfficerLoad_LoadAlreadyZero_NoChange() {
        testOfficerDepartment.setCurrentLoad(0);
        when(officerDepartmentRepository.findByOfficerId(5L))
                .thenReturn(Optional.of(testOfficerDepartment));

        autoAssignmentService.decrementOfficerLoad(5L);

        assertEquals(0, testOfficerDepartment.getCurrentLoad());
        verify(officerDepartmentRepository, never()).save(any(OfficerDepartment.class));
    }

    @Test
    void incrementOfficerLoad_Success() {
        when(officerDepartmentRepository.findByOfficerId(5L))
                .thenReturn(Optional.of(testOfficerDepartment));
        when(officerDepartmentRepository.save(any(OfficerDepartment.class)))
                .thenReturn(testOfficerDepartment);

        autoAssignmentService.incrementOfficerLoad(5L);

        assertEquals(1, testOfficerDepartment.getCurrentLoad());
        verify(officerDepartmentRepository, times(1)).save(any(OfficerDepartment.class));
    }
}