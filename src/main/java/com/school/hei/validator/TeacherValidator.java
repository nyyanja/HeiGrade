package com.school.hei.validator;

import com.school.hei.model.Teacher;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class TeacherValidator implements SaveValidator<Teacher> {

  private final UserValidator userValidator;

  @Override
  public void accept(Teacher teacher) {
    userValidator.validateCommonFields(teacher);
    if (teacher.getSpeciality() == null || teacher.getSpeciality().isBlank()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "the teacher special is blank or null it is not allowed");
    }
  }
}
