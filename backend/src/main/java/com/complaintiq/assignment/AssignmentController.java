package com.complaintiq.assignment;
import com.complaintiq.assignment.dto.*;
import com.complaintiq.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/assignments") @RequiredArgsConstructor
@Tag(name="Assignments",description="View and manage complaint assignments")
public class AssignmentController {
    private final AssignmentService assignmentService;
    @GetMapping("/{ticketId}") @PreAuthorize("hasAnyRole('AGENT','TEAM_LEAD','MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<AssignmentResponseDTO>> getAssignment(@PathVariable String ticketId) {
        return ResponseEntity.ok(ApiResponse.success("Assignment fetched.", assignmentService.getByTicketId(ticketId)));
    }
    @PostMapping("/{ticketId}/reassign") @PreAuthorize("hasAnyRole('TEAM_LEAD','MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<AssignmentResponseDTO>> reassign(@PathVariable String ticketId, @Valid @RequestBody AssignmentReassignDTO request, @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Complaint reassigned successfully.", assignmentService.reassignComplaint(ticketId, request, userDetails.getUsername())));
    }
}
