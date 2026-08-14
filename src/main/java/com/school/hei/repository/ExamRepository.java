package com.school.hei.repository;

import com.school.hei.entity.JExam;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExamRepository extends JpaRepository<JExam, UUID> {}