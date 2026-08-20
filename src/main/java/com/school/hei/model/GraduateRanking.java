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
public class GraduateRanking {
  private Integer rank;

  private UUID studentId;
  private String reference;
  private String firstName;
  private String lastName;

  private UUID groupId;
  private String groupName;
  private Double generalAverage;
  private Integer totalCredit;
}

