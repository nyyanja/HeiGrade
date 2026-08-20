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
public class Transcript {

  private UUID studentId;

  private String reference;

  private String firstName;

  private String lastName;

  private Integer level;

  private List<TranscriptCourseLine> courses;

  private Double generalAverage;

  private Integer totalCredit;
}

