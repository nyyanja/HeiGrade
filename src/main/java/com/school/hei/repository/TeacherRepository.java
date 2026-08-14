package com.school.hei.repository;

import com.school.hei.entity.JTeacher;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherRepository extends JpaRepository<JTeacher, UUID> {}
