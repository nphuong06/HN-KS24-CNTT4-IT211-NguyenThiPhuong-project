package org.example.project.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LectureMaterialResponse {

    private Long id;
    private Long courseId;
    private String title;
    private String fileUrl;
    private String fileType;
    private Long uploadedById;
    private String uploadedByName;
    private LocalDateTime uploadedAt;
}
