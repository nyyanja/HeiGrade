package com.school.hei.service.services;

import com.school.hei.entity.JGroup;
import com.school.hei.entity.JStudent;
import com.school.hei.model.GraduateRanking;
import com.school.hei.model.GraduateStatus;
import com.school.hei.model.Transcript;
import com.school.hei.repository.GroupRepository;
import com.school.hei.repository.PromotionRepository;
import com.school.hei.repository.StudentRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class GraduateService {

  private static final int REQUIRED_TOTAL_CREDITS = 180;

  private final TranscriptService transcriptService;
  private final StudentRepository studentRepository;
  private final GroupRepository groupRepository;
  private final PromotionRepository promotionRepository;

  @Transactional(readOnly = true)
  public GraduateStatus getGraduateStatus(UUID studentId) {
    JStudent student =
        studentRepository
            .findById(studentId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "student not found"));

    Transcript l1 = tryTranscript(studentId, 1);
    Transcript l2 = tryTranscript(studentId, 2);
    Transcript l3 = tryTranscript(studentId, 3);

    int l1Credit = l1 != null ? nullToZero(l1.getTotalCredit()) : 0;
    int l2Credit = l2 != null ? nullToZero(l2.getTotalCredit()) : 0;
    int l3Credit = l3 != null ? nullToZero(l3.getTotalCredit()) : 0;
    int totalCredit = l1Credit + l2Credit + l3Credit;

    Double generalAverage = null;
    if (l1 != null && l2 != null && l3 != null) {
      generalAverage = computeThreeYearAverage(l1, l2, l3);
    }

    boolean graduated = totalCredit >= REQUIRED_TOTAL_CREDITS;

    return GraduateStatus.builder()
        .studentId(student.getId())
        .reference(student.getReference())
        .firstName(student.getFirstName())
        .lastName(student.getLastName())
        .graduated(graduated)
        .totalCredit(totalCredit)
        .generalAverage(generalAverage)
        .l1Credit(l1Credit)
        .l2Credit(l2Credit)
        .l3Credit(l3Credit)
        .l1Average(l1 != null ? l1.getGeneralAverage() : null)
        .l2Average(l2 != null ? l2.getGeneralAverage() : null)
        .l3Average(l3 != null ? l3.getGeneralAverage() : null)
        .build();
  }

  @Transactional(readOnly = true)
  public List<GraduateRanking> getGraduatesByPromotion(UUID promotionId) {
    if (!promotionRepository.existsById(promotionId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "promotion not found");
    }

    List<JGroup> groups = groupRepository.findByPromotion_Id(promotionId);
    if (groups.isEmpty()) {
      return List.of();
    }
    Set<UUID> seen = new HashSet<>();
    List<JStudent> candidates = new ArrayList<>();
    for (JGroup group : groups) {
      for (JStudent student : studentRepository.findByGroup_Id(group.getId())) {
        if (seen.add(student.getId())) {
          candidates.add(student);
        }
      }
    }

    List<GraduateRanking> rankings = new ArrayList<>();

    for (JStudent student : candidates) {
      GraduateStatus status = getGraduateStatus(student.getId());
      if (!status.isGraduated()) {
        continue;
      }
      if (status.getGeneralAverage() == null) {
        continue;
      }

      String groupName = null;
      UUID groupId = null;
      if (student.getGroup() != null) {
        groupId = student.getGroup().getId();
        groupName = student.getGroup().getName();
      }

      rankings.add(
          GraduateRanking.builder()
              .studentId(student.getId())
              .reference(student.getReference())
              .firstName(student.getFirstName())
              .lastName(student.getLastName())
              .groupId(groupId)
              .groupName(groupName)
              .generalAverage(status.getGeneralAverage())
              .totalCredit(status.getTotalCredit())
              .build());
    }
    rankings.sort(
        Comparator.comparing(GraduateRanking::getGeneralAverage, Comparator.reverseOrder()));
    int rank = 1;
    for (GraduateRanking r : rankings) {
      r.setRank(rank++);
    }

    return rankings;
  }

  private Double computeThreeYearAverage(Transcript l1, Transcript l2, Transcript l3) {
    double weightedSum = 0.0;
    int totalCredits = 0;

    if (l1.getGeneralAverage() != null && l1.getTotalCredit() != null) {
      weightedSum += l1.getGeneralAverage() * 60;
      totalCredits += 60;
    }
    if (l2.getGeneralAverage() != null) {
      weightedSum += l2.getGeneralAverage() * 60;
      totalCredits += 60;
    }
    if (l3.getGeneralAverage() != null) {
      weightedSum += l3.getGeneralAverage() * 60;
      totalCredits += 60;
    }

    if (totalCredits == 0) {
      return null;
    }
    return weightedSum / totalCredits;
  }

  private Transcript tryTranscript(UUID studentId, int level) {
    try {
      return transcriptService.getStudentTranscript(studentId, level);
    } catch (ResponseStatusException e) {
      return null;
    }
  }

  private int nullToZero(Integer value) {
    return value == null ? 0 : value;
  }
}


