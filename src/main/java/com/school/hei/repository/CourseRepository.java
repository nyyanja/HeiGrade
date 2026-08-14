package com.school.hei.repository;

import java.util.Optional;
import java.util.UUID;
import com.school.hei.entity.JCourse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<JCourse, UUID> {
  Optional<JCourse> findByReferenceIgnoreCase(String reference);
}
