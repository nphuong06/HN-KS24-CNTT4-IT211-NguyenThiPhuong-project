package org.example.project.service;

import lombok.RequiredArgsConstructor;
import org.example.project.dto.SubmissionRequest;
import org.example.project.dto.SubmissionResponse;
import org.example.project.entity.Assignment;
import org.example.project.entity.Submission;
import org.example.project.entity.SubmissionStatus;
import org.example.project.entity.User;
import org.example.project.exception.ApiException;
import org.example.project.repository.EnrollmentRepository;
import org.example.project.repository.SubmissionRepository;
import org.example.project.security.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final AssignmentService assignmentService;
    private final EnrollmentRepository enrollmentRepository;
    private final CloudinaryService cloudinaryService;

    public SubmissionResponse submitWithGithub(SubmissionRequest request) {
        if (request.getGithubUrl() == null || request.getGithubUrl().isBlank()) {
            throw new ApiException("Link GitHub không được để trống", HttpStatus.BAD_REQUEST);
        }

        User student = SecurityUtils.getCurrentUser();
        Assignment assignment = assignmentService.getAssignmentById(request.getAssignmentId());
        validateEnrollment(student.getId(), assignment.getCourse().getId());

        Submission submission = findOrCreateSubmission(assignment, student);
        if (submission.getStatus() == SubmissionStatus.GRADED) {
            throw new ApiException("Bài nộp đã được chấm điểm, không thể cập nhật", HttpStatus.CONFLICT);
        }

        submission.setGithubUrl(request.getGithubUrl());
        updateSubmissionStatus(submission, assignment);
        return toResponse(submissionRepository.save(submission));
    }

    public SubmissionResponse submitWithFile(Long assignmentId, MultipartFile file) {
        User student = SecurityUtils.getCurrentUser();
        Assignment assignment = assignmentService.getAssignmentById(assignmentId);
        validateEnrollment(student.getId(), assignment.getCourse().getId());

        String fileUrl = cloudinaryService.upload(file, "submissions");

        Submission submission = findOrCreateSubmission(assignment, student);
        if (submission.getStatus() == SubmissionStatus.GRADED) {
            throw new ApiException("Bài nộp đã được chấm điểm, không thể cập nhật", HttpStatus.CONFLICT);
        }

        submission.setReportUrl(fileUrl);
        updateSubmissionStatus(submission, assignment);
        return toResponse(submissionRepository.save(submission));
    }

    public List<SubmissionResponse> getMySubmissions() {
        User student = SecurityUtils.getCurrentUser();
        return submissionRepository.findByStudentId(student.getId()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Submission getSubmissionById(Long id) {
        return submissionRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy bài nộp", HttpStatus.NOT_FOUND));
    }

    private Submission findOrCreateSubmission(Assignment assignment, User student) {
        return submissionRepository.findByAssignmentIdAndStudentId(assignment.getId(), student.getId())
                .orElseGet(() -> {
                    Submission newSubmission = new Submission();
                    newSubmission.setAssignment(assignment);
                    newSubmission.setStudent(student);
                    newSubmission.setStatus(SubmissionStatus.PENDING);
                    return newSubmission;
                });
    }

    private void validateEnrollment(Long studentId, Long courseId) {
        if (!enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new ApiException("Sinh viên chưa đăng ký khóa học này", HttpStatus.BAD_REQUEST);
        }
    }

    private void updateSubmissionStatus(Submission submission, Assignment assignment) {
        submission.setSubmittedAt(LocalDateTime.now());
        if (assignment.getDueDate() != null && LocalDateTime.now().isAfter(assignment.getDueDate())) {
            submission.setStatus(SubmissionStatus.LATE);
        } else {
            submission.setStatus(SubmissionStatus.SUBMITTED);
        }
    }

    private SubmissionResponse toResponse(Submission submission) {
        SubmissionResponse response = new SubmissionResponse();
        response.setId(submission.getId());
        response.setAssignmentId(submission.getAssignment().getId());
        response.setAssignmentTitle(submission.getAssignment().getTitle());
        response.setStudentId(submission.getStudent().getId());
        response.setStudentName(submission.getStudent().getFullName());
        response.setGithubUrl(submission.getGithubUrl());
        response.setReportUrl(submission.getReportUrl());
        response.setStatus(submission.getStatus());
        response.setScore(submission.getScore());
        response.setFeedback(submission.getFeedback());
        response.setSubmittedAt(submission.getSubmittedAt());
        response.setGradedAt(submission.getGradedAt());
        return response;
    }
}
