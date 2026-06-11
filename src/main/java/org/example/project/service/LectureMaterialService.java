package org.example.project.service;

import lombok.RequiredArgsConstructor;
import org.example.project.dto.LectureMaterialResponse;
import org.example.project.entity.Course;
import org.example.project.entity.LectureMaterial;
import org.example.project.entity.User;
import org.example.project.exception.ApiException;
import org.example.project.repository.CourseRepository;
import org.example.project.repository.EnrollmentRepository;
import org.example.project.repository.LectureMaterialRepository;
import org.example.project.security.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class LectureMaterialService {

    private final LectureMaterialRepository lectureMaterialRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CloudinaryService cloudinaryService;

    public LectureMaterialResponse uploadMaterial(Long courseId, String title, MultipartFile file) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ApiException("Không tìm thấy khóa học", HttpStatus.NOT_FOUND));

        String fileUrl = cloudinaryService.upload(file, "lecture-materials");
        User lecturer = SecurityUtils.getCurrentUser();

        LectureMaterial material = new LectureMaterial();
        material.setCourse(course);
        material.setTitle(title != null && !title.isBlank() ? title : file.getOriginalFilename());
        material.setFileUrl(fileUrl);
        material.setFileType(file.getContentType());
        material.setUploadedBy(lecturer);

        return toResponse(lectureMaterialRepository.save(material));
    }

    public List<LectureMaterialResponse> getMaterialsByCourse(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ApiException("Không tìm thấy khóa học", HttpStatus.NOT_FOUND);
        }

        return lectureMaterialRepository.findByCourseId(courseId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<LectureMaterialResponse> getMaterialsForStudent(Long courseId) {
        User student = SecurityUtils.getCurrentUser();
        if (!enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), courseId)) {
            throw new ApiException("Sinh viên chưa đăng ký khóa học này", HttpStatus.FORBIDDEN);
        }
        return getMaterialsByCourse(courseId);
    }

    public void deleteMaterial(Long materialId) {
        if (!lectureMaterialRepository.existsById(materialId)) {
            throw new ApiException("Không tìm thấy tài liệu", HttpStatus.NOT_FOUND);
        }
        lectureMaterialRepository.deleteById(materialId);
    }

    private LectureMaterialResponse toResponse(LectureMaterial material) {
        LectureMaterialResponse response = new LectureMaterialResponse();
        response.setId(material.getId());
        response.setCourseId(material.getCourse().getId());
        response.setTitle(material.getTitle());
        response.setFileUrl(material.getFileUrl());
        response.setFileType(material.getFileType());
        if (material.getUploadedBy() != null) {
            response.setUploadedById(material.getUploadedBy().getId());
            response.setUploadedByName(material.getUploadedBy().getFullName());
        }
        response.setUploadedAt(material.getUploadedAt());
        return response;
    }
}
