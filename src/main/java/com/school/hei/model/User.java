package com.school.hei.model;

import com.school.hei.enums.Role;
import com.school.hei.enums.Sex;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class User {

  private UUID id;
  private String firstName;
  private String lastName;
  private LocalDate birthday;
  private Sex sex;
  private String address;
  private String email;
  private Role role;
}
