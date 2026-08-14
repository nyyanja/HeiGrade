package com.school.hei.mapper;

import com.school.hei.entity.JCourse;
import com.school.hei.model.Course;

public class CourseMapper {

  public static Course toModel(JCourse entity) {
    if (entity == null) {
      return null;
    }

    return Course.builder()
        .id(entity.getId())
        .reference(entity.getReference())
        .title(entity.getTitle())
        .credit(entity.getCredit())
        .build();
  }

  public static JCourse toEntity(Course model) {
    if (model == null) {
      return null;
    }

    return JCourse.builder()
        .id(model.getId())
        .reference(model.getReference())
        .title(model.getTitle())
        .credit(model.getCredit())
        .build();
  }
}
