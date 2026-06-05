package com.cts.dto.request;

import java.time.LocalDateTime;

import com.cts.enums.InspectionStatus;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateAssetInspectionRequestDTO {
	
	@PastOrPresent(message = "performed at should be Past or Present")
	@NotNull(message = "performed at should not be null")
	private LocalDateTime performedAt;
	
	
	@Min(value = 1, message = "rating should be atleast 1")
	@Max(value = 5, message = "rating should be maximum 5")
	private Integer conditionRating;
	
	@NotBlank(message = "findings should not be null")
	private String findings;
	
	@NotBlank(message = "URI should not be null")
	private String photoUri;
	
	@NotNull(message = "Status should not be null")
	private InspectionStatus status;
	
	private Long assetId;

}
