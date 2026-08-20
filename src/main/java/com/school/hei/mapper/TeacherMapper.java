package com.school.hei.mapper;

import com.school.hei.entity.JTeacher;
import com.school.hei.model.Teacher;

public class TeacherMapper {

  public static Teacher toModel(JTeacher entity) {
    if (entity == null) {
      return null;
    }

    return Teacher.builder()
        .id(entity.getId())
        .firstName(entity.getFirstName())
        .lastName(entity.getLastName())
        .birthday(entity.getBirthday())
        .sex(entity.getSex())
        .address(entity.getAddress())
        .email(entity.getEmail())
        .role(entity.getRole())
        .speciality(entity.getSpeciality())
        .build();
  }

  public static JTeacher toEntity(Teacher model) {
    if (model == null) {
      return null;
    }

    return JTeacher.builder()
        .id(model.getId())
        .firstName(model.getFirstName())
        .lastName(model.getLastName())
        .birthday(model.getBirthday())
        .sex(model.getSex())
        .address(model.getAddress())
        .email(model.getEmail())
        .role(model.getRole())
        .speciality(model.getSpeciality())
        .password(model.getPassword())
        .build();
  }
}

