package com.complaintiq.ai;
import com.complaintiq.common.ApiResponse;
import com.complaintiq.complaint.*;
import com.complaintiq.customer.Customer;
import com.complaintiq.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/ai") @RequiredArgsConstructor
@Tag(name="AI Analysis",description="View and re-trigger AI analysis for complaints")
public class AIAnalysisController {
    private final AIAnalysisService aiAnalysisService;
    private final AIAnalysisRepository aiAnalysisRepository;
    private final ComplaintRepository complaintRepository;
    @Operation(summary="Get AI analysis for a complaint") @GetMapping("/analysis/{ticketId}") @PreAuthorize("hasAnyRole('AGENT','TEAM_LEAD','MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<AIAnalysis>> getAnalysis(@PathVariable String ticketId) {
        AIAnalysis analysis = aiAnalysisRepository.findByComplaintTicketId(ticketId).orElseThrow(() -> new ResourceNotFoundException("AIAnalysis","ticketId",ticketId));
        return ResponseEntity.ok(ApiResponse.success("AI analysis fetched.", analysis));
    }
    @Operation(summary="Re-trigger AI analysis") @PostMapping("/re-analyze/{ticketId}") @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<AIAnalysis>> reAnalyze(@PathVariable String ticketId) {
        Complaint complaint = complaintRepository.findByTicketId(ticketId).orElseThrow(() -> new ResourceNotFoundException("Complaint","ticketId",ticketId));
        Customer customer = complaint.getCustomer();
        AIAnalysis analysis = aiAnalysisService.reAnalyzeComplaint(complaint, customer);
        return ResponseEntity.ok(ApiResponse.success("Complaint re-analyzed successfully.", analysis));
    }
}
