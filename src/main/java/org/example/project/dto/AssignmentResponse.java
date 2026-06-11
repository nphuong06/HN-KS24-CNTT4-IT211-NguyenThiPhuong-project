package org.example.project.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AssignmentResponse {

    private Long id;
    private Long courseId;
    private String courseName;
    private String title;
    private String description;
    private LocalDateTime dueDate;
    private Long createdById;
}
