package com.grievance.grievance_service.service.impl;

import com.grievance.grievance_service.client.AuthServiceClient;
import com.grievance.grievance_service.dto.*;
import com.grievance.grievance_service.entity.*;
import com.grievance.grievance_service.enums.Priority;
import com.grievance.grievance_service.enums.Status;
import com.grievance.grievance_service.exception.ResourceNotFoundException;
import com.grievance.grievance_service.repository.*;
import com.grievance.grievance_service.service.AutoAssignmentService;
import com.grievance.grievance_service.service.GrievanceEventPublisher;
import com.grievance.grievance_service.service.GrievanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GrievanceServiceImpl implements GrievanceService {

    private final GrievanceRepository grievanceRepository;
    private final GrievanceHistoryRepository historyRepository;
    private final GrievanceEventPublisher eventPublisher;
    private final AuthServiceClient authServiceClient;
    private final AutoAssignmentService autoAssignmentService;

    @Override
    @Transactional
    public GrievanceCreatedResponse createGrievance(CreateGrievanceRequest request, Long citizenId) {
        Integer slaHours = request.getSlaHours() != null ? request.getSlaHours() : 48;

        Priority priority = request.getPriority();
        if (priority == null) {
            priority = determinePriority(slaHours);
        }

        Grievance grievance = Grievance.builder()
                .grievanceNumber(generateGrievanceNumber())
                .citizenId(citizenId)
                .departmentId(request.getDepartmentId())
                .departmentName(request.getDepartmentName())
                .categoryId(request.getCategoryId())
                .categoryName(request.getCategoryName())
                .subCategoryId(request.getSubCategoryId())
                .subCategoryName(request.getSubCategoryName())
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(priority)
                .status(Status.PENDING)
                .slaHours(slaHours)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        grievanceRepository.save(grievance);

        saveHistory(grievance, null, Status.PENDING, citizenId, "Grievance submitted");

        // Auto-assign officer
        Long assignedOfficerId = autoAssignmentService.assignOfficer(grievance);

        if (assignedOfficerId != null) {
            grievance.setAssignedOfficerId(assignedOfficerId);
            grievance.setStatus(Status.ASSIGNED);
            grievance.setAssignedAt(LocalDateTime.now());
            grievance.setSlaDeadline(LocalDateTime.now().plusHours(slaHours));
            grievance.setUpdatedAt(LocalDateTime.now());

            grievanceRepository.save(grievance);

            saveHistory(grievance, Status.PENDING, Status.ASSIGNED, assignedOfficerId, "Auto-assigned to officer");

            publishStatusChangedEvent(grievance, Status.PENDING, Status.ASSIGNED, "Auto-assigned to officer");
        }

        publishGrievanceCreatedEvent(grievance, citizenId);

        return GrievanceCreatedResponse.builder()
                .grievanceId(grievance.getId())
                .grievanceNumber(grievance.getGrievanceNumber())
                .status(grievance.getStatus().name())
                .assignedOfficerId(grievance.getAssignedOfficerId())
                .build();
    }

    @Override
    public Grievance getGrievanceById(Long id) {
        return grievanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grievance", id));
    }

    @Override
    public Grievance getByGrievanceNumber(String grievanceNumber) {
        return grievanceRepository.findByGrievanceNumber(grievanceNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Grievance", "grievanceNumber", grievanceNumber));
    }

    @Override
    public List<Grievance> getGrievancesByCitizenId(Long citizenId) {
        return grievanceRepository.findByCitizenId(citizenId);
    }

    @Override
    public List<Grievance> getGrievancesByDepartment(String department) {
        return grievanceRepository.findByDepartmentName(department);
    }

    @Override
    public List<Grievance> getGrievancesByOfficerId(Long officerId) {
        return grievanceRepository.findByAssignedOfficerId(officerId);
    }

    @Override
    public List<Grievance> getAllGrievances() {
        return grievanceRepository.findAll();
    }

    @Override
    public Page<Grievance> getAllGrievances(Pageable pageable) {
        return grievanceRepository.findAll(pageable);
    }

    @Override
    public Page<Grievance> getGrievancesByCitizenId(Long citizenId, Pageable pageable) {
        return grievanceRepository.findByCitizenId(citizenId, pageable);
    }

    @Override
    public Page<Grievance> getGrievancesByOfficerId(Long officerId, Pageable pageable) {
        return grievanceRepository.findByAssignedOfficerId(officerId, pageable);
    }

    @Override
    public Page<Grievance> getGrievancesByDepartment(String department, Pageable pageable) {
        return grievanceRepository.findByDepartmentName(department, pageable);
    }

    @Override
    public Page<Grievance> getGrievancesByStatus(String status, Pageable pageable) {
        Status statusEnum = Status.valueOf(status.toUpperCase());
        return grievanceRepository.findByStatus(statusEnum, pageable);
    }
    
    @Override
    @Transactional
    public Grievance updateStatus(Long id, UpdateStatusRequest request, Long officerId) {
        Grievance grievance = getGrievanceById(id);
        Status oldStatus = grievance.getStatus();
        Status newStatus = Status.valueOf(request.getStatus().toUpperCase());

        grievance.setStatus(newStatus);
        grievance.setUpdatedAt(LocalDateTime.now());

        if (newStatus == Status.RESOLVED || newStatus == Status.CLOSED) {
            grievance.setResolvedAt(LocalDateTime.now());

            // Decrement officer load when grievance is resolved/closed
            if (grievance.getAssignedOfficerId() != null) {
                autoAssignmentService.decrementOfficerLoad(grievance.getAssignedOfficerId());
            }
        }

        grievanceRepository.save(grievance);

        saveHistory(grievance, oldStatus, newStatus, officerId, request.getRemarks());

        publishStatusChangedEvent(grievance, oldStatus, newStatus, request.getRemarks());

        return grievance;
    }

    @Override
    @Transactional
    public Grievance assignOfficer(Long id, AssignOfficerRequest request) {
        Grievance grievance = getGrievanceById(id);
        Status oldStatus = grievance.getStatus();

        grievance.setAssignedOfficerId(request.getOfficerId());
        grievance.setStatus(Status.ASSIGNED);
        grievance.setAssignedAt(LocalDateTime.now());
        grievance.setSlaDeadline(LocalDateTime.now().plusHours(grievance.getSlaHours() != null ? grievance.getSlaHours() : 48));
        grievance.setUpdatedAt(LocalDateTime.now());

        grievanceRepository.save(grievance);

        saveHistory(grievance, oldStatus, Status.ASSIGNED, request.getOfficerId(), "Manually assigned to officer");

        publishStatusChangedEvent(grievance, oldStatus, Status.ASSIGNED, "Manually assigned to officer");

        return grievance;
    }

    @Override
    public List<Grievance> getGrievancesByStatus(String status) {
        Status statusEnum = Status.valueOf(status.toUpperCase());
        return grievanceRepository.findByStatus(statusEnum);
    }

    @Override
    @Transactional
    public Grievance escalateGrievance(Long id, String reason) {
        Grievance grievance = getGrievanceById(id);
        Status oldStatus = grievance.getStatus();

        grievance.setStatus(Status.ESCALATED);
        grievance.setUpdatedAt(LocalDateTime.now());

        grievanceRepository.save(grievance);

        saveHistory(grievance, oldStatus, Status.ESCALATED, null, "Escalated: " + reason);

        publishStatusChangedEvent(grievance, oldStatus, Status.ESCALATED, "Escalated: " + reason);

        return grievance;
    }

    @Override
    @Transactional
    public Grievance reassignOfficer(Long id, AssignOfficerRequest request) {
        Grievance grievance = getGrievanceById(id);
        Status oldStatus = grievance.getStatus();
        Long previousOfficerId = grievance.getAssignedOfficerId();

        // Decrement previous officer's load
        if (previousOfficerId != null) {
            autoAssignmentService.decrementOfficerLoad(previousOfficerId);
        }

        // Increment new officer's load
        autoAssignmentService.incrementOfficerLoad(request.getOfficerId());

        grievance.setAssignedOfficerId(request.getOfficerId());
        grievance.setStatus(Status.ASSIGNED);
        grievance.setAssignedAt(LocalDateTime.now());
        grievance.setSlaDeadline(LocalDateTime.now().plusHours(grievance.getSlaHours() != null ? grievance.getSlaHours() : 48));
        grievance.setUpdatedAt(LocalDateTime.now());

        grievanceRepository.save(grievance);

        String remarks = String.format("Reassigned from officer %d to officer %d",
                previousOfficerId != null ? previousOfficerId : 0, request.getOfficerId());
        saveHistory(grievance, oldStatus, Status.ASSIGNED, request.getOfficerId(), remarks);

        publishStatusChangedEvent(grievance, oldStatus, Status.ASSIGNED, remarks);

        return grievance;
    }

    private Priority determinePriority(Integer slaHours) {
        if (slaHours <= 12) return Priority.HIGH;
        if (slaHours <= 48) return Priority.MEDIUM;
        return Priority.LOW;
    }

    private String generateGrievanceNumber() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = grievanceRepository.count() + 1;
        return String.format("GRV-%s-%04d", datePart, count);
    }

    private void saveHistory(Grievance grievance, Status oldStatus, Status newStatus,
                             Long changedBy, String remarks) {
        GrievanceHistory history = GrievanceHistory.builder()
                .grievanceId(grievance.getId())
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .changedBy(changedBy)
                .remarks(remarks)
                .changedAt(LocalDateTime.now())
                .build();

        historyRepository.save(history);
    }

    private void publishGrievanceCreatedEvent(Grievance grievance, Long citizenId) {
        try {
            String citizenEmail = getCitizenEmail(citizenId);

            GrievanceEvent event = GrievanceEvent.builder()
                    .grievanceId(grievance.getId())
                    .grievanceNumber(grievance.getGrievanceNumber())
                    .citizenId(citizenId)
                    .citizenEmail(citizenEmail)
                    .title(grievance.getTitle())
                    .department(grievance.getDepartmentName())
                    .category(grievance.getCategoryName())
                    .newStatus(grievance.getStatus().name())
                    .build();

            eventPublisher.publishGrievanceCreated(event);
        } catch (Exception e) {
            log.error("Failed to publish grievance created event: {}", e.getMessage());
        }
    }

    private void publishStatusChangedEvent(Grievance grievance, Status oldStatus, Status newStatus, String remarks) {
        try {
            String citizenEmail = getCitizenEmail(grievance.getCitizenId());

            GrievanceEvent event = GrievanceEvent.builder()
                    .grievanceId(grievance.getId())
                    .grievanceNumber(grievance.getGrievanceNumber())
                    .citizenId(grievance.getCitizenId())
                    .citizenEmail(citizenEmail)
                    .title(grievance.getTitle())
                    .department(grievance.getDepartmentName())
                    .category(grievance.getCategoryName())
                    .oldStatus(oldStatus != null ? oldStatus.name() : null)
                    .newStatus(newStatus.name())
                    .remarks(remarks)
                    .build();

            eventPublisher.publishStatusChanged(event);
        } catch (Exception e) {
            log.error("Failed to publish status changed event: {}", e.getMessage());
        }
    }

    private String getCitizenEmail(Long citizenId) {
        try {
            Map<String, Object> user = authServiceClient.getUserById(citizenId);
            return (String) user.get("email");
        } catch (Exception e) {
            log.error("Failed to get citizen email: {}", e.getMessage());
            return null;
        }
    }

    private GrievanceResponse mapToResponse(Grievance grievance, String message) {
        return GrievanceResponse.builder()
                .grievanceId(grievance.getId())
                .message(message)
                .grievanceNumber(grievance.getGrievanceNumber())
                .citizenId(grievance.getCitizenId())
                .title(grievance.getTitle())
                .description(grievance.getDescription())
                .departmentId(grievance.getDepartmentId())
                .departmentName(grievance.getDepartmentName())
                .categoryId(grievance.getCategoryId())
                .categoryName(grievance.getCategoryName())
                .subCategoryId(grievance.getSubCategoryId())
                .subCategoryName(grievance.getSubCategoryName())
                .priority(grievance.getPriority() != null ? grievance.getPriority().name() : null)
                .status(grievance.getStatus() != null ? grievance.getStatus().name() : null)
                .assignedOfficerId(grievance.getAssignedOfficerId())
                .slaHours(grievance.getSlaHours())
                .slaDeadline(grievance.getSlaDeadline())
                .createdAt(grievance.getCreatedAt())
                .updatedAt(grievance.getUpdatedAt())
                .assignedAt(grievance.getAssignedAt())
                .build();
    }
}