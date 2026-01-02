package com.grievance.grievance_service.service.impl;

import com.grievance.grievance_service.client.AuthServiceClient;
import com.grievance.grievance_service.dto.*;
import com.grievance.grievance_service.entity.Grievance;
import com.grievance.grievance_service.entity.GrievanceHistory;
import com.grievance.grievance_service.enums.Status;
import com.grievance.grievance_service.repository.GrievanceHistoryRepository;
import com.grievance.grievance_service.repository.GrievanceRepository;
import com.grievance.grievance_service.service.GrievanceEventPublisher;
import com.grievance.grievance_service.service.GrievanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Override
    @Transactional
    public GrievanceResponse createGrievance(CreateGrievanceRequest request, Long citizenId) {
        Grievance grievance = Grievance.builder()
                .grievanceNumber(generateGrievanceNumber())
                .citizenId(citizenId)
                .title(request.getTitle())
                .description(request.getDescription())
                .department(request.getDepartment())
                .category(request.getCategory())
                .priority(request.getPriority())
                .status(Status.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        grievanceRepository.save(grievance);

        saveHistory(grievance, null, Status.PENDING, citizenId, "Grievance submitted");

        // Publish event to RabbitMQ
        publishGrievanceCreatedEvent(grievance, citizenId);

        return GrievanceResponse.builder()
                .grievanceId(grievance.getId())
                .message("Grievance submitted successfully")
                .grievanceNumber(grievance.getGrievanceNumber())
                .citizenId(grievance.getCitizenId())
                .title(grievance.getTitle())
                .description(grievance.getDescription())
                .category(grievance.getCategory())
                .department(grievance.getDepartment())
                .priority(grievance.getPriority().name())
                .status(grievance.getStatus().name())
                .createdAt(grievance.getCreatedAt())
                .updatedAt(grievance.getUpdatedAt())
                .build();
    }

    @Override
    public Grievance getGrievanceById(Long id) {
        return grievanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Grievance not found with id: " + id));
    }

    @Override
    public Grievance getByGrievanceNumber(String grievanceNumber) {
        return grievanceRepository.findByGrievanceNumber(grievanceNumber)
                .orElseThrow(() -> new RuntimeException("Grievance not found: " + grievanceNumber));
    }

    @Override
    public List<Grievance> getGrievancesByCitizenId(Long citizenId) {
        return grievanceRepository.findByCitizenId(citizenId);
    }

    @Override
    public List<Grievance> getGrievancesByDepartment(String department) {
        return grievanceRepository.findByDepartment(department);
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
    @Transactional
    public Grievance updateStatus(Long id, UpdateStatusRequest request, Long officerId) {
        Grievance grievance = getGrievanceById(id);
        Status oldStatus = grievance.getStatus();
        Status newStatus = Status.valueOf(request.getStatus().toUpperCase());

        grievance.setStatus(newStatus);
        grievance.setUpdatedAt(LocalDateTime.now());

        if (newStatus == Status.RESOLVED || newStatus == Status.CLOSED) {
            grievance.setResolvedAt(LocalDateTime.now());
        }

        grievanceRepository.save(grievance);

        saveHistory(grievance, oldStatus, newStatus, officerId, request.getRemarks());

        // Publish event to RabbitMQ
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
        grievance.setUpdatedAt(LocalDateTime.now());

        grievanceRepository.save(grievance);

        saveHistory(grievance, oldStatus, Status.ASSIGNED, request.getOfficerId(), "Assigned to officer");

        // Publish event to RabbitMQ
        publishStatusChangedEvent(grievance, oldStatus, Status.ASSIGNED, "Officer assigned to grievance");

        return grievance;
    }

    @Override
    public List<Grievance> getGrievancesByStatus(String status) {
        Status statusEnum = Status.valueOf(status.toUpperCase());
        return grievanceRepository.findByStatus(statusEnum);
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
                    .department(grievance.getDepartment())
                    .category(grievance.getCategory())
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
                    .department(grievance.getDepartment())
                    .category(grievance.getCategory())
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
}