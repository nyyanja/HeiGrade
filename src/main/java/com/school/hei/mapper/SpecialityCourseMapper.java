package com.school.hei.mapper;

import com.school.hei.entity.JSpecialityCourse;
import com.school.hei.model.SpecialityCourse;

public class SpecialityCourseMapper {

  public static SpecialityCourse toModel(JSpecialityCourse entity) {
    if (entity == null) {
      return null;
    }

    return SpecialityCourse.builder()
        .id(entity.getId())
        .speciality(SpecialityMapper.toModel(entity.getSpeciality()))
        .course(CourseMapper.toModel(entity.getCourse()))
        .build();
  }

  public static JSpecialityCourse toEntity(SpecialityCourse model) {
    if (model == null) {
      return null;
    }

    return JSpecialityCourse.builder()
        .id(model.getId())
        .speciality(SpecialityMapper.toEntity(model.getSpeciality()))
        .course(CourseMapper.toEntity(model.getCourse()))
        .build();
  }
}

