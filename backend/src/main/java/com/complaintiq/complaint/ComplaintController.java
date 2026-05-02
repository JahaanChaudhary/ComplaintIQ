package com.complaintiq.complaint;
import com.complaintiq.assignment.dto.*;
import com.complaintiq.common.*;
import com.complaintiq.complaint.dto.*;
import com.complaintiq.escalation.dto.*;
import com.complaintiq.resolution.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/complaints") @RequiredArgsConstructor
@Tag(name="Complaints",description="Full complaint lifecycle management")
public class ComplaintController {
    private final ComplaintService complaintService;
    @Operation(summary="Submit a new complaint") @PostMapping @PreAuthorize("hasAnyRole('CUSTOMER','AGENT','ADMIN')")
    public ResponseEntity<ApiResponse<ComplaintResponseDTO>> create(@Valid @RequestBody ComplaintRequestDTO request) {
        ComplaintResponseDTO response = complaintService.createComplaint(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Complaint submitted successfully. Ticket ID: " + response.getTicketId(), response));
    }
    @Operation(summary="Track complaint by ticket ID") @GetMapping("/track/{ticketId}")
    public ResponseEntity<ApiResponse<ComplaintResponseDTO>> track(@PathVariable String ticketId) {
        return ResponseEntity.ok(ApiResponse.success("Complaint found.", complaintService.trackComplaint(ticketId)));
    }
    @Operation(summary="Get my complaints") @GetMapping("/my") @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<PagedResponse<ComplaintSummaryDTO>>> getMyComplaints(@AuthenticationPrincipal UserDetails userDetails, @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Complaints fetched.", complaintService.getMyComplaints(userDetails.getUsername(), page, size)));
    }
    @Operation(summary="Get all complaints") @GetMapping @PreAuthorize("hasAnyRole('AGENT','TEAM_LEAD','MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<ComplaintSummaryDTO>>> getAll(@ModelAttribute ComplaintFilterDTO filter) {
        return ResponseEntity.ok(ApiResponse.success("Complaints fetched.", complaintService.getAllComplaints(filter)));
    }
    @Operation(summary="Get full complaint detail") @GetMapping("/{ticketId}") @PreAuthorize("hasAnyRole('CUSTOMER','AGENT','TEAM_LEAD','MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<ComplaintResponseDTO>> getOne(@PathVariable String ticketId, @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Complaint fetched.", complaintService.getByTicketIdForUser(ticketId, userDetails.getUsername(), userDetails.getAuthorities())));
    }
    @Operation(summary="Update complaint status") @PutMapping("/{ticketId}/status") @PreAuthorize("hasAnyRole('AGENT','TEAM_LEAD','MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<ComplaintResponseDTO>> updateStatus(@PathVariable String ticketId, @Valid @RequestBody StatusUpdateDTO request, @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Status updated successfully.", complaintService.updateStatus(ticketId, request, userDetails.getUsername())));
    }
    @Operation(summary="Resolve a complaint") @PostMapping("/{ticketId}/resolve") @PreAuthorize("hasAnyRole('AGENT','TEAM_LEAD','MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<ResolutionResponseDTO>> resolve(@PathVariable String ticketId, @Valid @RequestBody ResolutionRequestDTO request, @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Complaint resolved successfully.", complaintService.resolveComplaint(ticketId, request, userDetails.getUsername())));
    }
    @Operation(summary="Submit satisfaction feedback") @PostMapping("/{ticketId}/feedback") @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    public ResponseEntity<ApiResponse<ComplaintResponseDTO>> feedback(@PathVariable String ticketId, @Valid @RequestBody FeedbackRequestDTO request, @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Feedback submitted. Thank you!", complaintService.submitFeedback(ticketId, request, userDetails.getUsername())));
    }
    @Operation(summary="Reassign complaint") @PostMapping("/{ticketId}/reassign") @PreAuthorize("hasAnyRole('TEAM_LEAD','MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<AssignmentResponseDTO>> reassign(@PathVariable String ticketId, @Valid @RequestBody AssignmentReassignDTO request, @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Complaint reassigned successfully.", complaintService.reassignComplaint(ticketId, request, userDetails.getUsername())));
    }
    @Operation(summary="Manually escalate a complaint") @PostMapping("/{ticketId}/escalate") @PreAuthorize("hasAnyRole('TEAM_LEAD','MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<EscalationResponseDTO>> escalate(@PathVariable String ticketId, @Valid @RequestBody EscalationRequestDTO request, @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Complaint escalated successfully.", complaintService.escalateComplaint(ticketId, request, userDetails.getUsername())));
    }
}
