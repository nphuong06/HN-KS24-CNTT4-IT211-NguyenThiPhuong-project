package org.example.project.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.project.dto.ApiResponse;
import org.example.project.dto.EnrollmentRequest;
import org.example.project.dto.EnrollmentResponse;
import org.example.project.dto.LectureMaterialResponse;
import org.example.project.dto.SubmissionRequest;
import org.example.project.dto.SubmissionResponse;
import org.example.project.service.EnrollmentService;
import org.example.project.service.LectureMaterialService;
import org.example.project.service.SubmissionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/student")
@RequiredArgsConstructor
public class StudentController {

    private final EnrollmentService enrollmentService;
    private final SubmissionService submissionService;
    private final LectureMaterialService lectureMaterialService;

    @PostMapping("/enroll")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<EnrollmentResponse> enroll(@Valid @RequestBody EnrollmentRequest request) {
        return ApiResponse.success("Đăng ký khóa học thành công", enrollmentService.enroll(request));
    }

    @GetMapping("/{studentId}/courses")
    public ApiResponse<List<EnrollmentResponse>> getEnrolledCourses(@PathVariable Long studentId) {
        return ApiResponse.success(enrollmentService.getEnrollmentsByStudent(studentId));
    }

    @PostMapping("/submissions")
    public ApiResponse<SubmissionResponse> submitGithub(@Valid @RequestBody SubmissionRequest request) {
        return ApiResponse.success("Nộp bài thành công", submissionService.submitWithGithub(request));
    }

    @PostMapping("/submissions/upload")
    public ApiResponse<SubmissionResponse> submitFile(
            @RequestParam Long assignmentId,
            @RequestParam("file") MultipartFile file) {
        return ApiResponse.success("Nộp bài thành công", submissionService.submitWithFile(assignmentId, file));
    }

    @GetMapping("/submissions")
    public ApiResponse<List<SubmissionResponse>> getMySubmissions() {
        return ApiResponse.success(submissionService.getMySubmissions());
    }

    @GetMapping("/courses/{courseId}/materials")
    public ApiResponse<List<LectureMaterialResponse>> getCourseMaterials(@PathVariable Long courseId) {
        return ApiResponse.success(lectureMaterialService.getMaterialsForStudent(courseId));
    }
}
