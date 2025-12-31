package com.grievance.grievance_service.service.impl;

import com.grievance.grievance_service.dto.CreateGrievanceRequest;
import com.grievance.grievance_service.dto.GrievanceResponse;
import com.grievance.grievance_service.entity.Grievance;
import com.grievance.grievance_service.enums.Priority;
import com.grievance.grievance_service.enums.Status;
import com.grievance.grievance_service.repository.GrievanceRepository;
import com.grievance.grievance_service.service.GrievanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GrievanceServiceImpl implements GrievanceService {

    private final GrievanceRepository grievanceRepository;

    private String generateGrievanceNumber() {
        String yearMonth = java.time.LocalDate.now().toString().substring(0, 7).replace("-", "");
        long count = grievanceRepository.count() + 1;
        return String.format("GRV-%s-%04d", yearMonth, count);
    }

    @Override
    public GrievanceResponse createGrievance(CreateGrievanceRequest request, Long citizenId) {

        Priority priority = request.getPriority() != null ? request.getPriority() : Priority.MEDIUM;

        Grievance grievance = Grievance.builder()
                .grievanceNumber(generateGrievanceNumber())
                .citizenId(citizenId)
                .department(request.getDepartment())
                .category(request.getCategory())
                .priority(priority)
                .title(request.getTitle())
                .description(request.getDescription())
                .status(Status.PENDING)
                .build();

        grievanceRepository.save(grievance);

        return GrievanceResponse.builder()
                .grievanceId(grievance.getId())
                .citizenId(grievance.getCitizenId())
                .department(grievance.getDepartment())
                .category(grievance.getCategory())
                .priority(grievance.getPriority().name())
                .title(grievance.getTitle())
                .description(grievance.getDescription())
                .status(grievance.getStatus().name())
                .createdAt(grievance.getCreatedAt())
                .updatedAt(grievance.getUpdatedAt())
                .build();
    }
}