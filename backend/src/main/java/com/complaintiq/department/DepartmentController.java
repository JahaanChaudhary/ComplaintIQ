package com.complaintiq.department;
import com.complaintiq.common.ApiResponse;
import com.complaintiq.department.dto.DepartmentDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/departments") @RequiredArgsConstructor
@Tag(name="Departments",description="Department management")
public class DepartmentController {
    private final DepartmentService departmentService;
    @Operation(summary="Create a department") @PostMapping @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<DepartmentDTO>> create(@Valid @RequestBody DepartmentDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Department created successfully.", departmentService.createDepartment(request)));
    }
    @Operation(summary="Get all departments") @GetMapping @PreAuthorize("hasAnyRole('AGENT','TEAM_LEAD','MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<List<DepartmentDTO>>> getAll() { return ResponseEntity.ok(ApiResponse.success("Departments fetched.", departmentService.getAllDepartments())); }
    @Operation(summary="Get department by ID") @GetMapping("/{id}") @PreAuthorize("hasAnyRole('AGENT','TEAM_LEAD','MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<DepartmentDTO>> getById(@PathVariable Long id) { return ResponseEntity.ok(ApiResponse.success("Department fetched.", departmentService.getById(id))); }
    @Operation(summary="Update department") @PutMapping("/{id}") @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<DepartmentDTO>> update(@PathVariable Long id, @Valid @RequestBody DepartmentDTO request) { return ResponseEntity.ok(ApiResponse.success("Department updated.", departmentService.updateDepartment(id, request))); }
}
