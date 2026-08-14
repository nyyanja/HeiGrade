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
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "the parameter is null");
    }
    if (gradeHistory.getReason() == null || gradeHistory.getReason().isBlank()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "the reason is blank or null it is not valid because the reason have to be described");
    }
    if (gradeHistory.getOldValue() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "the old value is null");
    }
    if (gradeHistory.getNewValue() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "the new value is null");
    }
    if (gradeHistory.getNewValue().equals(gradeHistory.getOldValue())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "the new value is the same as the old value it is not allowed");
    }
    if (gradeHistory.getGrade() == null || gradeHistory.getGrade().getId() == null) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "the grade id is null or empty it is not allowed");
    }
    if (gradeHistory.getModifiedBy() == null || gradeHistory.getModifiedBy().getId() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "the modifier hev to be tracked ");
    }
    if (!gradeRepository.existsById(gradeHistory.getGrade().getId())) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "grade does not exist");
    }
    if (!userRepository.existsById(gradeHistory.getModifiedBy().getId())) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "modifier does not exist");
    }
  }
}
