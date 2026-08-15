package com.school.hei.repository;

import com.school.hei.entity.JSpecialityCourse;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

public interface SpecialityCourseRepository extends JpaRepository<JSpecialityCourse, UUID> {
  Optional<JSpecialityCourse> findBySpeciality_IdAndCourse_Id(UUID specialityId, UUID courseId);

  List<JSpecialityCourse> findByCourse_Id(UUID courseId);

  @Modifying
  void deleteByCourse_Id(UUID courseId);
}
