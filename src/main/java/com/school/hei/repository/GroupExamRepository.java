package com.school.hei.repository;

import com.school.hei.entity.JGroupExam;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupExamRepository extends JpaRepository<JGroupExam, UUID> {
  Optional<JGroupExam> findByGroup_IdAndExam_Id(UUID groupId, UUID examId);

  List<JGroupExam> findByExam_Id(UUID examId);

  void deleteByExam_Id(UUID examId);
}

