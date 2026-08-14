package com.school.hei.repository;

import com.school.hei.entity.JSpecialityCourse;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpecialityCourseRepository extends JpaRepository<JSpecialityCourse, UUID> {}
