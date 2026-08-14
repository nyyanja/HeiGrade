package com.school.hei.repository;

import com.school.hei.entity.JTeacherCourse;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherCourseRepository extends JpaRepository<JTeacherCourse, UUID> {}
