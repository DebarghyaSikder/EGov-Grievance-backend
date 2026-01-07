package com.grievance.grievance_service.service.impl;

import com.grievance.grievance_service.entity.Grievance;
import com.grievance.grievance_service.enums.Status;
import com.grievance.grievance_service.repository.GrievanceRepository;
import com.grievance.grievance_service.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final GrievanceRepository grievanceRepository;

    @Override
    public Map<String, Long> getGrievanceCountByStatus() {
        List<Grievance> grievances = grievanceRepository.findAll();

        return grievances.stream()
                .collect(Collectors.groupingBy(
                        g -> g.getStatus().name(),
                        Collectors.counting()
                ));
    }
    @Override
    public Map<String, Long> getGrievanceCountByDepartment() {
        List<Grievance> grievances = grievanceRepository.findAll();

        return grievances.stream()
                .filter(g -> g.getDepartmentName() != null)
                .collect(Collectors.groupingBy(
                        Grievance::getDepartmentName,
                        Collectors.counting()
                ));
    }

    @Override
    public Map<String, Long> getGrievanceCountByCategory() {
        List<Grievance> grievances = grievanceRepository.findAll();

        return grievances.stream()
                .filter(g -> g.getCategoryName() != null)
                .collect(Collectors.groupingBy(
                        Grievance::getCategoryName,
                        Collectors.counting()
                ));
    }

    @Override
    public Map<String, Long> getPendingVsResolved() {
        List<Grievance> grievances = grievanceRepository.findAll();

        long pending = grievances.stream()
                .filter(g -> g.getStatus() == Status.PENDING ||
                        g.getStatus() == Status.ASSIGNED ||
                        g.getStatus() == Status.IN_PROGRESS ||
                        g.getStatus() == Status.ESCALATED)
                .count();

        long resolved = grievances.stream()
                .filter(g -> g.getStatus() == Status.RESOLVED ||
                        g.getStatus() == Status.CLOSED)
                .count();

        Map<String, Long> result = new LinkedHashMap<>();
        result.put("pending", pending);
        result.put("resolved", resolved);
        result.put("total", (long) grievances.size());

        return result;
    }
    @Override
    public Map<String, Object> getAverageResolutionTime() {
        List<Grievance> resolvedGrievances = grievanceRepository.findAll().stream()
                .filter(g -> g.getResolvedAt() != null && g.getCreatedAt() != null)
                .collect(Collectors.toList());

        if (resolvedGrievances.isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("averageHours", 0.0);
            result.put("averageDays", 0.0);
            result.put("totalResolved", 0);
            return result;
        }

        double totalHours = resolvedGrievances.stream()
                .mapToDouble(g -> {
                    Duration duration = Duration.between(g.getCreatedAt(), g.getResolvedAt());
                    return duration.toHours();
                })
                .sum();

        double averageHours = totalHours / resolvedGrievances.size();
        double averageDays = averageHours / 24;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("averageHours", Math.round(averageHours * 100.0) / 100.0);
        result.put("averageDays", Math.round(averageDays * 100.0) / 100.0);
        result.put("totalResolved", resolvedGrievances.size());

        return result;
    }
    @Override
    public Map<String, Map<String, Object>> getDepartmentPerformance() {
        List<Grievance> grievances = grievanceRepository.findAll();

        Map<String, List<Grievance>> byDepartment = grievances.stream()
                .filter(g -> g.getDepartmentName() != null)
                .collect(Collectors.groupingBy(Grievance::getDepartmentName));

        Map<String, Map<String, Object>> result = new LinkedHashMap<>();

        byDepartment.forEach((department, deptGrievances) -> {
            Map<String, Object> stats = new LinkedHashMap<>();

            long total = deptGrievances.size();
            long resolved = deptGrievances.stream()
                    .filter(g -> g.getStatus() == Status.RESOLVED || g.getStatus() == Status.CLOSED)
                    .count();
            long pending = total - resolved;

            double resolutionRate = total > 0 ? (resolved * 100.0 / total) : 0;

            // Calculate average resolution time for this department
            List<Grievance> resolvedInDept = deptGrievances.stream()
                    .filter(g -> g.getResolvedAt() != null && g.getCreatedAt() != null)
                    .collect(Collectors.toList());

            double avgResolutionHours = 0;
            if (!resolvedInDept.isEmpty()) {
                avgResolutionHours = resolvedInDept.stream()
                        .mapToDouble(g -> Duration.between(g.getCreatedAt(), g.getResolvedAt()).toHours())
                        .average()
                        .orElse(0);
            }

            stats.put("totalGrievances", total);
            stats.put("resolved", resolved);
            stats.put("pending", pending);
            stats.put("resolutionRate", Math.round(resolutionRate * 100.0) / 100.0);
            stats.put("avgResolutionHours", Math.round(avgResolutionHours * 100.0) / 100.0);

            result.put(department, stats);
        });

        return result;
    }

    @Override
    public Map<String, Long> getMonthlyTrends() {
        List<Grievance> grievances = grievanceRepository.findAll();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        return grievances.stream()
                .filter(g -> g.getCreatedAt() != null)
                .collect(Collectors.groupingBy(
                        g -> g.getCreatedAt().format(formatter),
                        TreeMap::new,
                        Collectors.counting()
                ));
    }

    @Override
    public Map<Long, Map<String, Object>> getOfficerWorkload() {
        List<Grievance> grievances = grievanceRepository.findAll();

        Map<Long, List<Grievance>> byOfficer = grievances.stream()
                .filter(g -> g.getAssignedOfficerId() != null)
                .collect(Collectors.groupingBy(Grievance::getAssignedOfficerId));

        Map<Long, Map<String, Object>> result = new LinkedHashMap<>();

        byOfficer.forEach((officerId, officerGrievances) -> {
            Map<String, Object> stats = new LinkedHashMap<>();

            long total = officerGrievances.size();
            long resolved = officerGrievances.stream()
                    .filter(g -> g.getStatus() == Status.RESOLVED || g.getStatus() == Status.CLOSED)
                    .count();
            long inProgress = officerGrievances.stream()
                    .filter(g -> g.getStatus() == Status.IN_PROGRESS || g.getStatus() == Status.ASSIGNED)
                    .count();
            long escalated = officerGrievances.stream()
                    .filter(g -> g.getStatus() == Status.ESCALATED)
                    .count();

            stats.put("totalAssigned", total);
            stats.put("resolved", resolved);
            stats.put("inProgress", inProgress);
            stats.put("escalated", escalated);

            result.put(officerId, stats);
        });

        return result;
    }

    @Override
    public Map<String, Object> getDashboardSummary() {
        List<Grievance> grievances = grievanceRepository.findAll();

        Map<String, Object> summary = new LinkedHashMap<>();

        // Total counts
        summary.put("totalGrievances", grievances.size());

        // Count by status
        Map<String, Long> statusCounts = grievances.stream()
                .collect(Collectors.groupingBy(
                        g -> g.getStatus().name(),
                        Collectors.counting()
                ));
        summary.put("byStatus", statusCounts);

        // Pending vs Resolved
        long pending = grievances.stream()
                .filter(g -> g.getStatus() == Status.PENDING ||
                        g.getStatus() == Status.ASSIGNED ||
                        g.getStatus() == Status.IN_PROGRESS)
                .count();
        long escalated = grievances.stream()
                .filter(g -> g.getStatus() == Status.ESCALATED)
                .count();
        long resolved = grievances.stream()
                .filter(g -> g.getStatus() == Status.RESOLVED || g.getStatus() == Status.CLOSED)
                .count();

        summary.put("pendingCount", pending);
        summary.put("escalatedCount", escalated);
        summary.put("resolvedCount", resolved);

        // Today's grievances
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        long todayCount = grievances.stream()
                .filter(g -> g.getCreatedAt() != null && g.getCreatedAt().isAfter(todayStart))
                .count();
        summary.put("todayCount", todayCount);

        // This week's grievances
        LocalDateTime weekStart = LocalDateTime.now().minusDays(7);
        long weekCount = grievances.stream()
                .filter(g -> g.getCreatedAt() != null && g.getCreatedAt().isAfter(weekStart))
                .count();
        summary.put("thisWeekCount", weekCount);

        // SLA breached (escalated or past deadline)
        long slaBreached = grievances.stream()
                .filter(g -> g.getStatus() == Status.ESCALATED ||
                        (g.getSlaDeadline() != null && g.getSlaDeadline().isBefore(LocalDateTime.now()) &&
                                g.getStatus() != Status.RESOLVED && g.getStatus() != Status.CLOSED)).count();
        summary.put("slaBreachedCount", slaBreached);

        return summary;
    }
}