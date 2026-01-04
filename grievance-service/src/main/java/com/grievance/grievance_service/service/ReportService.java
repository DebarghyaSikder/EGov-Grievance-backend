package com.grievance.grievance_service.service;

import java.util.Map;

public interface ReportService {

    Map<String, Long> getGrievanceCountByStatus();

    Map<String, Long> getGrievanceCountByDepartment();

    Map<String, Long> getGrievanceCountByCategory();

    Map<String, Long> getPendingVsResolved();

    Map<String, Object> getAverageResolutionTime();

    Map<String, Map<String, Object>> getDepartmentPerformance();

    Map<String, Long> getMonthlyTrends();

    Map<Long, Map<String, Object>> getOfficerWorkload();

    Map<String, Object> getDashboardSummary();
}