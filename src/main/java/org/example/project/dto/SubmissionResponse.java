package org.example.project.dto;

import lombok.Data;
import org.example.project.entity.SubmissionStatus;

import java.time.LocalDateTime;

@Data
public class SubmissionResponse {

    private Long id;
    private Long assignmentId;
    private String assignmentTitle;
    private Long studentId;
    private String studentName;
    private String githubUrl;
    private String reportUrl;
    private SubmissionStatus status;
    private Integer score;
    private String feedback;
    private LocalDateTime submittedAt;
    private LocalDateTime gradedAt;
}
