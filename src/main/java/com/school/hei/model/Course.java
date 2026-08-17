package com.school.hei.model;

import java.util.List;
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
public class Course {

  private UUID id;
  private String reference;
  private String title;
  private Integer credit;
  private Integer level;

  private List<Teacher> teachers;
  private List<Speciality> specialities;
}


