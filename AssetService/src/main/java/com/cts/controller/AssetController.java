package com.cts.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cts.api.ApiResponse;
import com.cts.dto.request.CreateAssetRequestDTO;
import com.cts.dto.response.AssetResponseDTO;
import com.cts.service.AssetService;

import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/assets")
@Validated
public class AssetController{
	
	@Autowired
	AssetService assetService;
	
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR', 'CITIZEN')")
	@GetMapping
	public ResponseEntity<?> getAllAssets(){
		List<AssetResponseDTO> assets = assetService.getAllAssets();
		return ResponseEntity.ok(ApiResponse.<List<AssetResponseDTO>>builder()
				.status("FOUND")
				.message("Assets Retrieved")
				.data(assets)
				.build()
		);
	}
	
	@PreAuthorize(("hasRole('ADMIN') or hasRole('SUPERVISOR') or hasRole('CITIZEN')"))
	@GetMapping("/{assetId}")
	public ResponseEntity<?> getAssetById(@PathVariable @Positive(message = "id must be Positive") Long assetId) {
		AssetResponseDTO asset = assetService.getAssetById(assetId);
		return ResponseEntity.ok(ApiResponse.<AssetResponseDTO>builder()
				.status("FOUND")
				.message("Asset Retrieved")
				.data(asset)
				.build()
				);
	}
	
	@PreAuthorize(("hasRole('ADMIN')"))
	@PostMapping
	public ResponseEntity<?> addAssets(@RequestBody CreateAssetRequestDTO assets){
		AssetResponseDTO asset = assetService.addAssets(assets);
		return ResponseEntity.ok(ApiResponse.<AssetResponseDTO>builder()
				.status("SUCCESS")
				.message("Asset Created")
				.data(asset)
				.build()
		);
	}
	
	@PreAuthorize(("hasRole('ADMIN')"))
	@PatchMapping("/{assetId}")
	public ResponseEntity<?> updateAssetPartiallyById(@PathVariable @Positive(message = "id must be Positive") Long assetId, @RequestBody Map<String,Object> field) {
		AssetResponseDTO asset = assetService.updateAssetPartiallyById(assetId, field);
		return ResponseEntity.ok(ApiResponse.<AssetResponseDTO>builder()
				.status("SUCCESS")
				.message("Asset Modified")
				.data(asset)
				.build()
		);
	}
	
	@PreAuthorize(("hasRole('ADMIN')"))
	@DeleteMapping("/{assetId}")
	public ResponseEntity<?> deleteAssetById(@PathVariable @Positive(message = "id must be Positive") Long assetId) {
			assetService.deleteAssetById(assetId);
			return ResponseEntity.ok(ApiResponse.<AssetResponseDTO>builder()
					.status("SUCCESS")
					.message("Asset Deleted Successfully")
					.build()
			);
	}
	
	@PreAuthorize(("hasRole('ADMIN')"))
	@PutMapping("/{assetId}")
	public ResponseEntity<?> updateAssetById(@PathVariable @Positive(message = "id must be Positive") Long assetId, @RequestBody CreateAssetRequestDTO asset) {
		AssetResponseDTO assets = assetService.updateAssetById(assetId, asset);
		return ResponseEntity.ok(ApiResponse.<AssetResponseDTO>builder()
				.status("SUCCESS")
				.message("Asset Modified")
				.data(assets)
				.build()
		);
	}

}
