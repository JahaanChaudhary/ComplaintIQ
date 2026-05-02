package com.complaintiq.analytics;
import com.complaintiq.agent.AgentRepository;
import com.complaintiq.analytics.dto.*;
import com.complaintiq.complaint.ComplaintRepository;
import com.complaintiq.complaint.enums.*;
import com.complaintiq.escalation.EscalationRepository;
import com.complaintiq.resolution.ResolutionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;
@Slf4j @Service @RequiredArgsConstructor
public class AnalyticsService {
    private final ComplaintRepository complaintRepository;
    private final AgentRepository agentRepository;
    private final ResolutionRepository resolutionRepository;
    private final EscalationRepository escalationRepository;
    @Transactional(readOnly=true)
    public DashboardStatsDTO getDashboardStats() {
        long totalComplaints = complaintRepository.count();
        long openComplaints = complaintRepository.countByStatus(ComplaintStatus.OPEN) + complaintRepository.countByStatus(ComplaintStatus.ASSIGNED);
        long escalatedComplaints = complaintRepository.countByStatus(ComplaintStatus.ESCALATED);
        long resolvedToday = complaintRepository.countResolvedToday(LocalDate.now().atStartOfDay());
        long criticalOpen = complaintRepository.countByUrgencyAndStatus(UrgencyLevel.CRITICAL, ComplaintStatus.OPEN) + complaintRepository.countByUrgencyAndStatus(UrgencyLevel.CRITICAL, ComplaintStatus.ASSIGNED);
        long totalSlaBreaches = escalationRepository.countSlaBreaches();
        double slaBreachRate = totalComplaints > 0 ? (totalSlaBreaches * 100.0) / totalComplaints : 0.0;
        Double avgSatisfaction = resolutionRepository.findAverageSatisfactionScore();
        Map<String,Long> byCategory = new LinkedHashMap<>(); complaintRepository.countByCategory().forEach(row -> byCategory.put(row[0].toString(), ((Number)row[1]).longValue()));
        Map<String,Long> byUrgency = new LinkedHashMap<>(); complaintRepository.countByUrgency().forEach(row -> byUrgency.put(row[0].toString(), ((Number)row[1]).longValue()));
        Map<String,Long> byStatus = new LinkedHashMap<>(); complaintRepository.countByStatus().forEach(row -> byStatus.put(row[0].toString(), ((Number)row[1]).longValue()));
        List<AgentPerformanceDTO> topAgents = agentRepository.findTopPerformers(PageRequest.of(0,5)).stream().map(agent -> {
            Double avgTime = resolutionRepository.findAvgResolutionHoursByAgent(agent.getId());
            long breaches = escalationRepository.countSlaBreachesByAgent(agent.getId());
            return AgentPerformanceDTO.builder().agentId(agent.getId()).agentName(agent.getName()).departmentName(agent.getDepartment() != null ? agent.getDepartment().getName() : null).role(agent.getRole().name()).totalResolved((long)agent.getTotalResolved()).avgResolutionTimeHours(avgTime != null ? avgTime : 0.0).slaBreachCount(breaches).currentLoad(agent.getCurrentLoad()).maxLoad(agent.getMaxLoad()).build();
        }).collect(Collectors.toList());
        return DashboardStatsDTO.builder().totalComplaints(totalComplaints).openComplaints(openComplaints).resolvedToday(resolvedToday).criticalOpen(criticalOpen).escalatedComplaints(escalatedComplaints).slaBreachRate(slaBreachRate).avgSatisfactionScore(avgSatisfaction != null ? avgSatisfaction : 0.0).complaintsByCategory(byCategory).complaintsByUrgency(byUrgency).complaintsByStatus(byStatus).topPerformingAgents(topAgents).build();
    }
    @Transactional(readOnly=true)
    public ComplaintTrendDTO getComplaintTrends(String period) {
        if (period == null || period.isBlank()) period = "daily";
        LocalDateTime from = period.equalsIgnoreCase("weekly") ? LocalDateTime.now().minusWeeks(12) : LocalDateTime.now().minusDays(30);
        List<ComplaintTrendDTO.TrendPoint> dataPoints = complaintRepository.findDailyTrend(from).stream().map(row -> ComplaintTrendDTO.TrendPoint.builder().date(row[0].toString()).count(((Number)row[1]).longValue()).build()).collect(Collectors.toList());
        return ComplaintTrendDTO.builder().period(period).dataPoints(dataPoints).build();
    }
    @Transactional(readOnly=true)
    public SLAReportDTO getSLAReport() {
        long total = complaintRepository.count(); long breached = escalationRepository.countSlaBreaches(); long onTime = Math.max(0, total - breached);
        double breachRate = total > 0 ? (breached * 100.0) / total : 0.0;
        List<SLAReportDTO.SLABreachDetail> recentBreaches = complaintRepository.findSlaBreachedComplaints(LocalDateTime.now()).stream().limit(10).map(c -> {
            double overdue = 0.0;
            if (c.getSlaDeadline() != null) overdue = java.time.temporal.ChronoUnit.MINUTES.between(c.getSlaDeadline(), LocalDateTime.now()) / 60.0;
            return SLAReportDTO.SLABreachDetail.builder().ticketId(c.getTicketId()).urgency(c.getUrgency() != null ? c.getUrgency().name() : "UNKNOWN").agentName("N/A").hoursOverdue(Math.max(0, overdue)).build();
        }).collect(Collectors.toList());
        return SLAReportDTO.builder().totalComplaints(total).onTimeCount(onTime).warningCount(0L).breachedCount(breached).breachRatePercent(breachRate).recentBreaches(recentBreaches).build();
    }
    @Transactional(readOnly=true)
    public List<AgentPerformanceDTO> getAgentPerformance() {
        return agentRepository.findTopPerformers(PageRequest.of(0,20)).stream().map(agent -> {
            Double avgTime = resolutionRepository.findAvgResolutionHoursByAgent(agent.getId()); long breaches = escalationRepository.countSlaBreachesByAgent(agent.getId());
            return AgentPerformanceDTO.builder().agentId(agent.getId()).agentName(agent.getName()).departmentName(agent.getDepartment() != null ? agent.getDepartment().getName() : null).role(agent.getRole().name()).totalResolved((long)agent.getTotalResolved()).avgResolutionTimeHours(avgTime != null ? avgTime : 0.0).slaBreachCount(breaches).currentLoad(agent.getCurrentLoad()).maxLoad(agent.getMaxLoad()).build();
        }).collect(Collectors.toList());
    }
    @Transactional(readOnly=true)
    public HeatmapDTO getHeatmap() {
        Map<Integer,Long> hourMap = new LinkedHashMap<>(); for (int i=0; i<24; i++) hourMap.put(i, 0L);
        complaintRepository.findHourlyVolume().forEach(row -> hourMap.put(((Number)row[0]).intValue(), ((Number)row[1]).longValue()));
        List<HeatmapDTO.HeatmapCell> cells = hourMap.entrySet().stream().map(e -> HeatmapDTO.HeatmapCell.builder().hour(e.getKey()).complaintCount(e.getValue()).label(formatHour(e.getKey())).build()).collect(Collectors.toList());
        return HeatmapDTO.builder().cells(cells).build();
    }
    private String formatHour(int hour) { if (hour==0) return "12 AM"; if (hour<12) return hour+" AM"; if (hour==12) return "12 PM"; return (hour-12)+" PM"; }
}
