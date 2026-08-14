package com.school.hei.repository;

import com.school.hei.entity.JGradeHistory;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GradeHistoryRepository extends JpaRepository<JGradeHistory, UUID> {}