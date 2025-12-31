package com.grievance.grievance_service.service;

import com.grievance.grievance_service.dto.CreateGrievanceRequest;
import com.grievance.grievance_service.dto.GrievanceResponse;

public interface GrievanceService {
    GrievanceResponse createGrievance(CreateGrievanceRequest request, Long citizenId);
}