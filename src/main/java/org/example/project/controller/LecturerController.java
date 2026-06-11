package org.example.project.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.project.dto.ApiResponse;
import org.example.project.dto.AssignmentRequest;
import org.example.project.dto.AssignmentResponse;
import org.example.project.dto.GradeRequest;
import org.example.project.dto.GradeResponse;
import org.example.project.dto.LectureMaterialResponse;
import org.example.project.service.AssignmentService;
import org.example.project.service.GradeService;
import org.example.project.service.LectureMaterialService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lecturer")
@RequiredArgsConstructor
public class LecturerController {

    private final GradeService gradeService;
    private final AssignmentService assignmentService;
    private final LectureMaterialService lectureMaterialService;

    @PostMapping("/grades")
    public ApiResponse<GradeResponse> gradeSubmission(@Valid @RequestBody GradeRequest request) {
        return ApiResponse.success("Chấm điểm thành công", gradeService.gradeSubmission(request));
    }

    @PutMapping("/grades")
    public ApiResponse<GradeResponse> updateGrade(@Valid @RequestBody GradeRequest request) {
        return ApiResponse.success("Cập nhật điểm thành công", gradeService.gradeSubmission(request));
    }

    @PostMapping("/assignments")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AssignmentResponse> createAssignment(@Valid @RequestBody AssignmentRequest request) {
        return ApiResponse.success("Tạo bài tập thành công", assignmentService.createAssignment(request));
    }

    @GetMapping("/courses/{courseId}/assignments")
    public ApiResponse<List<AssignmentResponse>> getAssignments(@PathVariable Long courseId) {
        return ApiResponse.success(assignmentService.getAssignmentsByCourse(courseId));
    }

    @PostMapping("/courses/{courseId}/materials")
    public ApiResponse<LectureMaterialResponse> uploadMaterial(
            @PathVariable Long courseId,
            @RequestParam(required = false) String title,
            @RequestParam("file") MultipartFile file) {
        return ApiResponse.success("Tải lên tài liệu thành công",
                lectureMaterialService.uploadMaterial(courseId, title, file));
    }

    @GetMapping("/courses/{courseId}/materials")
    public ApiResponse<List<LectureMaterialResponse>> getMaterials(@PathVariable Long courseId) {
        return ApiResponse.success(lectureMaterialService.getMaterialsByCourse(courseId));
    }

    @DeleteMapping("/materials/{materialId}")
    public ApiResponse<Void> deleteMaterial(@PathVariable Long materialId) {
        lectureMaterialService.deleteMaterial(materialId);
        return ApiResponse.success("Xóa tài liệu thành công", null);
    }
}
