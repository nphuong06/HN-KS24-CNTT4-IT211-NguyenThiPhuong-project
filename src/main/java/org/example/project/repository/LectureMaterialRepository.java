package org.example.project.repository;

import org.example.project.entity.LectureMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository

public interface LectureMaterialRepository extends JpaRepository<LectureMaterial, Long> {

    List<LectureMaterial> findByCourseId(Long courseId);
}
