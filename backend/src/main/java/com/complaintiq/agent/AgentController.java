package com.complaintiq.agent;
import com.complaintiq.agent.dto.*;
import com.complaintiq.common.*;
import com.complaintiq.complaint.ComplaintService;
import com.complaintiq.complaint.dto.ComplaintSummaryDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/agents") @RequiredArgsConstructor
@Tag(name="Agents",description="Agent management and dashboard")
public class AgentController {
    private final AgentService agentService;
    private final ComplaintService complaintService;
    @Operation(summary="Create a new agent") @PostMapping @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AgentResponseDTO>> create(@Valid @RequestBody AgentCreateDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Agent created successfully.", agentService.createAgent(request)));
    }
    @Operation(summary="Get all agents") @GetMapping @PreAuthorize("hasAnyRole('AGENT','TEAM_LEAD','MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<AgentResponseDTO>>> getAll(@RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Agents fetched.", agentService.getAllAgents(page, size)));
    }
    @Operation(summary="Get agent by ID") @GetMapping("/{id}") @PreAuthorize("hasAnyRole('AGENT','TEAM_LEAD','MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<AgentResponseDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Agent fetched.", agentService.getById(id)));
    }
    @Operation(summary="Get agent dashboard") @GetMapping("/{id}/dashboard") @PreAuthorize("hasAnyRole('AGENT','TEAM_LEAD','MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<AgentDashboardDTO>> getDashboard(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Dashboard fetched.", agentService.getDashboard(id)));
    }
    @Operation(summary="Toggle agent availability") @PutMapping("/{id}/availability") @PreAuthorize("hasAnyRole('AGENT','TEAM_LEAD','MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<AgentResponseDTO>> toggleAvailability(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Availability updated.", agentService.toggleAvailability(id)));
    }
    @Operation(summary="Get agent complaints") @GetMapping("/{id}/complaints") @PreAuthorize("hasAnyRole('AGENT','TEAM_LEAD','MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<ComplaintSummaryDTO>>> getComplaints(@PathVariable Long id, @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Agent complaints fetched.", complaintService.getComplaintsByAgent(id, page, size)));
    }
}
