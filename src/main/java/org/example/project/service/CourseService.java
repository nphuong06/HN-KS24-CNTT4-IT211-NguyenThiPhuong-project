package org.example.project.service;

import lombok.RequiredArgsConstructor;
import org.example.project.entity.Course;
import org.example.project.exception.ApiException;
import org.example.project.repository.CourseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;

    public Page<Course> getCourses(String keyword, Pageable pageable) {
        if (keyword != null && !keyword.isBlank()) {
            return courseRepository.findByCourseNameContainingIgnoreCase(keyword, pageable);
        }
        return courseRepository.findAll(pageable);
    }

    public Course getCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy khóa học", HttpStatus.NOT_FOUND));
    }

    public Course createCourse(Course course) {
        return courseRepository.save(course);
    }

    public Course updateCourse(Long id, Course request) {
        Course course = getCourseById(id);
        course.setCourseName(request.getCourseName());
        return courseRepository.save(course);
    }

    public void deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new ApiException("Không tìm thấy khóa học", HttpStatus.NOT_FOUND);
        }
        courseRepository.deleteById(id);
    }
}
