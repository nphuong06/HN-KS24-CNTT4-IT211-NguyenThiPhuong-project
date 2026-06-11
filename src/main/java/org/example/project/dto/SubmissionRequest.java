package org.example.project.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubmissionRequest {

    @NotNull(message = "Assignment ID không được để trống")
    private Long assignmentId;

    private String githubUrl;
}
