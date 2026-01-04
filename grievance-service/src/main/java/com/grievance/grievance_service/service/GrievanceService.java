package com.grievance.grievance_service.service;

import com.grievance.grievance_service.dto.*;
import com.grievance.grievance_service.entity.Grievance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface GrievanceService {

    GrievanceCreatedResponse createGrievance(CreateGrievanceRequest request, Long citizenId);

    Grievance getGrievanceById(Long id);

    Grievance getByGrievanceNumber(String grievanceNumber);

    List<Grievance> getGrievancesByCitizenId(Long citizenId);

    List<Grievance> getGrievancesByDepartment(String department);

    List<Grievance> getGrievancesByOfficerId(Long officerId);

    List<Grievance> getAllGrievances();

    List<Grievance> getGrievancesByStatus(String status);

    Grievance updateStatus(Long id, UpdateStatusRequest request, Long officerId);

    Grievance assignOfficer(Long id, AssignOfficerRequest request);

    Grievance escalateGrievance(Long id, String reason);

    Grievance reassignOfficer(Long id, AssignOfficerRequest request);

    // Paginated methods
    Page<Grievance> getAllGrievances(Pageable pageable);

    Page<Grievance> getGrievancesByCitizenId(Long citizenId, Pageable pageable);

    Page<Grievance> getGrievancesByOfficerId(Long officerId, Pageable pageable);

    Page<Grievance> getGrievancesByDepartment(String department, Pageable pageable);

    Page<Grievance> getGrievancesByStatus(String status, Pageable pageable);
}