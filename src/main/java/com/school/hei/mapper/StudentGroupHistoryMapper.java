package com.school.hei.mapper;

import com.school.hei.entity.JStudentGroupHistory;
import com.school.hei.model.StudentGroupHistory;

public class StudentGroupHistoryMapper {

  public static StudentGroupHistory toModel(JStudentGroupHistory entity) {
    if (entity == null) {
      return null;
    }

    return StudentGroupHistory.builder()
        .id(entity.getId())
        .student(StudentMapper.toModel(entity.getStudent()))
        .group(GroupMapper.toModel(entity.getGroup()))
        .startDate(entity.getStartDate())
        .endDate(entity.getEndDate())
        .build();
  }

  public static JStudentGroupHistory toEntity(StudentGroupHistory model) {
    if (model == null) {
      return null;
    }

    return JStudentGroupHistory.builder()
        .id(model.getId())
        .student(StudentMapper.toEntity(model.getStudent()))
        .group(GroupMapper.toEntity(model.getGroup()))
        .startDate(model.getStartDate())
        .endDate(model.getEndDate())
        .build();
  }
}


