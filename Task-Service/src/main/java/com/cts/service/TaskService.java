package com.cts.service;

import com.cts.dto.request.CreateTaskDto;
import com.cts.dto.response.TaskResponseDTO;
import com.cts.dto.request.UpdateTaskDto;
import com.cts.entity.Task;

import java.util.List;

public interface TaskService {
    TaskResponseDTO createTask(CreateTaskDto t);
    TaskResponseDTO findTaskById(Long id);
    Task findTaskEntityById(Long id);
    List<TaskResponseDTO> findAllTasks();
    TaskResponseDTO updateTaskById(Long id, UpdateTaskDto dto);
    void deleteTask(Long id);
    List<TaskResponseDTO> findTaskByWorkerId(Long workerId);
}
