package com.school.hei.repository;

import com.school.hei.entity.JStudent;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<JStudent, UUID> {}