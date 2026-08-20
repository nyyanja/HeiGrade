package com.school.hei.mapper;

import com.school.hei.entity.JExam;
import com.school.hei.model.Exam;

public class ExamMapper {

  public static Exam toModel(JExam entity) {
    if (entity == null) {
      return null;
    }

    return Exam.builder()
        .id(entity.getId())
        .date(entity.getDate())
        .coeff(entity.getCoeff())
        .title(entity.getTitle())
        .course(CourseMapper.toModel(entity.getCourse()))
        .build();
  }

  public static JExam toEntity(Exam model) {
    if (model == null) {
      return null;
    }

    return JExam.builder()
        .id(model.getId())
        .date(model.getDate())
        .coeff(model.getCoeff())
        .title(model.getTitle())
        .course(CourseMapper.toEntity(model.getCourse()))
        .build();
  }
}

