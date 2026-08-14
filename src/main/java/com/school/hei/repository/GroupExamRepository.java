package com.school.hei.repository;

import com.school.hei.entity.JGroupExam;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface GroupExamRepository extends JpaRepository<JGroupExam, UUID> {
  Optional<JGroupExam> findByGroup_IdAndExam_Id(UUID groupId, UUID examId);
}

