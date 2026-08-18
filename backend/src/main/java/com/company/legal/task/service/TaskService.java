package com.company.legal.task.service;

import com.company.legal.common.dto.PageResponse;
import com.company.legal.common.exception.ResourceNotFoundException;
import com.company.legal.task.dto.TaskDTO;
import com.company.legal.task.entity.Task;
import com.company.legal.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Transactional
    public TaskDTO createTask(TaskDTO dto) {
        Task task = mapToEntity(dto);
        task = taskRepository.save(task);
        return mapToDTO(task);
    }

    @Transactional
    public TaskDTO updateTask(String id, TaskDTO dto) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));
        
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setPriority(dto.getPriority());
        task.setStatus(dto.getStatus());
        task.setDueDate(dto.getDueDate());
        task.setProgress(dto.getProgress() != null ? dto.getProgress() : 0);
        task.setNotes(dto.getNotes());
        
        task = taskRepository.save(task);
        return mapToDTO(task);
    }

    @Transactional(readOnly = true)
    public TaskDTO getTaskById(String id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));
        return mapToDTO(task);
    }

    @Transactional(readOnly = true)
    public List<TaskDTO> getTasksByCase(String caseId) {
        return taskRepository.findByCaseId(caseId).stream()
                .map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageResponse<TaskDTO> getTasks(String assignedTo, Integer page, Integer size, String status) {
        Pageable pageable = PageRequest.of(page != null ? page : 0, size != null ? size : 20,
                Sort.by("createdAt").descending());
        
        Page<Task> taskPage = taskRepository.findByAssignedTo(assignedTo, pageable);
        return PageResponse.from(taskPage.map(this::mapToDTO));
    }

    @Transactional(readOnly = true)
    public List<TaskDTO> getPendingTasks(String assignedTo) {
        return taskRepository.findPendingTasksByAssignee(assignedTo).stream()
                .map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional
    public void deleteTask(String id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));
        taskRepository.delete(task);
    }

    private Task mapToEntity(TaskDTO dto) {
        return Task.builder()
                .id(dto.getId())
                .taskNumber(dto.getTaskNumber() != null ? dto.getTaskNumber() : "TASK-" + System.currentTimeMillis())
                .caseId(dto.getCaseId())
                .caseNumber(dto.getCaseNumber())
                .clientId(dto.getClientId())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .taskType(dto.getTaskType())
                .priority(dto.getPriority() != null ? dto.getPriority() : "MEDIUM")
                .status(dto.getStatus() != null ? dto.getStatus() : "TODO")
                .assignedTo(dto.getAssignedTo())
                .assignedToName(dto.getAssignedToName())
                .dueDate(dto.getDueDate())
                .dueTime(dto.getDueTime())
                .estimatedHours(dto.getEstimatedHours())
                .actualHours(dto.getActualHours())
                .progress(dto.getProgress() != null ? dto.getProgress() : 0)
                .billable(dto.getBillable() != null ? dto.getBillable() : true)
                .billingRate(dto.getBillingRate())
                .billingAmount(dto.getBillingAmount())
                .parentTaskId(dto.getParentTaskId())
                .tags(dto.getTags())
                .notes(dto.getNotes())
                .build();
    }

    private TaskDTO mapToDTO(Task task) {
        return TaskDTO.builder()
                .id(task.getId())
                .taskNumber(task.getTaskNumber())
                .caseId(task.getCaseId())
                .caseNumber(task.getCaseNumber())
                .clientId(task.getClientId())
                .title(task.getTitle())
                .description(task.getDescription())
                .taskType(task.getTaskType())
                .priority(task.getPriority())
                .status(task.getStatus())
                .assignedTo(task.getAssignedTo())
                .assignedToName(task.getAssignedToName())
                .dueDate(task.getDueDate())
                .dueTime(task.getDueTime())
                .estimatedHours(task.getEstimatedHours())
                .actualHours(task.getActualHours())
                .progress(task.getProgress())
                .billable(task.getBillable())
                .billingRate(task.getBillingRate())
                .billingAmount(task.getBillingAmount())
                .parentTaskId(task.getParentTaskId())
                .tags(task.getTags())
                .notes(task.getNotes())
                .completedAt(task.getCompletedAt() != null ? task.getCompletedAt().format(DATE_FORMATTER) : null)
                .createdAt(task.getCreatedAt() != null ? task.getCreatedAt().format(DATE_FORMATTER) : null)
                .updatedAt(task.getUpdatedAt() != null ? task.getUpdatedAt().format(DATE_FORMATTER) : null)
                .createdBy(task.getCreatedBy())
                .build();
    }
}
