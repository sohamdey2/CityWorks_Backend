package com.cts.workorderservice.service.impl;

import com.cts.workorderservice.api.ApiResponse;
import com.cts.workorderservice.audit.annotation.Auditable;
import com.cts.workorderservice.client.CompletionEvidenceClient;
import com.cts.workorderservice.client.ServiceRequestClient;
import com.cts.workorderservice.dto.request.AssignWorkerRequest;
import com.cts.workorderservice.dto.request.CreateWorkOrderRequest;
import com.cts.workorderservice.dto.response.AssignWorkerResponseDTO;
import com.cts.workorderservice.dto.response.CompletionEvidenceResponseDTO;
import com.cts.workorderservice.dto.response.CreateWorkOrderResponseDTO;
import com.cts.workorderservice.dto.response.WorkOrderResponseDTO;
import com.cts.workorderservice.entity.WorkOrder;
import com.cts.workorderservice.enums.WorkOrderStatus;
import com.cts.workorderservice.exception.InvalidOperationException;
import com.cts.workorderservice.exception.ResourceNotFoundException;
import com.cts.workorderservice.mapper.WorkOrderMapper;
import com.cts.workorderservice.repository.WorkOrderRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WorkOrderServiceImpl {

    private final WorkOrderRepository workOrderRepository;
    private final ServiceRequestClient serviceRequestClient;
    private final CompletionEvidenceClient evidenceClient;

    public WorkOrderServiceImpl(WorkOrderRepository workOrderRepository,
                                ServiceRequestClient serviceRequestClient,
                                CompletionEvidenceClient evidenceClient) {
        this.workOrderRepository = workOrderRepository;
        this.serviceRequestClient = serviceRequestClient;
        this.evidenceClient = evidenceClient;
    }

    // ─── CREATE WORK ORDER ───────────────────────────────────────────────────

    /**
     * Duplicate check is done BEFORE the Feign call and OUTSIDE the
     * @CircuitBreaker scope. This is critical — if the duplicate check
     * was inside the circuit breaker method, Resilience4j would treat
     * the InvalidOperationException as a Feign failure and trip the circuit.
     */
    @Auditable(action = "CREATE_WORK_ORDER", resourceType = "WorkOrder")
    public CreateWorkOrderResponseDTO createWorkOrder(CreateWorkOrderRequest request) {

        // Step 1: Check for duplicate BEFORE making any Feign call
        // This runs outside the circuit breaker — exception propagates cleanly
        workOrderRepository.findByRequestId(request.getRequestId()).ifPresent(wo -> {
            throw new InvalidOperationException(
                    "A work order already exists for request id: "
                    + request.getRequestId());
        });

        // Step 2: Feign call is isolated in a separate method with circuit breaker
        return createWorkOrderWithFeign(request);
    }

    @CircuitBreaker(name = "serviceRequestCB", fallbackMethod = "createWorkOrderFallback")
    @Retry(name = "serviceRequestCB")
    public CreateWorkOrderResponseDTO createWorkOrderWithFeign(CreateWorkOrderRequest request) {

        // Step 3: Feign call to service-request-service
        Map<String, Object> srResponse;
        try {
            srResponse = serviceRequestClient.getServiceRequestById(request.getRequestId());
        } catch (Exception e) {
            throw new ResourceNotFoundException(
                    "Service request not found with id: " + request.getRequestId());
        }

        // Step 4: Extract data from ApiResponse envelope
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) srResponse.get("data");
        if (data == null) {
            throw new ResourceNotFoundException(
                    "Service request not found with id: " + request.getRequestId());
        }

        // Step 5: Validate status is APPROVED
        String status = (String) data.get("status");
        if (!"APPROVED".equalsIgnoreCase(status)) {
            throw new InvalidOperationException(
                    "Work order can only be created for APPROVED service requests. "
                    + "Current status: " + status);
        }

        // Step 6: Extract assetId from service request data
        Long assetId = null;
        if (data.get("assetId") != null) {
            assetId = Long.valueOf(data.get("assetId").toString());
        }

        // Step 7: Create work order
        WorkOrder workOrder = new WorkOrder();
        workOrder.setRequestId(request.getRequestId());
        workOrder.setAssetId(assetId);
        workOrder.setStatus(WorkOrderStatus.CREATED);

        WorkOrder saved = workOrderRepository.save(workOrder);
        System.out.println("Work order created with id: " + saved.getWorkOrderId()
                + " for requestId: " + request.getRequestId());
        return WorkOrderMapper.toCreateResponse(saved);
    }

    public WorkOrderResponseDTO createWorkOrderFallback(
            CreateWorkOrderRequest request, Throwable ex) {
        System.err.println("[CIRCUIT BREAKER] service-request-service unavailable: "
                + ex.getMessage());
        throw new InvalidOperationException(
                "Unable to create work order: Service Request Service is currently "
                + "unavailable. Please try again later.");
    }

    // ─── ASSIGN WORKER ───────────────────────────────────────────────────────

    @Auditable(action = "ASSIGN_WORKER", resourceType = "WorkOrder")
    public AssignWorkerResponseDTO assignWorker(AssignWorkerRequest request) {
        WorkOrder workOrder = workOrderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Work order not found with id: " + request.getOrderId()));

        if (WorkOrderStatus.COMPLETED.equals(workOrder.getStatus())) {
            throw new InvalidOperationException(
                    "Cannot assign worker to a completed work order.");
        }

        workOrder.setAssignedWorkerId(request.getWorkerId());
        workOrder.setAssignedAt(LocalDateTime.now());
        workOrder.setStatus(WorkOrderStatus.ASSIGNED);

        WorkOrder updated = workOrderRepository.save(workOrder);
        System.out.println("Worker " + request.getWorkerId()
                + " assigned to work order " + request.getOrderId());
        return WorkOrderMapper.toAssignResponse(updated);
    }

    // ─── READ ────────────────────────────────────────────────────────────────

    public List<WorkOrderResponseDTO> getAllWorkOrders() {
        return workOrderRepository.findAll()
                .stream()
                .map(WorkOrderMapper::toResponse)
                .collect(Collectors.toList());
    }

    public WorkOrderResponseDTO getById(Long id) {
        WorkOrder workOrder = workOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Work order not found with id: " + id));
        return WorkOrderMapper.toResponse(workOrder);
    }

    public List<WorkOrderResponseDTO> getWorkOrdersForWorker(Long workerId) {
        List<WorkOrder> list = workOrderRepository.findByAssignedWorkerId(workerId);
        if (list.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No work orders found for worker id: " + workerId);
        }
        return list.stream()
                .map(WorkOrderMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ─── UPDATE STATUS (with evidence gate for COMPLETED) ───────────────────

    @Auditable(action = "UPDATE_WORK_ORDER_STATUS", resourceType = "WorkOrder")
    @CircuitBreaker(name = "evidenceCB", fallbackMethod = "updateStatusFallback")
    @Retry(name = "evidenceCB")
    public WorkOrderResponseDTO updateStatus(Long orderId, WorkOrderStatus newStatus) {
        WorkOrder workOrder = workOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Work order not found with id: " + orderId));

        if (WorkOrderStatus.COMPLETED.equals(workOrder.getStatus())) {
            throw new InvalidOperationException(
                    "Work order is already completed.");
        }

        // ── EVIDENCE GATE ──────────────────────────────────────────────────
        if (WorkOrderStatus.COMPLETED.equals(newStatus)) {
            ApiResponse<List<CompletionEvidenceResponseDTO>> evidenceList;
            try {
                evidenceList = evidenceClient.getEvidenceByWorkOrderId(orderId, "VERIFIED");
            } catch (Exception e) {
                throw new InvalidOperationException(
                        "Cannot mark work order as COMPLETED: unable to verify "
                        + "completion evidence. Please try again.");
            }

            if (evidenceList.getData() == null || evidenceList.getData().isEmpty()) {
                throw new InvalidOperationException(
                        "Cannot mark work order as COMPLETED: no completion evidence "
                        + "has been uploaded. Please upload work evidence first, "
                        + "then set status to COMPLETED.");
            }

            workOrder.setCompletedAt(LocalDateTime.now());
            System.out.println("Evidence verified for work order "
                    + orderId + ". Marking as COMPLETED.");
        }
        // ── END EVIDENCE GATE ──────────────────────────────────────────────

        workOrder.setStatus(newStatus);
        WorkOrder updated = workOrderRepository.save(workOrder);
        System.out.println("Work order " + orderId
                + " status updated to " + newStatus);
        return WorkOrderMapper.toResponse(updated);
    }

    public WorkOrderResponseDTO updateStatusFallback(
            Long orderId, WorkOrderStatus newStatus, Throwable ex) {
        System.err.println("[CIRCUIT BREAKER] completion-evidence-service unavailable: "
                + ex.getMessage());
        throw new InvalidOperationException(
                "Cannot update work order status: Completion Evidence Service is "
                + "currently unavailable. Please try again later.");
    }
    
    public WorkOrderResponseDTO getWorkOrderByRequestId(Long requestId) {
    	WorkOrder order = workOrderRepository.findByRequestId(requestId).orElseThrow(() -> new ResourceNotFoundException("Not Found"));
    	
    	return WorkOrderMapper.toResponse(order);
    }
}