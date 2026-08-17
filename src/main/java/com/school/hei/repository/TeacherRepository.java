package com.school.hei.repository;

import com.school.hei.entity.JTeacher;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TeacherRepository extends JpaRepository<JTeacher, UUID> {
  List<JTeacher> findBySpecialityContainingIgnoreCase(String speciality);

  List<JTeacher> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
      String firstName, String lastName);

  @Query(
      """
      SELECT DISTINCT tc.teacher FROM JTeacherCourse tc
      WHERE tc.course.id = :courseId
      """)
  List<JTeacher> findByCourseId(@Param("courseId") UUID courseId);
}


