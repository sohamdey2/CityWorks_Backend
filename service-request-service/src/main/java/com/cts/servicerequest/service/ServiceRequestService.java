package com.cts.servicerequest.service;

import java.util.List;

import com.cts.servicerequest.dto.ServiceRequestDTO;
import com.cts.servicerequest.enums.ServiceRequestStatus;

public interface ServiceRequestService {

    ServiceRequestDTO createRequest(ServiceRequestDTO dto);

    List<ServiceRequestDTO> getAllRequests();

    ServiceRequestDTO getById(Long id);

    ServiceRequestDTO approveRequest(Long id);

    ServiceRequestDTO rejectRequest(Long id);

    List<ServiceRequestDTO> getAllByCitizenId(Long citizenId);

    void delete(Long id);
    
    void updateRequestStatusById(Long requestId, ServiceRequestStatus status);
}
