package com.school.hei.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "student")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class JStudent extends JUser {

  @Column(name = "reference", unique = true, nullable = false)
  private String reference;

  @ManyToOne
  @JoinColumn(name = "group_id")
  private JGroup group;
}

