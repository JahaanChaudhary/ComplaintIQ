package com.complaintiq.resolution;
import com.complaintiq.common.ApiResponse;
import com.complaintiq.resolution.dto.ResolutionResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/resolutions") @RequiredArgsConstructor
@Tag(name="Resolutions",description="View complaint resolutions and feedback")
public class ResolutionController {
    private final ResolutionService resolutionService;
    @Operation(summary="Get resolution for a complaint") @GetMapping("/{ticketId}") @PreAuthorize("hasAnyRole('AGENT','TEAM_LEAD','MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<ResolutionResponseDTO>> getByTicket(@PathVariable String ticketId) { return ResponseEntity.ok(ApiResponse.success("Resolution fetched.", resolutionService.getByTicketId(ticketId))); }
    @Operation(summary="Get average satisfaction score") @GetMapping("/stats/avg-satisfaction") @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<Double>> getAvgSatisfaction() { return ResponseEntity.ok(ApiResponse.success("Average satisfaction score fetched.", resolutionService.getAverageSatisfactionScore())); }
}
