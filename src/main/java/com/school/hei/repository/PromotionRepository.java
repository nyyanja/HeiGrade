package com.school.hei.repository;

import com.school.hei.entity.JPromotion;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromotionRepository extends JpaRepository<JPromotion, UUID> {
  List<JPromotion> findByYear(Integer year);

  List<JPromotion> findByNameContainingIgnoreCase(String name);
}


