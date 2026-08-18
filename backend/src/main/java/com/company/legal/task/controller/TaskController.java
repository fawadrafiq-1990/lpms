package com.company.legal.task.controller;

import com.company.legal.common.dto.ApiResponse;
import com.company.legal.common.dto.PageResponse;
import com.company.legal.task.dto.TaskDTO;
import com.company.legal.task.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/v1/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Task management APIs")
public class TaskController {
    private final TaskService taskService;

    @PostMapping
    @Operation(summary = "Create a new task")
    public ResponseEntity<ApiResponse<TaskDTO>> createTask(@Valid @RequestBody TaskDTO dto) {
        TaskDTO created = taskService.createTask(dto);
        return ResponseEntity.ok(ApiResponse.success(created, "Task created successfully"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a task")
    public ResponseEntity<ApiResponse<TaskDTO>> updateTask(@PathVariable String id, @Valid @RequestBody TaskDTO dto) {
        TaskDTO updated = taskService.updateTask(id, dto);
        return ResponseEntity.ok(ApiResponse.success(updated, "Task updated successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get task by ID")
    public ResponseEntity<ApiResponse<TaskDTO>> getTask(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(taskService.getTaskById(id)));
    }

    @GetMapping("/case/{caseId}")
    @Operation(summary = "Get tasks by case ID")
    public ResponseEntity<ApiResponse<List<TaskDTO>>> getTasksByCase(@PathVariable String caseId) {
        return ResponseEntity.ok(ApiResponse.success(taskService.getTasksByCase(caseId)));
    }

    @GetMapping
    @Operation(summary = "Get tasks with pagination")
    public ResponseEntity<ApiResponse<PageResponse<TaskDTO>>> getTasks(
            @RequestParam String assignedTo,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(ApiResponse.success(taskService.getTasks(assignedTo, page, size, status)));
    }

    @GetMapping("/pending")
    @Operation(summary = "Get pending tasks for user")
    public ResponseEntity<ApiResponse<List<TaskDTO>>> getPendingTasks(@RequestParam String assignedTo) {
        return ResponseEntity.ok(ApiResponse.success(taskService.getPendingTasks(assignedTo)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a task")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable String id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Task deleted successfully"));
    }
}
