package com.school.hei.repository;

import com.school.hei.entity.JExam;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExamRepository extends JpaRepository<JExam, UUID> {
  List<JExam> findByCourse_Id(UUID courseId);

  List<JExam> findByTitleContainingIgnoreCase(String title);

  List<JExam> findByDate(LocalDate date);

  List<JExam> findByCoeff(Double coeff);

  @Query(
      """
      SELECT DISTINCT ge.exam FROM JGroupExam ge
      WHERE ge.group.id = :groupId
      """)
  List<JExam> findByGroupId(@Param("groupId") UUID groupId);

  @Query(
      """
      SELECT DISTINCT ge.exam FROM JGroupExam ge
      WHERE ge.group.speciality.id = :specialityId
      """)
  List<JExam> findBySpecialityId(@Param("specialityId") UUID specialityId);
}


