package com.school.hei.validator;

import com.school.hei.model.Student;
import com.school.hei.repository.GroupRepository;
import com.school.hei.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class StudentValidator implements SaveValidator<Student> {

  private final UserValidator userValidator;
  private final StudentRepository studentRepository;
  private final GroupRepository groupRepository;

  @Override
  public void accept(Student student) {
    userValidator.validateCommonFields(student);

    if (student.getReference() == null || student.getReference().isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "student reference is required");
    }

    studentRepository
        .findByReference(student.getReference())
        .ifPresent(
            existing -> {
              boolean isSameStudent =
                  student.getId() != null && existing.getId().equals(student.getId());

              if (!isSameStudent) {
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "student reference already used");
              }
            });

    validateGroup(student);
  }

  private void validateGroup(Student student) {
    if (student.getGroup() == null || student.getGroup().getId() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "student group is required");
    }

    if (!groupRepository.existsById(student.getGroup().getId())) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "group not found");
    }
  }
}
