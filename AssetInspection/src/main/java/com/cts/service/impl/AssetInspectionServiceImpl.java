package com.cts.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.cts.aspect.audit.Auditable;
import com.cts.dto.request.CreateAssetInspectionRequestDTO;
import com.cts.dto.response.AssetInspectionResponseDTO;
import com.cts.entity.AssetInspection;
import com.cts.enums.InspectionStatus;
import com.cts.exception.ResourceNotFoundException;
import com.cts.exception.ServiceUnavailableException;
import com.cts.mapper.AssetInspectionMapper;
import com.cts.repository.AssetInspectionRepository;
import com.cts.service.ApiClient;
import com.cts.service.AssetInspectionService;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AssetInspectionServiceImpl implements AssetInspectionService {
	
	private final AssetInspectionRepository assetInspectionRepository;
	private final AssetInspectionMapper inspectionMapper;
	private final ApiClient apiClient;

	@Auditable( action = "CREATE", resourceType = "Asset Inspection" )
	@CircuitBreaker(name = "AssetInspection-Service", fallbackMethod = "getDefaultAssetDetails")
	@Retry(name = "AssetInspection-Service")
	@Override
	public AssetInspectionResponseDTO createAssetInspection(CreateAssetInspectionRequestDTO assetInspection) {
		
		apiClient.getAssetDetails(assetInspection.getAssetId()).orElseThrow(() -> new ResourceNotFoundException("Asset not found"));
		
		AssetInspection inspection = inspectionMapper.toEntity(assetInspection);
		
		AssetInspectionResponseDTO dto = inspectionMapper.toResponse(assetInspectionRepository.save(inspection));
		
		return dto;
	}

	
	@Auditable( action = "FETCH", resourceType = "Asset Inspection" )
	@Override
	public List<AssetInspectionResponseDTO> getAllAssetInspection() {
		
		List<AssetInspection> inspections = assetInspectionRepository.findAll();
		List<AssetInspectionResponseDTO> dto = inspections.stream()
				.map(inspectionMapper::toResponse)
				.collect(Collectors.toList());
		return dto;
	}

	
	@Auditable( action = "FETCH", resourceType = "Asset Inspection" )
	@Override
	public AssetInspectionResponseDTO getAssetInspectionById(Long id) {
		AssetInspection inspection = assetInspectionRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Asset Inspection id "+id+" is not Found"));
		
		AssetInspectionResponseDTO dto = inspectionMapper.toResponse(inspection);
		
		return dto;
	}

	
	@Auditable( action = "UPDATE", resourceType = "Asset Inspection" )
	@Override
	public AssetInspectionResponseDTO updateAssetInspectionById(Long id, CreateAssetInspectionRequestDTO assetInspection) {
		AssetInspection inspection = assetInspectionRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Asset Inspection id "+id+" is not Found"));
		
		inspection.setConditionRating(assetInspection.getConditionRating());
		inspection.setFindings(assetInspection.getFindings());
		inspection.setPhotoUri(assetInspection.getPhotoUri());
		inspection.setStatus(assetInspection.getStatus());
		
		AssetInspectionResponseDTO dto = inspectionMapper.toResponse(assetInspectionRepository.save(inspection));
		
		return dto;
	}

	
	@Auditable( action = "UPDATE", resourceType = "Asset Inspection" )
	@Override
	public AssetInspectionResponseDTO updateAssetInspectionPartiallyById(Long id, Map<String, Object> field) {
		AssetInspection inspection = assetInspectionRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Asset Inspection id "+id+" is not Found"));
		
		field.forEach((key,value) -> {
			switch(key) {
				case "status": inspection.setStatus(InspectionStatus.valueOf(value.toString())); break;
				case "performedAt": inspection.setPerformedAt(LocalDateTime.parse(value.toString()));
			}
		});
		
		AssetInspectionResponseDTO dto = inspectionMapper.toResponse(assetInspectionRepository.save(inspection));
		return dto;
	}

	
	 
	@Auditable( action = "DELETE", resourceType = "Asset Inspection" )
	@Override
	public boolean deleteAssetById(Long id) {
		AssetInspection inspection = assetInspectionRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Asset Inspection id "+id+" is not Found"));
		
		assetInspectionRepository.delete(inspection);
		
		return true;
	}
	
	public AssetInspectionResponseDTO getDefaultAssetDetails(CreateAssetInspectionRequestDTO assetInspection, Throwable exception) {
		throw new ServiceUnavailableException("Asset Service Unavailable");
	}

}