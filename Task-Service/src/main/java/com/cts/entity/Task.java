package com.cts.entity;

import java.time.LocalDateTime;

import com.cts.enums.TaskStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name="task")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Task{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long taskId;

    private Long workOrder;

    private String description;

    private Long assignedTo;

    private LocalDateTime dueDate;

    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = false;

    @Column(nullable = false)
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private TaskStatus status = TaskStatus.PENDING;
}
