package com.grievance.grievance_service.scheduler;

import com.grievance.grievance_service.entity.Grievance;
import com.grievance.grievance_service.entity.GrievanceHistory;
import com.grievance.grievance_service.enums.Status;
import com.grievance.grievance_service.repository.GrievanceHistoryRepository;
import com.grievance.grievance_service.repository.GrievanceRepository;
import com.grievance.grievance_service.service.GrievanceEventPublisher;
import com.grievance.grievance_service.dto.GrievanceEvent;
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

    // Run every hour
    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void checkAndEscalateGrievances() {
        log.info("Running escalation check...");

        LocalDateTime now = LocalDateTime.now();

        // Find grievances that are ASSIGNED or IN_PROGRESS and past SLA deadline
        List<Status> statusesToCheck = Arrays.asList(Status.ASSIGNED, Status.IN_PROGRESS);

        List<Grievance> grievances = grievanceRepository.findByStatusInAndSlaDeadlineBefore(statusesToCheck, now);

        for (Grievance grievance : grievances) {
            escalateGrievance(grievance);
        }

        log.info("Escalation check completed. Escalated {} grievances.", grievances.size());
    }

    private void escalateGrievance(Grievance grievance) {
        Status oldStatus = grievance.getStatus();

        grievance.setStatus(Status.ESCALATED);
        grievance.setUpdatedAt(LocalDateTime.now());

        grievanceRepository.save(grievance);

        // Save history
        GrievanceHistory history = GrievanceHistory.builder()
                .grievanceId(grievance.getId())
                .oldStatus(oldStatus)
                .newStatus(Status.ESCALATED)
                .changedBy(null)
                .remarks("Auto-escalated due to SLA breach")
                .changedAt(LocalDateTime.now())
                .build();

        historyRepository.save(history);

        // Publish event
        try {
            String citizenEmail = getCitizenEmail(grievance.getCitizenId());

            GrievanceEvent event = GrievanceEvent.builder()
                    .grievanceId(grievance.getId())
                    .grievanceNumber(grievance.getGrievanceNumber())
                    .citizenId(grievance.getCitizenId())
                    .citizenEmail(citizenEmail)
                    .title(grievance.getTitle())
                    .oldStatus(oldStatus.name())
                    .newStatus(Status.ESCALATED.name())
                    .remarks("Auto-escalated due to SLA breach")
                    .build();

            eventPublisher.publishStatusChanged(event);
        } catch (Exception e) {
            log.error("Failed to publish escalation event: {}", e.getMessage());
        }

        log.info("Auto-escalated grievance: {}", grievance.getGrievanceNumber());
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