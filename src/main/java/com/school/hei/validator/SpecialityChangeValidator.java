package com.school.hei.validator;

import com.school.hei.entity.JGroup;
import com.school.hei.entity.JStudent;
import com.school.hei.entity.JStudentGroupHistory;
import com.school.hei.enums.GroupSpeciality;
import com.school.hei.repository.StudentGroupHistoryRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class SpecialityChangeValidator {

  private final StudentGroupHistoryRepository studentGroupHistoryRepository;

  public void accept(JStudent student, JGroup newGroup) {
    if (student == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "student is required");
    }
    if (newGroup == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "group is required");
    }
    if (newGroup.getSpeciality() == null || newGroup.getSpeciality().getName() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "group has no speciality");
    }

    String newSpecName = normalizeSpecialityName(newGroup.getSpeciality().getName());

    if (isCommonPart(newSpecName)) {
      return;
    }

    List<JStudentGroupHistory> history =
        new ArrayList<>(studentGroupHistoryRepository.findByStudent_Id(student.getId()));
    history.sort(
        Comparator.comparing(h -> h.getStartDate() != null ? h.getStartDate() : LocalDate.MIN));

    List<String> specializedSequence = new ArrayList<>();
    for (JStudentGroupHistory h : history) {
      if (h.getGroup() == null || h.getGroup().getSpeciality() == null) {
        continue;
      }
      String name = normalizeSpecialityName(h.getGroup().getSpeciality().getName());
      if (!isCommonPart(name)) {
        if (specializedSequence.isEmpty()
            || !specializedSequence.get(specializedSequence.size() - 1).equalsIgnoreCase(name)) {
          specializedSequence.add(name);
        }
      }
    }

    int changesAlready = Math.max(0, specializedSequence.size() - 1);
    String lastSpecialized =
        specializedSequence.isEmpty()
            ? null
            : specializedSequence.get(specializedSequence.size() - 1);

    boolean isChange = lastSpecialized != null && !lastSpecialized.equalsIgnoreCase(newSpecName);

    if (isChange && changesAlready >= 1) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "speciality change not allowed: specialized speciality (EL/TN) can change at most "
              + "once (L2). In L3 the speciality must remain invariant.");
    }
  }

  private boolean isCommonPart(String name) {
    return name != null && name.equalsIgnoreCase(GroupSpeciality.COMMON_PART.name());
  }

  private String normalizeSpecialityName(Object name) {
    if (name == null) {
      return "";
    }
    if (name instanceof GroupSpeciality gs) {
      return gs.name();
    }
    return name.toString().trim();
  }
}


