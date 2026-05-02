package com.complaintiq.sla;
import com.complaintiq.common.ApiResponse;
import com.complaintiq.sla.dto.SLABreachDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/sla") @RequiredArgsConstructor
@Tag(name="SLA",description="SLA configuration and breach reporting")
public class SLAController {
    private final SLAService slaService;
    @GetMapping("/configs") @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<List<SLAConfig>>> getConfigs() {
        return ResponseEntity.ok(ApiResponse.success("SLA configs fetched.", slaService.getAllConfigs()));
    }
    @GetMapping("/breaches") @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<List<SLABreachDTO>>> getBreaches() {
        List<SLABreachDTO> breaches = slaService.getBreachedComplaints();
        return ResponseEntity.ok(ApiResponse.success(breaches.size() + " SLA breach(es) found.", breaches));
    }
}
