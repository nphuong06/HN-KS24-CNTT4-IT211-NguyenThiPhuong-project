package org.example.project.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EnrollmentResponse {

    private Long id;
    private Long studentId;
    private String studentName;
    private Long courseId;
    private String courseName;
    private LocalDateTime enrolledAt;
}
