package com.school.hei.repository;

import com.school.hei.entity.JAdmin;
import com.school.hei.entity.JUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface AdminRepository extends JpaRepository<JAdmin, UUID> {
  Optional<JUser> findByEmailIgnoreCase(String email);
}

