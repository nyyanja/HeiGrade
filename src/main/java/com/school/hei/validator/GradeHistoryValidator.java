package com.school.hei.validator;

import com.school.hei.model.GradeHistory;
import com.school.hei.repository.GradeRepository;
import com.school.hei.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class GradeHistoryValidator implements SaveValidator<GradeHistory> {

  private final GradeRepository gradeRepository;
  private final UserRepository userRepository;

  @Override
  public void accept(GradeHistory gradeHistory) {
    if (gradeHistory == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "grade history cannot be null");
    }
    if (gradeHistory.getReason() == null || gradeHistory.getReason().isBlank()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "reason is required for grade history");
    }
    if (gradeHistory.getOldValue() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "old value is required");
    }
    if (gradeHistory.getNewValue() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "new value is required");
    }
    if (gradeHistory.getNewValue().equals(gradeHistory.getOldValue())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "new value must be different from old value");
    }
    if (gradeHistory.getGrade() == null || gradeHistory.getGrade().getId() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "grade is required");
    }
    if (gradeHistory.getModifiedBy() == null || gradeHistory.getModifiedBy().getId() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "modifier is required");
    }
    if (!gradeRepository.existsById(gradeHistory.getGrade().getId())) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "grade not found");
    }
    if (!userRepository.existsById(gradeHistory.getModifiedBy().getId())) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "modifier not found");
    }
  }
}


