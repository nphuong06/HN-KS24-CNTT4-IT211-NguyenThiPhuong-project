package org.example.project.service;

import lombok.RequiredArgsConstructor;
import org.example.project.dto.EnrollmentRequest;
import org.example.project.dto.EnrollmentResponse;
import org.example.project.entity.Course;
import org.example.project.entity.Enrollment;
import org.example.project.entity.User;
import org.example.project.exception.ApiException;
import org.example.project.repository.CourseRepository;
import org.example.project.repository.EnrollmentRepository;
import org.example.project.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    public EnrollmentResponse enroll(EnrollmentRequest request) {
        User student = userRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ApiException("Không tìm thấy sinh viên", HttpStatus.NOT_FOUND));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ApiException("Không tìm thấy khóa học", HttpStatus.NOT_FOUND));

        if (!"STUDENT".equals(student.getRole())) {
            throw new ApiException("Chỉ sinh viên mới được đăng ký khóa học", HttpStatus.BAD_REQUEST);
        }

        if (enrollmentRepository.existsByStudentIdAndCourseId(request.getStudentId(), request.getCourseId())) {
            throw new ApiException("Sinh viên đã đăng ký khóa học này", HttpStatus.CONFLICT);
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment = enrollmentRepository.save(enrollment);

        return toResponse(enrollment);
    }

    public List<EnrollmentResponse> getEnrollmentsByStudent(Long studentId) {
        if (!userRepository.existsById(studentId)) {
            throw new ApiException("Không tìm thấy sinh viên", HttpStatus.NOT_FOUND);
        }

        List<EnrollmentResponse> result = new ArrayList<>();
        for (Enrollment enrollment : enrollmentRepository.findByStudentId(studentId)) {
            result.add(toResponse(enrollment));
        }
        return result;
    }

    private EnrollmentResponse toResponse(Enrollment enrollment) {
        EnrollmentResponse response = new EnrollmentResponse();
        response.setId(enrollment.getId());
        response.setStudentId(enrollment.getStudent().getId());
        response.setStudentName(enrollment.getStudent().getFullName());
        response.setCourseId(enrollment.getCourse().getId());
        response.setCourseName(enrollment.getCourse().getCourseName());
        response.setEnrolledAt(enrollment.getEnrolledAt());
        return response;
    }
}
