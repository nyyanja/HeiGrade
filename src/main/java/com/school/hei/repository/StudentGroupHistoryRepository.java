package com.school.hei.repository;

import com.school.hei.entity.JStudentGroupHistory;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentGroupHistoryRepository extends JpaRepository<JStudentGroupHistory, UUID> {

  List<JStudentGroupHistory> findByStudent_Id(UUID studentId);

  List<JStudentGroupHistory> findByGroup_Id(UUID groupId);

  Optional<JStudentGroupHistory> findByStudent_IdAndEndDateIsNull(UUID studentId);
}
