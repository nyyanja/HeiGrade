package com.school.hei.repository;

import com.school.hei.entity.JGrade;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GradeRepository extends JpaRepository<JGrade, UUID> {
  Optional<JGrade> findByStudent_IdAndExam_Id(UUID studentId, UUID examId);

  List<JGrade> findByExam_Id(UUID examId);

  List<JGrade> findByStudent_Id(UUID studentId);

  @Query(
      """
      SELECT g FROM JGrade g
      WHERE g.exam.course.id = :courseId
      """)
  List<JGrade> findByCourseId(@Param("courseId") UUID courseId);

  @Query(
      """
      SELECT g FROM JGrade g
      WHERE g.student.id = :studentId AND g.exam.course.id = :courseId
      """)
  List<JGrade> findByStudentIdAndCourseId(
      @Param("studentId") UUID studentId, @Param("courseId") UUID courseId);

  @Query(
      """
      SELECT DISTINCT g FROM JGrade g
      JOIN JGroupExam ge ON ge.exam = g.exam
      WHERE ge.group.id = :groupId
      """)
  List<JGrade> findByGroupId(@Param("groupId") UUID groupId);

  @Query(
      """
      SELECT g FROM JGrade g
      JOIN JGroupExam ge ON ge.exam = g.exam
      WHERE ge.group.id = :groupId AND g.exam.id = :examId
      """)
  List<JGrade> findByGroupIdAndExamId(@Param("groupId") UUID groupId, @Param("examId") UUID examId);

  @Query(
      """
      SELECT g FROM JGrade g
      JOIN JGroupExam ge ON ge.exam = g.exam
      WHERE ge.group.id = :groupId AND g.exam.course.id = :courseId
      """)
  List<JGrade> findByGroupIdAndCourseId(
      @Param("groupId") UUID groupId, @Param("courseId") UUID courseId);

  @Query(
      """
      SELECT DISTINCT g FROM JGrade g
      JOIN JGroupExam ge ON ge.exam = g.exam
      WHERE ge.group.speciality.id = :specialityId
      """)
  List<JGrade> findBySpecialityId(@Param("specialityId") UUID specialityId);

  @Query(
      """
      SELECT g FROM JGrade g
      JOIN JGroupExam ge ON ge.exam = g.exam
      WHERE ge.group.speciality.id = :specialityId AND g.exam.id = :examId
      """)
  List<JGrade> findBySpecialityIdAndExamId(
      @Param("specialityId") UUID specialityId, @Param("examId") UUID examId);

  @Query(
      """
      SELECT g FROM JGrade g
      JOIN JGroupExam ge ON ge.exam = g.exam
      WHERE ge.group.speciality.id = :specialityId AND g.exam.course.id = :courseId
      """)
  List<JGrade> findBySpecialityIdAndCourseId(
      @Param("specialityId") UUID specialityId, @Param("courseId") UUID courseId);

  List<JGrade> findByDate(LocalDate date);

  List<JGrade> findByValueGreaterThanEqual(Double value);
}
