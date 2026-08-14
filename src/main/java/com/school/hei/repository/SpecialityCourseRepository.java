package com.school.hei.repository;

import com.school.hei.entity.JSpecialityCourse;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpecialityCourseRepository extends JpaRepository<JSpecialityCourse, UUID> {
  Optional<JSpecialityCourse> findBySpeciality_IdAndCourse_Id(UUID specialityId, UUID courseId);
}
