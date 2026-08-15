package com.school.hei.repository;

import com.school.hei.entity.JTeacherCourse;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherCourseRepository extends JpaRepository<JTeacherCourse, UUID> {
  Optional<JTeacherCourse> findByTeacher_IdAndCourse_Id(UUID teacherId, UUID courseId);

  List<JTeacherCourse> findByCourse_Id(UUID courseId);

  void deleteByCourse_Id(UUID courseId);
}
