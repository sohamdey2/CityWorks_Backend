package com.cts.controller;

import java.util.List;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
import com.cts.dto.request.CreateAssetInspectionRequestDTO;
import com.cts.dto.response.AssetInspectionResponseDTO;
import com.cts.service.AssetInspectionService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/inspection_record")
@Validated
public class AssetInspectionController {
	
	@Autowired private AssetInspectionService assetInspectionService;
	
	@PreAuthorize("hasRole('SUPERVISOR')")
	@PostMapping
	public ResponseEntity<?> createAssetInspection(@RequestBody @Valid CreateAssetInspectionRequestDTO assetInspection){
		AssetInspectionResponseDTO inspection = assetInspectionService.createAssetInspection(assetInspection);
		
		return ResponseEntity.ok(ApiResponse.<AssetInspectionResponseDTO>builder()
				.status("SUCCESS")
				.message("Asset Inspection Created")
				.data(inspection)
				.build()
		);
	}
	
	@PreAuthorize("hasRole('SUPERVISOR') or hasRole('ADMIN')")
	@GetMapping
	public ResponseEntity<?> getAllAssetInspection(){
		List<AssetInspectionResponseDTO> inspections = assetInspectionService.getAllAssetInspection();
		
		return ResponseEntity.ok(ApiResponse.<List<AssetInspectionResponseDTO>>builder()
				.status("FOUND")
				.message("Asset Inspections Retrived")
				.data(inspections)
				.build()
				);
	}
	
	@PreAuthorize("hasRole('SUPERVISOR') or hasRole('ADMIN')")
	@GetMapping("/{id}")
	public ResponseEntity<?> getAssetInspectionById(@PathVariable @Positive(message = "id Should be Positive") Long id){
		AssetInspectionResponseDTO inspection = assetInspectionService.getAssetInspectionById(id);
		
		return ResponseEntity.ok(ApiResponse.<AssetInspectionResponseDTO>builder()
				.status("FOUND")
				.message("Asset Inspection Retrived")
				.data(inspection)
				.build()
				);
	}
	
	@PreAuthorize("hasRole('SUPERVISOR') or hasRole('ADMIN')")
	@PutMapping("/{id}")
	public ResponseEntity<?> updateAssetInspectionById(@PathVariable @Positive(message = "id should be Positive") Long id, @RequestBody @Valid CreateAssetInspectionRequestDTO assetInspection){
		AssetInspectionResponseDTO inspection = assetInspectionService.updateAssetInspectionById(id, assetInspection);
		
		return ResponseEntity.ok(ApiResponse.<AssetInspectionResponseDTO>builder()
				.status("SUCCESS")
				.message("Asset Inspection Modified")
				.data(inspection)
				.build()
				);
	}
	
	@PreAuthorize("hasRole('SUPERVISOR') or hasRole('ADMIN')")
	@PatchMapping("/{id}")
	public ResponseEntity<?> updateAssetInspectionPartiallyById(@PathVariable @Positive(message = "id should be Positive") Long id, @RequestBody @Valid Map<String, Object> field){
		AssetInspectionResponseDTO inspection = assetInspectionService.updateAssetInspectionPartiallyById(id, field);
		
		return ResponseEntity.ok(ApiResponse.<AssetInspectionResponseDTO>builder()
				.status("SUCCESS")
				.message("Asset Inspection Modified")
				.data(inspection)
				.build()
				);
	}
	
	@PreAuthorize("hasRole('SUPERVISOR') or hasRole('ADMIN')")
	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteAssetById(@PathVariable @Positive(message = "id should be Positive") Long id){
		boolean flag = assetInspectionService.deleteAssetById(id);
		
		if(flag) {
			return ResponseEntity.ok(ApiResponse.<AssetInspectionResponseDTO>builder()
					.status("SUCCESS")
					.message("Deleted Successfully")
					.build()
					);
		}
		
		else {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something Error Happend");
		}
		
		
	}

}
