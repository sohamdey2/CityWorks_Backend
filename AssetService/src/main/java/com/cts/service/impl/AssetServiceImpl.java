package com.cts.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.cts.audit.Auditable;
import com.cts.dto.request.CreateAssetRequestDTO;
import com.cts.dto.response.AssetResponseDTO;
import com.cts.entity.Asset;
import com.cts.enums.AssetCondition;
import com.cts.enums.AssetStatus;
import com.cts.exception.ResourceNotFoundException;
import com.cts.mapper.AssetMapper;
import com.cts.repository.AssetRepository;
import com.cts.service.AssetService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AssetServiceImpl implements AssetService {
	
	
	private final AssetRepository assetRepository;

	@Auditable( action = "CREATE",
			resourceType = "Asset"
			)
	@Override
	public AssetResponseDTO addAssets(CreateAssetRequestDTO assetDTO) {
		
		Asset asset = AssetMapper.toEntity(assetDTO);
		
		AssetResponseDTO dto = AssetMapper.toResponse(assetRepository.save(asset));
		
		return dto;
	}

	@Auditable( action = "FETCH",
			resourceType = "Asset"
			)
	@Override
	public List<AssetResponseDTO> getAllAssets() {
		
		List<Asset> assets = assetRepository.findAll();
		
		List<AssetResponseDTO> dto = assets.stream()
				.map(AssetMapper::toResponse)
				.collect(Collectors.toList());
		
		return dto;
	}

	@Auditable( action = "FETCH",
			resourceType = "Asset"
			)
	@Override
	public AssetResponseDTO getAssetById(Long assetId) {
		
		Asset asset = assetRepository.findById(assetId).orElseThrow(() -> new ResourceNotFoundException("Asset id "+assetId+" is Not Found"));
		
		return AssetMapper.toResponse(asset);
	}

	@Auditable( action = "UPDATE",
			resourceType = "Asset"
			)
	@Override
	public AssetResponseDTO updateAssetPartiallyById(Long assetId, Map<String,Object> updates){
		
		Asset oldAsset = assetRepository.findById(assetId)
				.orElseThrow(() -> new ResourceNotFoundException("Asset id "+assetId+" is Not Found"));
		
		updates.forEach((key,values) -> {
			switch(key) 
			{
				case "condition": oldAsset.setCondition(AssetCondition.valueOf(values.toString())); break;
				case "status": oldAsset.setStatus(AssetStatus.valueOf(values.toString())); break;
			}
		});
		
		Asset dto = assetRepository.save(oldAsset);
		
		return AssetMapper.toResponse(dto);
	}

	@Auditable( action = "DELETE",
			resourceType = "Asset"
			)
	@Override
	public void deleteAssetById(Long assetId) {
		
		Asset asset = assetRepository.findById(assetId).orElseThrow(() -> new ResourceNotFoundException("Asset id "+assetId+" is Not Found"));
		
		assetRepository.delete(asset);
		
	}

	@Auditable( action = "UPDATE",
			resourceType = "Asset"
			)
	@Override
	public AssetResponseDTO updateAssetById(Long assetId, CreateAssetRequestDTO asset) {
		
		Asset oldAsset = assetRepository.findById(assetId).orElseThrow(() -> new ResourceNotFoundException("Asset id "+assetId+" is Not Found"));
		
		oldAsset.setName(asset.getName());
		oldAsset.setCondition(asset.getCondition());
		oldAsset.setType(asset.getType());
		oldAsset.setLocation(asset.getLocation());
		oldAsset.setStatus(asset.getStatus());
		oldAsset.setInstalledAt(asset.getInstalledAt());
		
		Asset dto = assetRepository.save(oldAsset);
		
		return AssetMapper.toResponse(dto);
	}

}