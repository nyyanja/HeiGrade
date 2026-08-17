package com.school.hei.validator;

import com.school.hei.model.StudentGroupHistory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class StudentGroupHistoryValidator {

  public void accept(StudentGroupHistory history) {
    if (history == null) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "student group history is required");
    }

    if (history.getStudent() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "student is required");
    }

    if (history.getGroup() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "group is required");
    }

    if (history.getStartDate() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "start date is required");
    }

    if (history.getEndDate() != null && history.getEndDate().isBefore(history.getStartDate())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "end date cannot be before start date");
    }
  }
}


