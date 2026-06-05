package com.cts.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cts.aspect.audit.Auditable;
import com.cts.dto.request.CreateTaskDto;
import com.cts.dto.request.UpdateTaskDto;
import com.cts.dto.response.TaskResponseDTO;
import com.cts.entity.Task;
import com.cts.exception.ResourceNotFoundException;
import com.cts.exception.ServiceUnavailableException;
import com.cts.mapper.CreateTaskMapper;
import com.cts.mapper.ResponseTaskMapper;
import com.cts.mapper.UpdateTaskMapper;
import com.cts.repository.TaskRepository;
import com.cts.service.TaskService;
import com.cts.service.WorkOrderClient;
import com.cts.service.WorkerClient;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
 
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
 
    private final WorkOrderClient workOrderClient;

    private final WorkerClient workerClient;
    
    @Auditable(action = "CREATE", resourceType = "TASK")
    @CircuitBreaker(name="Task-Service", fallbackMethod = "handleFallback")
    @Retry(name="Task-Service")
    @Override
    public TaskResponseDTO createTask(CreateTaskDto createTaskDto) {
        workOrderClient.getWorkOrder(createTaskDto.getWorkOrderId()).orElseThrow(()-> new ResourceNotFoundException("Work order with id " + createTaskDto.getWorkOrderId() + " not found"));
 
        workerClient.getWorker(createTaskDto.getAssignedTo());
 
        Task task = CreateTaskMapper.toEntity(createTaskDto);
        taskRepository.save(task);
 
        return ResponseTaskMapper.toResponseDto(task);
    }
  
    @Override
    public TaskResponseDTO findTaskById(Long id){
        Task task = taskRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Task with id " + id + " not found"));
        return ResponseTaskMapper.toResponseDto(task);
    }
    
    @Override
    public Task findTaskEntityById(Long id) {
        return taskRepository.findByTaskIdAndDeletedFalse(id).orElseThrow(() -> new ResourceNotFoundException("Task with id " + id + " not found"));
    }
 
    @Override
    public List<TaskResponseDTO> findAllTasks() {
 
        List<Task> tasks = taskRepository.findAllByDeletedFalse().orElseThrow(() -> new ResourceNotFoundException("No tasks found"));
        return ResponseTaskMapper.toResponseDtoList(tasks);
    }
 
    @Auditable(action = "UPDATE", resourceType = "TASK")
    @CircuitBreaker(name="Task-Service", fallbackMethod = "handleUpdateFallback")
    @Override
    public TaskResponseDTO updateTaskById(Long id, UpdateTaskDto dto){
 
        Task existingTask = findTaskEntityById(id);
        if(dto.getAssignedTo() != null) {
            workerClient.getWorker(dto.getAssignedTo());
        }
        if(dto.getWorkOrderId() != null) {
        	workOrderClient.getWorkOrder(dto.getWorkOrderId()).orElseThrow(()-> new ResourceNotFoundException("Work order with id " + dto.getWorkOrderId() + " not found"));
        }
        UpdateTaskMapper.toEntity(existingTask, dto);
 
        return ResponseTaskMapper.toResponseDto(taskRepository.save(existingTask));
    }
    //How to tackle empty string for workorder
    //Why is 500 error is coming for invalid token
    @Auditable(action = "DELETE", resourceType = "TASK")
    @Override
    @Transactional
    public void deleteTask(Long id) {
        Task task = findTaskEntityById(id);
        if(task.isDeleted()){
            throw new ResourceNotFoundException("Task with id " + id + " does not exist");
        }
        task.setDeleted(true);
        taskRepository.save(task);
    }
 
    public TaskResponseDTO handleFallback(CreateTaskDto createTaskDto, Throwable e) {
        System.out.println("Fallback triggered with exception: " + e.getClass().getName() + " - " + e.getMessage());
        System.out.println("Exception cause: " + (e.getCause() != null ? e.getCause().getClass().getName() : "null"));
 
        // Check if it's a NoFallbackAvailableException and extract the cause
        Throwable cause = e.getCause();
        if (cause != null && cause instanceof FeignException feignEx) {
            System.out.println("Root cause FeignException status: " + feignEx.status());
            if (feignEx.status() == 404) {
                // Check which service failed - Worker or WorkOrder
                if (feignEx.request() != null && feignEx.request().url().contains("/users/workers/")) {
                    throw new ResourceNotFoundException(
                            "Worker with id " + createTaskDto.getAssignedTo() + " not found");
                } else if (feignEx.request() != null && feignEx.request().url().contains("/workorders/")) {
                    throw new ResourceNotFoundException(
                            "Work order with id " + createTaskDto.getWorkOrderId() + " not found");
                }
            }
        }
 
        // Handle direct FeignException
        if (e instanceof FeignException feignEx) {
            System.out.println("Direct FeignException status: " + feignEx.status());
            if (feignEx.status() == 404) {
                if (feignEx.request() != null && feignEx.request().url().contains("/users/workers/")) {
                    throw new ResourceNotFoundException(
                            "Worker with id " + createTaskDto.getAssignedTo() + " not found");
                } else if (feignEx.request() != null && feignEx.request().url().contains("/workorders/")) {
                    throw new ResourceNotFoundException(
                            "Work order with id " + createTaskDto.getWorkOrderId() + " not found");
                }
            }
        }
 
        throw new ServiceUnavailableException(
                "Task service is currently unavailable. Cause: " + e.getClass().getSimpleName());
    }
    
    public TaskResponseDTO handleUpdateFallback(Long id, UpdateTaskDto dto, Throwable e) {
        System.out.println("Fallback triggered with exception: " + e.getClass().getName() + " - " + e.getMessage());
        System.out.println("Exception cause: " + (e.getCause() != null ? e.getCause().getClass().getName() : "null"));
 
        // Check if it's a NoFallbackAvailableException and extract the cause
        Throwable cause = e.getCause();
        if (cause != null && cause instanceof FeignException feignEx) {
            System.out.println("Root cause FeignException status: " + feignEx.status());
            if (feignEx.status() == 404) {
                // Check which service failed - Worker or WorkOrder
                if (feignEx.request() != null && feignEx.request().url().contains("/users/workers/")) {
                    throw new ResourceNotFoundException(
                            "Worker with id " + dto.getAssignedTo() + " not found");
                } else if (feignEx.request() != null && feignEx.request().url().contains("/workorders/")) {
                    throw new ResourceNotFoundException(
                            "Work order with id " + dto.getWorkOrderId() + " not found");
                }
            }
        }
 
        // Handle direct FeignException
        if (e instanceof FeignException feignEx) {
            System.out.println("Direct FeignException status: " + feignEx.status());
            if (feignEx.status() == 404) {
                if (feignEx.request() != null && feignEx.request().url().contains("/users/workers/")) {
                    throw new ResourceNotFoundException(
                            "Worker with id " + dto.getAssignedTo() + " not found");
                } else if (feignEx.request() != null && feignEx.request().url().contains("/workorders/")) {
                    throw new ResourceNotFoundException(
                            "Work order with id " + dto.getWorkOrderId() + " not found");
                }
            }
        }
 
        throw new ServiceUnavailableException(
                "Task service is currently unavailable. Cause: " + e.getClass().getSimpleName());
    }

    
	@Override
	public List<TaskResponseDTO> findTaskByWorkerId(Long workerId) {
		List<Task> tasks = taskRepository.findAllByAssignedTo(workerId);
		if(tasks.isEmpty()) {
			throw new ResourceNotFoundException("No Task For this Worker");
		}
		return ResponseTaskMapper.toResponseDtoList(tasks);
	}
}
