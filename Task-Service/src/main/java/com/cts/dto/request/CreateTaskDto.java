package com.cts.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateTaskDto {

    @NotNull(message = "Work order ID must not be null")
    @Positive(message = "Work order ID must be a poitive number")
    private Long workOrderId;

    @NotBlank(message = "Description must not be blank")
    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @NotNull(message = "AssignedTo user ID must not be null")
    @Positive(message="AssignedTo user ID must be a positive number")
    private Long assignedTo;

    @NotNull(message = "Due date must not be null")
    @FutureOrPresent(message = "Due date must be today or in the future")
    private LocalDateTime dueDate;

}
