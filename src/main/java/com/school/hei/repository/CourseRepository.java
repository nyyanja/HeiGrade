package com.school.hei.repository;

import com.school.hei.entity.JCourse;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<JCourse, UUID> {}
