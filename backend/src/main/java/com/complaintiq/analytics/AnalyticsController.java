package com.complaintiq.analytics;
import com.complaintiq.analytics.dto.*;
import com.complaintiq.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/analytics") @RequiredArgsConstructor
@Tag(name="Analytics",description="Admin analytics dashboard and reporting")
public class AnalyticsController {
    private final AnalyticsService analyticsService;
    @Operation(summary="Get main dashboard stats") @GetMapping("/dashboard") @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<DashboardStatsDTO>> getDashboard() { return ResponseEntity.ok(ApiResponse.success("Dashboard stats fetched.", analyticsService.getDashboardStats())); }
    @Operation(summary="Get complaint volume trends") @GetMapping("/trends") @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<ComplaintTrendDTO>> getTrends(@RequestParam(defaultValue="daily") String period) { return ResponseEntity.ok(ApiResponse.success("Trends fetched.", analyticsService.getComplaintTrends(period))); }
    @Operation(summary="Get SLA performance report") @GetMapping("/sla-report") @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<SLAReportDTO>> getSlaReport() { return ResponseEntity.ok(ApiResponse.success("SLA report fetched.", analyticsService.getSLAReport())); }
    @Operation(summary="Get agent performance leaderboard") @GetMapping("/agent-performance") @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<List<AgentPerformanceDTO>>> getAgentPerformance() { return ResponseEntity.ok(ApiResponse.success("Agent performance fetched.", analyticsService.getAgentPerformance())); }
    @Operation(summary="Get hourly complaint volume heatmap") @GetMapping("/heatmap") @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<HeatmapDTO>> getHeatmap() { return ResponseEntity.ok(ApiResponse.success("Heatmap fetched.", analyticsService.getHeatmap())); }
}
