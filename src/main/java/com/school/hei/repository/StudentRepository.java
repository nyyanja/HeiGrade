package com.school.hei.repository;

import com.school.hei.entity.JStudent;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<JStudent, UUID> {
  Optional<JStudent> findByReference(String reference);

  List<JStudent> findByGroup_Id(UUID groupId);

  List<JStudent> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
      String firstName, String lastName);

  List<JStudent> findByGroup_Speciality_Id(UUID specialityId);
}
