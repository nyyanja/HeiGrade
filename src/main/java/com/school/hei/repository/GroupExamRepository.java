package com.school.hei.repository;

import com.school.hei.entity.JGroupExam;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupExamRepository extends JpaRepository<JGroupExam, UUID> {}