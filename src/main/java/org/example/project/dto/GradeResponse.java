package org.example.project.dto;

import lombok.Data;
import org.example.project.entity.SubmissionStatus;

import java.time.LocalDateTime;

@Data
public class GradeResponse {

    private Long submissionId;
    private Long studentId;
    private String studentName;
    private Integer score;
    private String feedback;
    private SubmissionStatus status;
    private Long gradedById;
    private LocalDateTime gradedAt;
}
