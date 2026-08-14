package com.school.hei.repository;

import com.school.hei.entity.JPromotion;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromotionRepository extends JpaRepository<JPromotion, UUID> {}
