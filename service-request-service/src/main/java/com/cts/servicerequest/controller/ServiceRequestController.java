package com.cts.servicerequest.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cts.servicerequest.api.ApiResponse;
import com.cts.servicerequest.dto.ServiceRequestDTO;
import com.cts.servicerequest.enums.ServiceRequestStatus;
import com.cts.servicerequest.service.ServiceRequestService;

import jakarta.validation.Valid;

/**
 * REST controller for Service Requests.
 *
 * RBAC Summary:
 * ┌──────────────────────────────────┬──────────────────────────┐
 * │ Endpoint                         │ Allowed Roles            │
 * ├──────────────────────────────────┼──────────────────────────┤
 * │ POST   /api/requests             │ CITIZEN                  │
 * │ GET    /api/requests             │ SUPERVISOR, ADMIN        │
 * │ GET    /api/requests/{id}        │ Any authenticated        │
 * │ GET    /api/requests/citizen/{id}│ CITIZEN                  │
 * │ PUT    /api/requests/{id}/approve│ SUPERVISOR               │
 * │ PUT    /api/requests/{id}/reject │ SUPERVISOR               │
 * │ DELETE /api/requests/{id}        │ ADMIN                    │
 * └──────────────────────────────────┴──────────────────────────┘
 */
@RestController
@RequestMapping("/api/requests")
public class ServiceRequestController {

    private final ServiceRequestService service;

    public ServiceRequestController(ServiceRequestService service) {
        this.service = service;
    }

    // ── CITIZEN: Create a new service request ──────────────────────────────
    @PostMapping
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<ApiResponse<ServiceRequestDTO>> create(
            @Valid @RequestBody ServiceRequestDTO dto) {

        return ResponseEntity.ok(ApiResponse.<ServiceRequestDTO>builder()
                .status("SUCCESS")
                .message("Service request created successfully")
                .data(service.createRequest(dto))
                .build());
    }

    // ── SUPERVISOR / ADMIN: View all requests ──────────────────────────────
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMIN')")
    public ResponseEntity<ApiResponse<List<ServiceRequestDTO>>> getAll() {
        return ResponseEntity.ok(ApiResponse.<List<ServiceRequestDTO>>builder()
                .status("SUCCESS")
                .message("All service requests")
                .data(service.getAllRequests())
                .build());
    }

    // ── Any Authenticated: View a specific request ────────────────────────
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ServiceRequestDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.<ServiceRequestDTO>builder()
                .status("SUCCESS")
                .message("Service request found")
                .data(service.getById(id))
                .build());
    }

    // ── CITIZEN: View own requests ────────────────────────────────────────
    @GetMapping("/citizen/{citizenId}")
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<ApiResponse<List<ServiceRequestDTO>>> getByCitizenId(
            @PathVariable Long citizenId) {

        return ResponseEntity.ok(ApiResponse.<List<ServiceRequestDTO>>builder()
                .status("SUCCESS")
                .message("Requests for citizen " + citizenId)
                .data(service.getAllByCitizenId(citizenId))
                .build());
    }

    // ── SUPERVISOR: Approve a request ─────────────────────────────────────
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<ServiceRequestDTO>> approve(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.<ServiceRequestDTO>builder()
                .status("SUCCESS")
                .message("Service request approved")
                .data(service.approveRequest(id))
                .build());
    }

    // ── SUPERVISOR: Reject a request ─────────────────────────────────────
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<ApiResponse<ServiceRequestDTO>> reject(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.<ServiceRequestDTO>builder()
                .status("SUCCESS")
                .message("Service request rejected")
                .data(service.rejectRequest(id))
                .build());
    }

    // ── ADMIN: Delete a request ───────────────────────────────────────────
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status("SUCCESS")
                .message("Service request deleted successfully")
                .build());
    }
    
    @PreAuthorize("hasRole('SUPERVISOR')")
    @PatchMapping("/{requestId}/")
    public ResponseEntity<Map<String,String>> updateStatusByRequestId(@PathVariable Long requestId, @RequestParam ServiceRequestStatus status){
    	service.updateRequestStatusById(requestId, status);
    	return ResponseEntity.ok(Map.of("message","Status updated Successful"));
    }
}
