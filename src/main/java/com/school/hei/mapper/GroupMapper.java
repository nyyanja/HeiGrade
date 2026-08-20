package com.school.hei.mapper;

import com.school.hei.entity.JGroup;
import com.school.hei.model.Group;

public class GroupMapper {

  public static Group toModel(JGroup entity) {
    if (entity == null) {
      return null;
    }
    return Group.builder()
        .id(entity.getId())
        .name(entity.getName())
        .promotion(PromotionMapper.toModel(entity.getPromotion()))
        .speciality(SpecialityMapper.toModel(entity.getSpeciality()))
        .build();
  }

  public static JGroup toEntity(Group model) {
    if (model == null) {
      return null;
    }
    return JGroup.builder()
        .id(model.getId())
        .name(model.getName())
        .promotion(PromotionMapper.toEntity(model.getPromotion()))
        .speciality(SpecialityMapper.toEntity(model.getSpeciality()))
        .build();
  }
}

