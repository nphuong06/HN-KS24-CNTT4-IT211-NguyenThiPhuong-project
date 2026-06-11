package org.example.project.service;

import lombok.RequiredArgsConstructor;
import org.example.project.dto.GradeRequest;
import org.example.project.dto.GradeResponse;
import org.example.project.entity.Submission;
import org.example.project.entity.SubmissionStatus;
import org.example.project.entity.User;
import org.example.project.exception.InvalidStateException;
import org.example.project.repository.SubmissionRepository;
import org.example.project.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class GradeService {

    private final SubmissionRepository submissionRepository;

    public GradeResponse gradeSubmission(GradeRequest request) {
        Submission submission = submissionRepository.findById(request.getSubmissionId())
                .orElseThrow(() -> new InvalidStateException("Không tìm thấy bài nộp"));

        if (submission.getStatus() != SubmissionStatus.SUBMITTED
                && submission.getStatus() != SubmissionStatus.LATE) {
            throw new InvalidStateException("Sinh viên chưa nộp bài hoặc bài nộp chưa ở trạng thái hợp lệ");
        }

        User lecturer = SecurityUtils.getCurrentUser();
        submission.setScore(request.getScore());
        submission.setFeedback(request.getFeedback());
        submission.setStatus(SubmissionStatus.GRADED);
        submission.setGradedBy(lecturer);
        submission.setGradedAt(LocalDateTime.now());

        Submission saved = submissionRepository.save(submission);
        return toResponse(saved);
    }

    private GradeResponse toResponse(Submission submission) {
        GradeResponse response = new GradeResponse();
        response.setSubmissionId(submission.getId());
        response.setStudentId(submission.getStudent().getId());
        response.setStudentName(submission.getStudent().getFullName());
        response.setScore(submission.getScore());
        response.setFeedback(submission.getFeedback());
        response.setStatus(submission.getStatus());
        if (submission.getGradedBy() != null) {
            response.setGradedById(submission.getGradedBy().getId());
        }
        response.setGradedAt(submission.getGradedAt());
        return response;
    }
}
