package com.cts.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import com.cts.deserializer.StrictLongDeserializer;
import com.cts.enums.TaskStatus;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@Getter
@Setter
public class UpdateTaskDto {
	@JsonDeserialize(using = StrictLongDeserializer.class)
	@Positive(message = "Work order ID must be positive")
    private Long workOrderId;

    @Size(min = 10, max = 255, message = "Description must be between 10 and 255 characters")
    private String description;
    
    @JsonDeserialize(using = StrictLongDeserializer.class)
    @Positive(message = "AssignedTo user ID must be a positive number")
    private Long assignedTo;

    @FutureOrPresent(message = "Due date must be today or in the future")
    private LocalDateTime dueDate;

    private TaskStatus status;
}

