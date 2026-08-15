package com.school.hei.repository;

import com.school.hei.entity.JGroup;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GroupRepository extends JpaRepository<JGroup, UUID> {

  List<JGroup> findBySpeciality_Id(UUID specialityId);

  @Query(
      """
      SELECT DISTINCT ge.group FROM JGroupExam ge
      WHERE ge.exam.id = :examId
      """)
  List<JGroup> findByExamId(@Param("examId") UUID examId);

  @Query(
      """
      SELECT DISTINCT g FROM JGroup g
      JOIN JSpecialityCourse sc ON sc.speciality = g.speciality
      WHERE sc.course.id = :courseId
      """)
  List<JGroup> findByCourseId(@Param("courseId") UUID courseId);

  List<JGroup> findByNameContainingIgnoreCase(String name);
}
