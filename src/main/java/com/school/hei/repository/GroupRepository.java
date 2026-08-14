package com.school.hei.repository;

import com.school.hei.entity.JGroup;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<JGroup, UUID> {}