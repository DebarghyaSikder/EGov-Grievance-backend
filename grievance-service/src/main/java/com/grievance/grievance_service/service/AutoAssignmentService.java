package com.grievance.grievance_service.service;

import com.grievance.grievance_service.entity.Grievance;
import com.grievance.grievance_service.entity.OfficerDepartment;
import com.grievance.grievance_service.repository.OfficerDepartmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AutoAssignmentService {

    private final OfficerDepartmentRepository officerDepartmentRepository;

    @Transactional
    public Long assignOfficer(Grievance grievance) {
        Long departmentId = grievance.getDepartmentId();

        if (departmentId == null) {
            log.warn("No department set for grievance: {}", grievance.getGrievanceNumber());
            return null;
        }

        List<OfficerDepartment> officers = officerDepartmentRepository
                .findByDepartmentIdOrderByLoadAsc(departmentId);

        if (officers.isEmpty()) {
            log.warn("No officers available for department ID: {}", departmentId);
            return null;
        }

        OfficerDepartment selectedOfficer = officers.get(0);

        selectedOfficer.setCurrentLoad(selectedOfficer.getCurrentLoad() + 1);
        officerDepartmentRepository.save(selectedOfficer);

        log.info("Auto-assigned grievance {} to officer {} (load: {})",
                grievance.getGrievanceNumber(),
                selectedOfficer.getOfficerId(),
                selectedOfficer.getCurrentLoad());

        return selectedOfficer.getOfficerId();
    }

    @Transactional
    public void decrementOfficerLoad(Long officerId) {
        OfficerDepartment officer = officerDepartmentRepository.findByOfficerId(officerId).orElse(null);

        if (officer != null && officer.getCurrentLoad() > 0) {
            officer.setCurrentLoad(officer.getCurrentLoad() - 1);
            officerDepartmentRepository.save(officer);
        }
    }
}