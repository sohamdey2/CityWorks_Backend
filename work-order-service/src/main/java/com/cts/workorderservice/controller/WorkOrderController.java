package com.cts.workorderservice.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cts.workorderservice.api.ApiResponse;
import com.cts.workorderservice.dto.request.AssignWorkerRequest;
import com.cts.workorderservice.dto.request.CreateWorkOrderRequest;
import com.cts.workorderservice.dto.response.AssignWorkerResponseDTO;
import com.cts.workorderservice.dto.response.CreateWorkOrderResponseDTO;
import com.cts.workorderservice.dto.response.WorkOrderResponseDTO;
import com.cts.workorderservice.enums.WorkOrderStatus;
import com.cts.workorderservice.service.impl.WorkOrderServiceImpl;

@RestController
@RequestMapping("/api/workorders")
public class WorkOrderController {

    private final WorkOrderServiceImpl workOrderService;

    public WorkOrderController(WorkOrderServiceImpl workOrderService) {
        this.workOrderService = workOrderService;
    }


    @PreAuthorize("hasRole('SUPERVISOR')")
    @PostMapping
    public ResponseEntity<ApiResponse<CreateWorkOrderResponseDTO>> createWorkOrder(
            @RequestBody CreateWorkOrderRequest request) {

        CreateWorkOrderResponseDTO data = workOrderService.createWorkOrder(request);
        ApiResponse<CreateWorkOrderResponseDTO> response =
                new ApiResponse<>("SUCCESS", "Work order created successfully", data);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasRole('SUPERVISOR')")
    @PutMapping("/assign")
    public ResponseEntity<ApiResponse<AssignWorkerResponseDTO>> assignWorker(
            @RequestBody AssignWorkerRequest request) {

    	AssignWorkerResponseDTO data = workOrderService.assignWorker(request);
        ApiResponse<AssignWorkerResponseDTO> response =
                new ApiResponse<>("SUCCESS", "Worker assigned successfully", data);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR') or hasRole('WORKER')")
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<WorkOrderResponseDTO>>> getAllWorkOrders() {

        List<WorkOrderResponseDTO> data = workOrderService.getAllWorkOrders();
        ApiResponse<List<WorkOrderResponseDTO>> response =
                new ApiResponse<>("FOUND", "All work orders retrieved", data);
        return ResponseEntity.ok(response);
    }


    @PreAuthorize("hasRole('WORKER')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<WorkOrderResponseDTO>>> getMyWorkOrders(
            @RequestParam Long workerId) {

        List<WorkOrderResponseDTO> data = workOrderService.getWorkOrdersForWorker(workerId);
        ApiResponse<List<WorkOrderResponseDTO>> response =
                new ApiResponse<>("FOUND", "Work orders retrieved", data);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkOrderResponseDTO>> getById(
            @PathVariable Long id) {

        WorkOrderResponseDTO data = workOrderService.getById(id);
        ApiResponse<WorkOrderResponseDTO> response =
                new ApiResponse<>("FOUND", "Work order retrieved", data);
        return ResponseEntity.ok(response);
    }


    @PreAuthorize("hasRole('SUPERVISOR')")
    @PutMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<WorkOrderResponseDTO>> updateStatus(
            @PathVariable Long orderId,
            @RequestParam WorkOrderStatus status) {

        WorkOrderResponseDTO data = workOrderService.updateStatus(orderId, status);
        ApiResponse<WorkOrderResponseDTO> response =
                new ApiResponse<>("SUCCESS",
                        "Work order status updated to " + status, data);
        return ResponseEntity.ok(response);
    }
    @PreAuthorize("hasRole('SUPERVISOR')")
    @GetMapping("/requestId")
    public ResponseEntity<WorkOrderResponseDTO> getWorkOrderByRequestId(@RequestParam Long requestId){
    	WorkOrderResponseDTO dto = workOrderService.getWorkOrderByRequestId(requestId);
    	return ResponseEntity.ok(dto);
    }
}
