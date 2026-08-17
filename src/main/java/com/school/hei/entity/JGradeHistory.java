package com.school.hei.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "grade_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class JGradeHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "date", nullable = false)
  private LocalDateTime date;

  @Column(name = "old_value")
  private Double oldValue;

  @Column(name = "new_value", nullable = false)
  private Double newValue;

  @Column(name = "reason")
  private String reason;

  @ManyToOne
  @JoinColumn(name = "grade_id", nullable = false)
  private JGrade grade;

  @ManyToOne
  @JoinColumn(name = "modified_by", nullable = false)
  private JUser modifiedBy;
}


