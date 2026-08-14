package com.school.hei.repository;

import com.school.hei.entity.JGrade;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GradeRepository extends JpaRepository<JGrade, UUID> {}
