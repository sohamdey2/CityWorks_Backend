package com.cts.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.cts.aspect.audit.Auditable;
import com.cts.dto.request.CreateMaintenanceRecordRequestDTO;
import com.cts.dto.response.MaintenanceRecordResponseDTO;
import com.cts.entity.MaintenanceRecord;
import com.cts.enums.MaintenanceStatus;
import com.cts.exception.ResourceNotFoundException;
import com.cts.exception.ServiceUnavailableException;
import com.cts.mapper.MaintenanceRecordMapper;
import com.cts.repository.MaintenanceRecordRepository;
import com.cts.service.ApiClient;
import com.cts.service.MaintenanceRecordService;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MaintenanceRecordServiceImpl implements MaintenanceRecordService {

	private final MaintenanceRecordRepository maintenanceRecordRepository;
	private final ApiClient apiClient;
	private final MaintenanceRecordMapper recordMapper;

	@Auditable(action = "CREATE", resourceType = "Maintenance Record")
	@CircuitBreaker(name = "MaintenanceRecord-Service", fallbackMethod = "getDefaultAssetDetails")
	@Retry(name = "MaintenanceRecord-Service")
	@Override
	public MaintenanceRecordResponseDTO createMaintenanceRecord(CreateMaintenanceRecordRequestDTO mRecord) {

		apiClient.getAssetDetails(mRecord.getAssetId())
				.orElseThrow(() -> new ResourceNotFoundException("Asset Not found id : " + mRecord.getAssetId()));

		MaintenanceRecord record = recordMapper.toEntity(mRecord);

		MaintenanceRecordResponseDTO dto = recordMapper.toResponse(maintenanceRecordRepository.save(record));

		return dto;
	}

	@Auditable(action = "UPDATE", resourceType = "Maintenance Record")
	@Override
	public MaintenanceRecordResponseDTO updateRecordPartiallyById(Long id, Map<String, Object> updates) {

		MaintenanceRecord record = maintenanceRecordRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Record Not found id : " + id));

		updates.forEach((key, values) -> {
			switch (key) {
			case "status":
				record.setStatus(MaintenanceStatus.valueOf(values.toString()));
				break;
			}
		});

		MaintenanceRecordResponseDTO dto = recordMapper.toResponse(maintenanceRecordRepository.save(record));

		return dto;
	}

	@Auditable(action = "FETCH", resourceType = "Maintenance Record")
	@Override
	public List<MaintenanceRecordResponseDTO> getAllMaintenanceRecord() {

		List<MaintenanceRecord> records = maintenanceRecordRepository.findAll();

		List<MaintenanceRecordResponseDTO> dto = records.stream().map(recordMapper::toResponse)
				.collect(Collectors.toList());

		return dto;
	}

	@Auditable(action = "FETCH", resourceType = "Maintenance Record")
	@Override
	public MaintenanceRecordResponseDTO getMaintenanceById(Long id) {

		MaintenanceRecord record = maintenanceRecordRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Record Not found id : " + id));

		MaintenanceRecordResponseDTO dto = recordMapper.toResponse(record);

		return dto;
	}

	@Auditable(action = "DELETE", resourceType = "Maintenance Record")
	@Override
	public void deleteRecordById(Long id) {

		MaintenanceRecord record = maintenanceRecordRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Record Not found id : " + id));

		maintenanceRecordRepository.delete(record);

	}

	@Auditable(action = "UPDATE", resourceType = "Maintenance Record")
	@Override
	public MaintenanceRecordResponseDTO updateMaintenanceRecordById(Long id,
			CreateMaintenanceRecordRequestDTO mRecord) {

		MaintenanceRecord record = maintenanceRecordRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Record Not found id : " + id));

		record.setTaskDescription(mRecord.getTaskDescription());
		record.setPerformedBy(mRecord.getPerformedBy());
		record.setPerformedAt(mRecord.getPerformedAt());
		record.setCost(mRecord.getCost());
		record.setStatus(mRecord.getStatus());

		MaintenanceRecordResponseDTO dto = recordMapper.toResponse(record);

		return dto;
	}

	public MaintenanceRecordResponseDTO getDefaultAssetDetails(CreateMaintenanceRecordRequestDTO mRecord,
			Throwable exception) {
		throw new ServiceUnavailableException("Asset Service Unavailable");
	}

}