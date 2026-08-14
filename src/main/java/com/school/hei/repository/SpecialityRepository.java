package com.school.hei.repository;

import com.school.hei.entity.JSpeciality;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpecialityRepository extends JpaRepository<JSpeciality, UUID> {}
