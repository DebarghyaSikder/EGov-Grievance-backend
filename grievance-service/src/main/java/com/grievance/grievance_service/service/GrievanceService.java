package com.grievance.grievance_service.service;

import com.grievance.grievance_service.dto.*;
import com.grievance.grievance_service.entity.Grievance;

import java.util.List;

public interface GrievanceService {

    GrievanceResponse createGrievance(CreateGrievanceRequest request, Long citizenId);

    Grievance getGrievanceById(Long id);

    Grievance getByGrievanceNumber(String grievanceNumber);

    List<Grievance> getGrievancesByCitizenId(Long citizenId);

    List<Grievance> getGrievancesByDepartment(String department);

    List<Grievance> getGrievancesByOfficerId(Long officerId);

    List<Grievance> getAllGrievances();

    Grievance updateStatus(Long id, UpdateStatusRequest request, Long officerId);

    Grievance assignOfficer(Long id, AssignOfficerRequest request);

    List<Grievance> getGrievancesByStatus(String status);

    Grievance escalateGrievance(Long id, String reason);

    Grievance reassignOfficer(Long id, AssignOfficerRequest request);
}