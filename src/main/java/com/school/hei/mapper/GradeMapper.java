package com.school.hei.mapper;

import com.school.hei.entity.JGrade;
import com.school.hei.model.Grade;

public class GradeMapper {

  public static Grade toModel(JGrade entity) {
    if (entity == null) {
      return null;
    }

    return Grade.builder()
        .id(entity.getId())
        .value(entity.getValue())
        .date(entity.getDate())
        .student(StudentMapper.toModel(entity.getStudent()))
        .exam(ExamMapper.toModel(entity.getExam()))
        .build();
  }

  public static JGrade toEntity(Grade model) {
    if (model == null) {
      return null;
    }

    return JGrade.builder()
        .id(model.getId())
        .value(model.getValue())
        .date(model.getDate())
        .student(StudentMapper.toEntity(model.getStudent()))
        .exam(ExamMapper.toEntity(model.getExam()))
        .build();
  }
}
