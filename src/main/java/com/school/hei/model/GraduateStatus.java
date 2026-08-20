package com.school.hei.model;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GraduateStatus {

  private UUID studentId;
  private String reference;
  private String firstName;
  private String lastName;

  private boolean graduated;
  private Integer totalCredit;
  private Double generalAverage;

  private Integer l1Credit;
  private Integer l2Credit;
  private Integer l3Credit;

  private Double l1Average;
  private Double l2Average;
  private Double l3Average;
}

