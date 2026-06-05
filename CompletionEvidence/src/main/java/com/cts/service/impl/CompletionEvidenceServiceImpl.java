package com.cts.service.impl;

import java.util.List;


import org.springframework.stereotype.Service;

import com.cts.aspect.audit.Auditable;
import com.cts.dto.request.CreateCompletionEvidenceRequestDTO;
import com.cts.dto.response.CompletionEvidenceResponseDTO;
import com.cts.dto.response.WorkOrderResponseDTO;
import com.cts.entity.CompletionEvidence;
import com.cts.enums.EvidenceStatus;
import com.cts.exception.ResourceNotFoundException;
import com.cts.exception.ServiceUnavailableException;
import com.cts.mapper.CompletionEvidenceMapper;
import com.cts.repository.CompletionEvidenceRepository;
import com.cts.service.CompletionEvidenceService;
import com.cts.service.TaskApiClient;
import com.cts.service.WorkOrderApiClient;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompletionEvidenceServiceImpl implements CompletionEvidenceService {

	private final CompletionEvidenceRepository evidenceRepository;
	private final CompletionEvidenceMapper evidenceMapper;
	private final TaskApiClient taskApiClient;
	private final WorkOrderApiClient workOrderApiClient;
	
	@CircuitBreaker(name = "Completion-Service", fallbackMethod = "getDefaultWorkOrderDetails")
	@Retry(name = "Completion-Service")
	public WorkOrderResponseDTO getWorkOrderDetails(Long id) {
		return workOrderApiClient.getWorkOrderDetails(id).orElseThrow(() -> new ResourceNotFoundException("WorkOrder not found"));
	}

	@CircuitBreaker(name = "Completion-Service", fallbackMethod = "getDefaultCompletionDetails")
	@Retry(name = "Completion-Service")
	@Auditable(action = "CREATE", resourceType = "Completion Evidence")
	@Override
	public CompletionEvidenceResponseDTO addCompletionEvidence(CreateCompletionEvidenceRequestDTO dto) {

		taskApiClient.getTaskDetails(dto.getTaskId()).orElseThrow(() -> new ResourceNotFoundException("Task not found"));

		CompletionEvidence evidence = evidenceMapper.toEntity(dto);

		return evidenceMapper.toResponse(evidenceRepository.save(evidence));
	}

	@Auditable(action = "FETCH", resourceType = "Completion Evidence")
	@Override
	public List<CompletionEvidenceResponseDTO> getAllCompletionEvidences() {
		List<CompletionEvidence> evidences = evidenceRepository.findAll();
		return evidences.stream().map(evidenceMapper::toResponse).toList();
	}

	@Auditable(action = "FETCH", resourceType = "Completion Evidence")
	@Override
	public CompletionEvidenceResponseDTO getCompletionEvidenceById(Long id) {
		CompletionEvidence evidence = evidenceRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Evidence Id not Found " + id));
		return evidenceMapper.toResponse(evidence);
	}

	@Auditable(action = "DELETE", resourceType = "Completion Evidence")
	@Override
	public CompletionEvidenceResponseDTO deleteCompletionById(Long id) {
		CompletionEvidence evidence = evidenceRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Evidence Id not Found " + id));
		evidenceRepository.delete(evidence);
		return evidenceMapper.toResponse(evidence);
	}

	public CompletionEvidenceResponseDTO getDefaultTaskDetails(CreateCompletionEvidenceRequestDTO dto,
			Throwable throwable) {
		throw new ResourceNotFoundException("Task Not found id : " + dto.getTaskId());
	}

	@Auditable(action = "FETCH", resourceType = "Completion Evidence")
	@Override
	public List<CompletionEvidenceResponseDTO> getEvidenceByTaskId(Long id) {
		List<CompletionEvidence> evidence = evidenceRepository.findByTaskId(id);
		return evidence.stream().map(evidenceMapper::toResponse).toList();
	}

	@Override
	public CompletionEvidenceResponseDTO updateStatusOfEvidenceBySupervisor(Long id, EvidenceStatus status) {
		CompletionEvidence evidence = evidenceRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Evidence Id not Found " + id));
		evidence.setStatus(status);
		evidenceRepository.save(evidence);
		return evidenceMapper.toResponse(evidence);
	}
	
	public WorkOrderResponseDTO getDefaultWorkOrderDetails(Long id, Throwable throwable) {
		throw new ServiceUnavailableException("WorkOrder Service Unavailable");
	}

	@Override
	public List<CompletionEvidenceResponseDTO> getEvidenceByWorkOrderIdAndStatus(Long workOrderId,EvidenceStatus status) {
		List<CompletionEvidence> evidences = evidenceRepository.findByWorkOrderIdAndStatus(workOrderId,status);
		List<CompletionEvidenceResponseDTO> dtos = evidences.stream().map(evidenceMapper::toResponse).toList();
		return dtos;
	}

	@Override
	public List<CompletionEvidenceResponseDTO> getEvidenceByWorkOrderId(Long id) {
		List<CompletionEvidence> evidences = evidenceRepository.findByWorkOrderId(id);
		List<CompletionEvidenceResponseDTO> dtos = evidences.stream().map(evidenceMapper::toResponse).toList();
		return dtos;
	}
	
	public CompletionEvidenceResponseDTO getDefaultCompletionDetails(CreateCompletionEvidenceRequestDTO dto, Throwable t) {
		throw new ServiceUnavailableException("User Service Unavailable");
	}

}