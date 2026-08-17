package com.school.hei.model;

import com.school.hei.enums.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUserRequest {

  private String firstName;
  private String lastName;
  private String email;
  private String password;
  private Role role;
}
