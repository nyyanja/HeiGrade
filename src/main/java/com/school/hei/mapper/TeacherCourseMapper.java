package com.school.hei.mapper;

import com.school.hei.entity.JTeacherCourse;
import com.school.hei.model.TeacherCourse;

public class TeacherCourseMapper {

  public static TeacherCourse toModel(JTeacherCourse entity) {
    if (entity == null) {
      return null;
    }

    return TeacherCourse.builder()
        .id(entity.getId())
        .teacher(TeacherMapper.toModel(entity.getTeacher()))
        .course(CourseMapper.toModel(entity.getCourse()))
        .build();
  }

  public static JTeacherCourse toEntity(TeacherCourse model) {
    if (model == null) {
      return null;
    }

    return JTeacherCourse.builder()
        .id(model.getId())
        .teacher(TeacherMapper.toEntity(model.getTeacher()))
        .course(CourseMapper.toEntity(model.getCourse()))
        .build();
  }
}
