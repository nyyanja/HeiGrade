package com.school.hei.model;

import com.school.hei.enums.Role;
import com.school.hei.enums.Sex;
import java.time.LocalDate;
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
  private Sex sex;
  private LocalDate birthday;
  private String address;
}
