package com.school.hei.mapper;

import com.school.hei.entity.JStudent;
import com.school.hei.model.Student;

public class StudentMapper {

  public static Student toModel(JStudent entity) {
    if (entity == null) {
      return null;
    }

    return Student.builder()
        .id(entity.getId())
        .firstName(entity.getFirstName())
        .lastName(entity.getLastName())
        .birthday(entity.getBirthday())
        .sex(entity.getSex())
        .address(entity.getAddress())
        .email(entity.getEmail())
        .role(entity.getRole())
        .reference(entity.getReference())
        .group(GroupMapper.toModel(entity.getGroup()))
        .build();
  }

  public static JStudent toEntity(Student model) {
    if (model == null) {
      return null;
    }

    return JStudent.builder()
        .id(model.getId())
        .firstName(model.getFirstName())
        .lastName(model.getLastName())
        .birthday(model.getBirthday())
        .sex(model.getSex())
        .address(model.getAddress())
        .email(model.getEmail())
        .role(model.getRole())
        .reference(model.getReference())
        .group(GroupMapper.toEntity(model.getGroup()))
        .build();
  }
}


