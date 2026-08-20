package com.school.hei.model;

import java.time.LocalDate;
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
public class Exam {

  private UUID id;
  private LocalDate date;
  private Double coeff;
  private String title;
  private Course course;
  private List<Group> groups;
}

