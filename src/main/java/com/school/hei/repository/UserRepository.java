package com.school.hei.repository;

import com.school.hei.entity.JUser;
import com.school.hei.enums.Role;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<JUser, UUID> {
  Optional<JUser> findByEmailIgnoreCase(String email);

  List<JUser> findByRole(Role role);

  List<JUser> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
      String firstName, String lastName);
}

