package com.school.hei.mapper;

import com.school.hei.entity.JAdmin;
import com.school.hei.model.Admin;

public class AdminMapper {

  public static Admin toModel(JAdmin entity) {
    if (entity == null) {
      return null;
    }

    return Admin.builder()
        .id(entity.getId())
        .firstName(entity.getFirstName())
        .lastName(entity.getLastName())
        .birthday(entity.getBirthday())
        .sex(entity.getSex())
        .address(entity.getAddress())
        .email(entity.getEmail())
        .role(entity.getRole())
        .adminReference(entity.getAdminReference())
        .build();
  }

  public static JAdmin toEntity(Admin model) {
    if (model == null) {
      return null;
    }

    return JAdmin.builder()
        .id(model.getId())
        .firstName(model.getFirstName())
        .lastName(model.getLastName())
        .birthday(model.getBirthday())
        .sex(model.getSex())
        .address(model.getAddress())
        .email(model.getEmail())
        .role(model.getRole())
        .adminReference(model.getAdminReference())
        .password(model.getPassword())
        .build();
  }
}

