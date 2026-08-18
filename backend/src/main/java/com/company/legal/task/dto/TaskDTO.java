package com.company.legal.task.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskDTO {
    private String id;
    private String taskNumber;
    private String caseId;
    private String caseNumber;
    private String clientId;
    private String title;
    private String description;
    private String taskType;
    private String priority;
    private String status;
    private String assignedTo;
    private String assignedToName;
    private LocalDate dueDate;
    private LocalTime dueTime;
    private BigDecimal estimatedHours;
    private BigDecimal actualHours;
    private Integer progress;
    private Boolean billable;
    private BigDecimal billingRate;
    private BigDecimal billingAmount;
    private String parentTaskId;
    private String tags;
    private String notes;
    private String completedAt;
    private String createdAt;
    private String updatedAt;
    private String createdBy;
}
