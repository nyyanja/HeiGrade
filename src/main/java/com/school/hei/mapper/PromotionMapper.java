package com.school.hei.mapper;

import com.school.hei.entity.JPromotion;
import com.school.hei.model.Promotion;

public class PromotionMapper {

  public static Promotion toModel(JPromotion entity) {
    if (entity == null) {
      return null;
    }

    return Promotion.builder()
        .id(entity.getId())
        .name(entity.getName())
        .year(entity.getYear())
        .build();
  }

  public static JPromotion toEntity(Promotion model) {
    if (model == null) {
      return null;
    }

    return JPromotion.builder()
        .id(model.getId())
        .name(model.getName())
        .year(model.getYear())
        .build();
  }
}
