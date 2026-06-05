package com.cts.servicerequest.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.cts.servicerequest.client.CitizenServiceClient;
import com.cts.servicerequest.dto.ServiceRequestDTO;
import com.cts.servicerequest.entity.ServiceRequest;
import com.cts.servicerequest.enums.ServiceRequestStatus;
import com.cts.servicerequest.exception.ResourceNotFoundException;
import com.cts.servicerequest.mapper.ServiceRequestMapper;
import com.cts.servicerequest.repository.ServiceRequestRepository;
import com.cts.servicerequest.security.CustomUserDetails;
import com.cts.servicerequest.service.ServiceRequestService;

@Service
public class ServiceRequestServiceImpl implements ServiceRequestService {

    private final ServiceRequestRepository requestRepository;
    private final ServiceRequestMapper mapper;
    private final CitizenServiceClient citizenServiceClient;
    

    public ServiceRequestServiceImpl(ServiceRequestRepository requestRepository,
                                     ServiceRequestMapper mapper,
                                     CitizenServiceClient citizenServiceClient) {
        this.requestRepository = requestRepository;
        this.mapper = mapper;
        this.citizenServiceClient = citizenServiceClient;
    }

    // ----------------------------------------------------------------
    // CREATE – CITIZEN only (enforced at controller via @PreAuthorize)
    // ----------------------------------------------------------------
    @Override
    public ServiceRequestDTO createRequest(ServiceRequestDTO dto) {
    	final Object authentication = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        final CustomUserDetails userDetails = (CustomUserDetails) authentication;
    	citizenServiceClient.getAssetById(dto.getAssetId());
        ServiceRequest entity = mapper.toEntity(dto);
        entity.setCitizenId(userDetails.getUserId());
        entity.setStatus(ServiceRequestStatus.PENDING);
        entity.setSubmittedAt(LocalDateTime.now());
        ServiceRequest saved = requestRepository.save(entity);
        return mapper.toDTO(saved);
    }

    // ----------------------------------------------------------------
    // READ ALL – SUPERVISOR / ADMIN only
    // ----------------------------------------------------------------
    @Override
    public List<ServiceRequestDTO> getAllRequests() {
        return requestRepository.findAll()
                .stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    // ----------------------------------------------------------------
    // READ BY ID – any authenticated user
    // ----------------------------------------------------------------
    @Override
    public ServiceRequestDTO getById(Long id) {
        ServiceRequest entity = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service request not found with id: " + id));
        return mapper.toDTO(entity);
    }

    // ----------------------------------------------------------------
    // APPROVE – SUPERVISOR only
    // ----------------------------------------------------------------
    @Override
    public ServiceRequestDTO approveRequest(Long id) {
        ServiceRequest entity = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service request not found with id: " + id));
        entity.setStatus(ServiceRequestStatus.APPROVED);
        return mapper.toDTO(requestRepository.save(entity));
    }

    // ----------------------------------------------------------------
    // REJECT – SUPERVISOR only
    // ----------------------------------------------------------------
    @Override
    public ServiceRequestDTO rejectRequest(Long id) {
        ServiceRequest entity = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service request not found with id: " + id));
        entity.setStatus(ServiceRequestStatus.REJECTED);
        return mapper.toDTO(requestRepository.save(entity));
    }

    // ----------------------------------------------------------------
    // GET ALL BY CITIZEN ID – CITIZEN only (own requests)
    // ----------------------------------------------------------------
    @Override
    public List<ServiceRequestDTO> getAllByCitizenId(Long citizenId) {
        return requestRepository.findAllByCitizenId(citizenId)
                .stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    // ----------------------------------------------------------------
    // DELETE – ADMIN only
    // ----------------------------------------------------------------
    @Override
    public void delete(Long id) {
        ServiceRequest entity = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service request not found with id: " + id));
        requestRepository.delete(entity);
    }
    
    @Override
	public void updateRequestStatusById(Long requestId, ServiceRequestStatus status) {
		ServiceRequest request = requestRepository.findById(requestId).orElseThrow(() -> new ResourceNotFoundException("Service Request with id not Found "+requestId));
		request.setStatus(status);
		requestRepository.save(request);
	}
    
}
