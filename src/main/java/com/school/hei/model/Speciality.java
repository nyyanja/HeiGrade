package com.school.hei.model;

import java.util.UUID;

import com.school.hei.enums.GroupSpeciality;
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
public class Speciality {

  private UUID id;
  private GroupSpeciality name;
}
