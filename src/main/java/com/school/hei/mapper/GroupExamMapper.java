package com.school.hei.mapper;

import com.school.hei.entity.JGroupExam;
import com.school.hei.model.GroupExam;

public class GroupExamMapper {

  public static GroupExam toModel(JGroupExam entity) {
    if (entity == null) {
      return null;
    }

    return GroupExam.builder()
        .id(entity.getId())
        .group(GroupMapper.toModel(entity.getGroup()))
        .exam(ExamMapper.toModel(entity.getExam()))
        .build();
  }

  public static JGroupExam toEntity(GroupExam model) {
    if (model == null) {
      return null;
    }

    return JGroupExam.builder()
        .id(model.getId())
        .group(GroupMapper.toEntity(model.getGroup()))
        .exam(ExamMapper.toEntity(model.getExam()))
        .build();
  }
}


