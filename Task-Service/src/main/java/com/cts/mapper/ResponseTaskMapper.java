package com.cts.mapper;
import com.cts.dto.response.TaskResponseDTO;
import com.cts.entity.Task;

import java.util.List;

public class ResponseTaskMapper {
    private ResponseTaskMapper() {

    }
    public static TaskResponseDTO toResponseDto(Task task) {
        if (task == null){
            return null;
        }
        return TaskResponseDTO.builder()
                .taskId(task.getTaskId())
                .description(task.getDescription())
                .assignedTo(task.getAssignedTo())
                .dueDate(task.getDueDate())
                .status(task.getStatus())
                .workOrderId(task.getWorkOrder())
                .build();
    }

    public static List<TaskResponseDTO> toResponseDtoList(List<Task> tasks) {
        return tasks.stream()
                .map(ResponseTaskMapper::toResponseDto)
                .toList();
    }
}
