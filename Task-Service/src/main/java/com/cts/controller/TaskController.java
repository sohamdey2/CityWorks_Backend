package com.cts.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cts.api.ApiResponse;
import com.cts.dto.request.CreateTaskDto;
import com.cts.dto.request.UpdateTaskDto;
import com.cts.dto.response.TaskResponseDTO;
import com.cts.entity.Task;
import com.cts.mapper.ResponseTaskMapper;
import com.cts.service.TaskService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
 
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {
    
    private final TaskService taskService;
 
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public ResponseEntity <ApiResponse<TaskResponseDTO>> createTask(@RequestBody @Valid CreateTaskDto createTaskDto){
        TaskResponseDTO taskResponseDto = taskService.createTask(createTaskDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<TaskResponseDTO>builder()
                        .status("Success")
                        .message("Task created successfully")
                        .data(taskResponseDto)
                        .build());
    }
 
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR', 'WORKER')")
    public ResponseEntity <ApiResponse<TaskResponseDTO>> findTaskById(@PathVariable("id") Long id){
        TaskResponseDTO taskResponseDto = taskService.findTaskById(id);
        return ResponseEntity.ok(ApiResponse.<TaskResponseDTO>builder()
                .status("Success")
                .message("Task retrieved successfully")
                .data(taskResponseDto)
                .build());
    }
 
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<List<TaskResponseDTO>>> getAllTasks() {
        List<TaskResponseDTO> tasksDto = taskService.findAllTasks();
 
        return ResponseEntity.ok(
                ApiResponse.<List<TaskResponseDTO>>builder()
                        .status("Success")
                        .message("Task retrieved successfully")
                        .data(tasksDto)
                        .build()
        );
    }
    
    @GetMapping("/worker/{workerId}")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<ApiResponse<List<TaskResponseDTO>>> findTaskByWorkerId(@PathVariable("workerId") Long workerId){
    	List<TaskResponseDTO> taskDto = taskService.findTaskByWorkerId(workerId);
    	return ResponseEntity.ok(
    			ApiResponse.<List<TaskResponseDTO>>builder()
    			.status("Success")
    			.message("Task retrieved successfully for worker")
    			.data(taskDto)
    			.build()
    	);
    }
 
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public ResponseEntity <ApiResponse<TaskResponseDTO>> updateTaskById(@PathVariable("id") Long id, @RequestBody @Valid UpdateTaskDto updateTaskDto){
        
        return ResponseEntity.ok(ApiResponse.<TaskResponseDTO>builder()
                .status("Success")
                .message("Task updated successfully")
                .data(taskService.updateTaskById(id, updateTaskDto))
                .build());
    }
 
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable Long id){
        taskService.deleteTask(id);
 
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .status("Success")
                        .message("Task deleted successfully")
                        .data(null)
                        .build()
        );
    }
    
}
