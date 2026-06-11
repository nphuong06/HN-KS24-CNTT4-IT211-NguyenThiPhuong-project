package org.example.project.service;

import lombok.RequiredArgsConstructor;
import org.example.project.dto.AssignmentRequest;
import org.example.project.dto.AssignmentResponse;
import org.example.project.entity.Assignment;
import org.example.project.entity.Course;
import org.example.project.entity.User;
import org.example.project.exception.ApiException;
import org.example.project.repository.AssignmentRepository;
import org.example.project.repository.CourseRepository;
import org.example.project.security.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final CourseRepository courseRepository;

    public AssignmentResponse createAssignment(AssignmentRequest request) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ApiException("Không tìm thấy khóa học", HttpStatus.NOT_FOUND));

        User lecturer = SecurityUtils.getCurrentUser();

        Assignment assignment = new Assignment();
        assignment.setCourse(course);
        assignment.setTitle(request.getTitle());
        assignment.setDescription(request.getDescription());
        assignment.setDueDate(request.getDueDate());
        assignment.setCreatedBy(lecturer);

        return toResponse(assignmentRepository.save(assignment));
    }

    public List<AssignmentResponse> getAssignmentsByCourse(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ApiException("Không tìm thấy khóa học", HttpStatus.NOT_FOUND);
        }

        return assignmentRepository.findByCourseId(courseId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Assignment getAssignmentById(Long id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy bài tập", HttpStatus.NOT_FOUND));
    }

    private AssignmentResponse toResponse(Assignment assignment) {
        AssignmentResponse response = new AssignmentResponse();
        response.setId(assignment.getId());
        response.setCourseId(assignment.getCourse().getId());
        response.setCourseName(assignment.getCourse().getCourseName());
        response.setTitle(assignment.getTitle());
        response.setDescription(assignment.getDescription());
        response.setDueDate(assignment.getDueDate());
        if (assignment.getCreatedBy() != null) {
            response.setCreatedById(assignment.getCreatedBy().getId());
        }
        return response;
    }
}
