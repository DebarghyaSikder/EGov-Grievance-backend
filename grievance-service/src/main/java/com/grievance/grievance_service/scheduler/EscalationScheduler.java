package com.grievance.grievance_service.scheduler;

import com.grievance.grievance_service.dto.GrievanceEvent;
import com.grievance.grievance_service.entity.Grievance;
import com.grievance.grievance_service.entity.GrievanceHistory;
import com.grievance.grievance_service.enums.Status;
import com.grievance.grievance_service.repository.GrievanceHistoryRepository;
import com.grievance.grievance_service.repository.GrievanceRepository;
import com.grievance.grievance_service.service.GrievanceEventPublisher;
import com.grievance.grievance_service.client.AuthServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class EscalationScheduler {

    private final GrievanceRepository grievanceRepository;
    private final GrievanceHistoryRepository historyRepository;
    private final GrievanceEventPublisher eventPublisher;
    private final AuthServiceClient authServiceClient;
    @Scheduled(fixedRate = 300000)
    @Transactional
    public void checkAndEscalateGrievances() {
        log.info("-------------AUTO-ESCALATION CHECK STARTED ----------------");
        LocalDateTime now = LocalDateTime.now();

        List<Status> statusesToCheck = Arrays.asList(Status.ASSIGNED, Status.IN_PROGRESS);

        List<Grievance> grievancesToEscalate = grievanceRepository
                .findByStatusInAndSlaDeadlineBefore(statusesToCheck, now);

        log.info("Found {} grievances past SLA deadline", grievancesToEscalate.size());

        for (Grievance grievance : grievancesToEscalate) {
            try {
                escalateGrievance(grievance);
            } catch (Exception e) {
                log.error("Failed to escalate grievance {}: {}", grievance.getGrievanceNumber(), e.getMessage());
            }
        }

        log.info("-------------AUTO-ESCALATION CHECK COMPLETED--------------");
    }

    private void escalateGrievance(Grievance grievance) {
        Status oldStatus = grievance.getStatus();
        grievance.setStatus(Status.ESCALATED);
        grievance.setUpdatedAt(LocalDateTime.now());
        grievanceRepository.save(grievance);
        GrievanceHistory history = GrievanceHistory.builder()
                .grievanceId(grievance.getId())
                .oldStatus(oldStatus)
                .newStatus(Status.ESCALATED)
                .changedBy(null)
                .remarks("AUTO-ESCALATED: SLA breach - No resolution within " + grievance.getSlaHours() + " hours")
                .changedAt(LocalDateTime.now())
                .build();

        historyRepository.save(history);

        publishEscalationEvent(grievance, oldStatus);

        log.info("AUTO-ESCALATED: {} | Was: {} | SLA: {} hours | Deadline was: {}",
                grievance.getGrievanceNumber(),
                oldStatus,
                grievance.getSlaHours(),
                grievance.getSlaDeadline());
    }

    private void publishEscalationEvent(Grievance grievance, Status oldStatus) {
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
                    .oldStatus(oldStatus.name())
                    .newStatus(Status.ESCALATED.name())
                    .remarks("AUTO-ESCALATED: SLA breach - No resolution within deadline")
                    .build();
            eventPublisher.publishStatusChanged(event);
        } catch (Exception e) {
            log.error("Failed to publish escalation event: {}", e.getMessage());
        }
    }

    private String getCitizenEmail(Long citizenId) {
        try {
            Map<String, Object> user = authServiceClient.getUserById(citizenId);
            return (String) user.get("email");
        } catch (Exception e) {
            return null;
        }
    }
}