package com.cts.dto.response;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrderResponseDTO {
    private Long workOrderId;
    private Long requestId;
    private Long assetId;
    private Long assignedTo;
    private LocalDateTime assignedAt;
    private LocalDateTime dueDate;
    private String status;
}