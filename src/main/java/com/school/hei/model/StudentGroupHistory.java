package com.school.hei.model;

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
public class StudentGroupHistory {

  private UUID id;

  private Student student;

  private Group group;

  private LocalDate startDate;

  private LocalDate endDate;
}


