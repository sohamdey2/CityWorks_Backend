package com.cts.dto.request;

import java.time.LocalDate;

import com.cts.enums.MaintenanceStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateMaintenanceRecordRequestDTO {
	
	@NotBlank(message = "Task Description Should not be Empty")
	private String taskDescription;
	
	@NotBlank(message = "Performed By should be assigned")
	private String performedBy;
	
	@PastOrPresent(message = "Performed At should be Past or Present")
	private LocalDate performedAt;
	
	@PositiveOrZero(message = "Cost should be greater or Equal to 0")
	private Double cost;
	
	@NotNull(message = "Status should not be null")
	private MaintenanceStatus status;
	
	private Long assetId;

}
