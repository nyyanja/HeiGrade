package com.school.hei.mapper;

import com.school.hei.entity.JGradeHistory;
import com.school.hei.model.GradeHistory;

public class GradeHistoryMapper {

  public static GradeHistory toModel(JGradeHistory entity) {
    if (entity == null) {
      return null;
    }

    return GradeHistory.builder()
        .id(entity.getId())
        .date(entity.getDate())
        .oldValue(entity.getOldValue())
        .newValue(entity.getNewValue())
        .reason(entity.getReason())
        .grade(GradeMapper.toModel(entity.getGrade()))
        .modifiedBy(UserMapper.toModel(entity.getModifiedBy()))
        .build();
  }

  public static JGradeHistory toEntity(GradeHistory model) {
    if (model == null) {
      return null;
    }

    return JGradeHistory.builder()
        .id(model.getId())
        .date(model.getDate())
        .oldValue(model.getOldValue())
        .newValue(model.getNewValue())
        .reason(model.getReason())
        .grade(GradeMapper.toEntity(model.getGrade()))
        .modifiedBy(UserMapper.toEntity(model.getModifiedBy()))
        .build();
  }
}
