package com.complaintiq.customer;
import com.complaintiq.common.*;
import com.complaintiq.customer.enums.CustomerTier;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/customers") @RequiredArgsConstructor
@Tag(name="Customers",description="Customer profile management")
public class CustomerController {
    private final CustomerService customerService;
    @Operation(summary="Get all customers") @GetMapping @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<Customer>>> getAll(@RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="10") int size) { return ResponseEntity.ok(ApiResponse.success("Customers fetched.", customerService.getAllCustomers(page, size))); }
    @Operation(summary="Get customer by ID") @GetMapping("/{id}") @PreAuthorize("hasAnyRole('AGENT','TEAM_LEAD','MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<Customer>> getById(@PathVariable Long id) { return ResponseEntity.ok(ApiResponse.success("Customer fetched.", customerService.getById(id))); }
    @Operation(summary="Update customer tier") @PutMapping("/{id}/tier") @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<Customer>> updateTier(@PathVariable Long id, @RequestParam CustomerTier tier) { return ResponseEntity.ok(ApiResponse.success("Customer tier updated to " + tier.name() + ".", customerService.updateTier(id, tier))); }
    @Operation(summary="Deactivate customer") @DeleteMapping("/{id}") @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) { customerService.deactivateCustomer(id); return ResponseEntity.ok(ApiResponse.success("Customer deactivated successfully.")); }
}
