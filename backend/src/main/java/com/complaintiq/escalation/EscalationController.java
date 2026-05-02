package com.complaintiq.escalation;
import com.complaintiq.common.*;
import com.complaintiq.escalation.dto.EscalationResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/escalations") @RequiredArgsConstructor
@Tag(name="Escalations",description="View escalation history")
public class EscalationController {
    private final EscalationService escalationService;
    @Operation(summary="Get escalations for a complaint") @GetMapping("/{ticketId}") @PreAuthorize("hasAnyRole('AGENT','TEAM_LEAD','MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<List<EscalationResponseDTO>>> getByTicket(@PathVariable String ticketId) { return ResponseEntity.ok(ApiResponse.success("Escalations fetched.", escalationService.getByTicketId(ticketId))); }
    @Operation(summary="Get escalations assigned to agent") @GetMapping("/agent/{agentId}") @PreAuthorize("hasAnyRole('AGENT','TEAM_LEAD','MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<EscalationResponseDTO>>> getByAgent(@PathVariable Long agentId, @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="10") int size) { return ResponseEntity.ok(ApiResponse.success("Agent escalations fetched.", escalationService.getByAgentId(agentId, page, size))); }
}
