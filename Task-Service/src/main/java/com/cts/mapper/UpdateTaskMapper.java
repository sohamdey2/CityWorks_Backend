package com.cts.mapper;

import com.cts.dto.request.UpdateTaskDto;
import com.cts.entity.Task;

public class UpdateTaskMapper {
    private UpdateTaskMapper(){

    }

    public static void toEntity(Task task, UpdateTaskDto dto){
        if (dto == null || task == null){
            return;
        }

        if(dto.getWorkOrderId() != null){
            task.setWorkOrder(dto.getWorkOrderId());
        }

        if(dto.getDescription() != null){
            task.setDescription(dto.getDescription());
        }

        if(dto.getAssignedTo() != null){
            task.setAssignedTo(dto.getAssignedTo());
        }

        if(dto.getDueDate() != null){
            task.setDueDate(dto.getDueDate());
        }

        if(dto.getStatus() != null) {
            task.setStatus(dto.getStatus());
        }
    }
}
