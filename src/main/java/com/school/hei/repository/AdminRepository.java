package com.school.hei.repository;

import com.school.hei.entity.JAdmin;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<JAdmin, UUID> {}