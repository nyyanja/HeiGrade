package com.school.hei.repository;

import com.school.hei.entity.JCourse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseRepository extends JpaRepository<JCourse, UUID> {
  Optional<JCourse> findByReferenceIgnoreCase(String reference);
  List<JCourse> findByCredit(Integer credit);

  @Query("""
    SELECT DISTINCT tc.course FROM JTeacherCourse tc
    WHERE tc.teacher.id = :teacherId
    """)
  List<JCourse> findByTeacherId(@Param("teacherId") UUID teacherId);

  @Query("""
    SELECT DISTINCT sc.course FROM JSpecialityCourse sc
    WHERE sc.speciality.id = :specialityId
    """)
  List<JCourse> findBySpecialityId(@Param("specialityId") UUID specialityId);
}
