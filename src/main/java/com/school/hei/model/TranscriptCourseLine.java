package com.school.hei.model;

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
public class TranscriptCourseLine {

  private UUID courseId;
  private String reference;
  private String title;
  private Integer credit;
  private Double average;
  private Integer obtainedCredit;
}


