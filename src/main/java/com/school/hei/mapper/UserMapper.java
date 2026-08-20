package com.school.hei.mapper;

import com.school.hei.entity.JUser;
import com.school.hei.model.User;

public class UserMapper {

  public static User toModel(JUser entity) {
    if (entity == null) {
      return null;
    }

    return User.builder()
        .id(entity.getId())
        .firstName(entity.getFirstName())
        .lastName(entity.getLastName())
        .birthday(entity.getBirthday())
        .sex(entity.getSex())
        .address(entity.getAddress())
        .email(entity.getEmail())
        .role(entity.getRole())
        .build();
  }

  public static JUser toEntity(User model) {
    if (model == null) {
      return null;
    }

    return JUser.builder()
        .id(model.getId())
        .firstName(model.getFirstName())
        .lastName(model.getLastName())
        .birthday(model.getBirthday())
        .sex(model.getSex())
        .address(model.getAddress())
        .email(model.getEmail())
        .role(model.getRole())
        .build();
  }
}

