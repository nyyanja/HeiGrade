package com.school.hei.model;

import java.time.LocalDateTime;
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
public class GradeHistory {

  private UUID id;
  private LocalDateTime date;
  private Double oldValue;
  private Double newValue;
  private String reason;
  private Grade grade;
  private User modifiedBy;
}


