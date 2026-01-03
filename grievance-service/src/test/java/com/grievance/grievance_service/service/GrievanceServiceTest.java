package com.grievance.grievance_service.service;

import com.grievance.grievance_service.client.AuthServiceClient;
import com.grievance.grievance_service.dto.CreateGrievanceRequest;
import com.grievance.grievance_service.dto.GrievanceCreatedResponse;
import com.grievance.grievance_service.entity.Grievance;
import com.grievance.grievance_service.enums.Priority;
import com.grievance.grievance_service.enums.Status;
import com.grievance.grievance_service.exception.ResourceNotFoundException;
import com.grievance.grievance_service.repository.GrievanceHistoryRepository;
import com.grievance.grievance_service.repository.GrievanceRepository;
import com.grievance.grievance_service.service.impl.GrievanceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GrievanceServiceTest {

    @Mock
    private GrievanceRepository grievanceRepository;

    @Mock
    private GrievanceHistoryRepository historyRepository;

    @Mock
    private GrievanceEventPublisher eventPublisher;

    @Mock
    private AuthServiceClient authServiceClient;

    @Mock
    private AutoAssignmentService autoAssignmentService;

    @InjectMocks
    private GrievanceServiceImpl grievanceService;

    private Grievance testGrievance;
    private CreateGrievanceRequest createRequest;

    @BeforeEach
    void setUp() {
        testGrievance = Grievance.builder()
                .id(1L)
                .grievanceNumber("GRV-20260103-0001")
                .citizenId(1L)
                .departmentId(1L)
                .departmentName("Water Supply Department")
                .categoryId(1L)
                .categoryName("Water Supply Issues")
                .subCategoryId(1L)
                .subCategoryName("No Water Supply")
                .title("Test Grievance")
                .description("Test Description")
                .status(Status.PENDING)
                .priority(Priority.HIGH)
                .slaHours(24)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        createRequest = new CreateGrievanceRequest();
        createRequest.setDepartmentId(1L);
        createRequest.setDepartmentName("Water Supply Department");
        createRequest.setCategoryId(1L);
        createRequest.setCategoryName("Water Supply Issues");
        createRequest.setSubCategoryId(1L);
        createRequest.setSubCategoryName("No Water Supply");
        createRequest.setSlaHours(24);
        createRequest.setTitle("Test Grievance");
        createRequest.setDescription("Test Description");
    }

    @Test
    void getGrievanceById_Success() {
        when(grievanceRepository.findById(1L)).thenReturn(Optional.of(testGrievance));

        Grievance result = grievanceService.getGrievanceById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("GRV-20260103-0001", result.getGrievanceNumber());
    }

    @Test
    void getGrievanceById_NotFound_ThrowsException() {
        when(grievanceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> grievanceService.getGrievanceById(99L));
    }

    @Test
    void getGrievancesByCitizenId_Success() {
        when(grievanceRepository.findByCitizenId(1L)).thenReturn(Arrays.asList(testGrievance));

        List<Grievance> results = grievanceService.getGrievancesByCitizenId(1L);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(1L, results.get(0).getCitizenId());
    }

    @Test
    void getGrievancesByStatus_Success() {
        when(grievanceRepository.findByStatus(Status.PENDING)).thenReturn(Arrays.asList(testGrievance));

        List<Grievance> results = grievanceService.getGrievancesByStatus("PENDING");

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(Status.PENDING, results.get(0).getStatus());
    }

    @Test
    void createGrievance_Success() {
        when(grievanceRepository.count()).thenReturn(0L);
        when(grievanceRepository.save(any(Grievance.class))).thenReturn(testGrievance);
        when(historyRepository.save(any())).thenReturn(null);
        when(autoAssignmentService.assignOfficer(any(Grievance.class))).thenReturn(5L);
        when(authServiceClient.getUserById(anyLong())).thenReturn(Map.of("email", "test@test.com"));
        doNothing().when(eventPublisher).publishGrievanceCreated(any());
        doNothing().when(eventPublisher).publishStatusChanged(any());

        GrievanceCreatedResponse response = grievanceService.createGrievance(createRequest, 1L);

        assertNotNull(response);
        verify(grievanceRepository, times(2)).save(any(Grievance.class));
    }

    @Test
    void getAllGrievances_Success() {
        when(grievanceRepository.findAll()).thenReturn(Arrays.asList(testGrievance));

        List<Grievance> results = grievanceService.getAllGrievances();

        assertNotNull(results);
        assertEquals(1, results.size());
    }
}