package com.school.hei.mapper;

import com.school.hei.entity.JSpeciality;
import com.school.hei.model.Speciality;

public class SpecialityMapper {

  public static Speciality toModel(JSpeciality entity) {
    if (entity == null) {
      return null;
    }

    return Speciality.builder().id(entity.getId()).name(entity.getName()).build();
  }

  public static JSpeciality toEntity(Speciality model) {
    if (model == null) {
      return null;
    }

    return JSpeciality.builder().id(model.getId()).name(model.getName()).build();
  }
}
