package com.school.hei.repository;

import com.school.hei.entity.JStudentGroupHistory;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudentGroupHistoryRepository extends JpaRepository<JStudentGroupHistory, UUID> {

  List<JStudentGroupHistory> findByStudent_Id(UUID studentId);

  List<JStudentGroupHistory> findByGroup_Id(UUID groupId);

  Optional<JStudentGroupHistory> findByStudent_IdAndEndDateIsNull(UUID studentId);

  @Query(
      """
      SELECT h FROM JStudentGroupHistory h
      WHERE h.student.id = :studentId
        AND h.startDate <= :date
        AND (h.endDate IS NULL OR h.endDate >= :date)
      ORDER BY h.startDate DESC
      """)
  Optional<JStudentGroupHistory> findStudentGroupAtDate(
      @Param("studentId") UUID studentId, @Param("date") LocalDate date);
}

