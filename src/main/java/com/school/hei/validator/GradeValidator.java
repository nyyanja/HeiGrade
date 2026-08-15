package com.school.hei.validator;

import com.school.hei.model.Grade;
import com.school.hei.repository.ExamRepository;
import com.school.hei.repository.GradeRepository;
import com.school.hei.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class GradeValidator implements SaveValidator<Grade> {

  private final StudentRepository studentRepository;
  private final ExamRepository examRepository;
  private final GradeRepository gradeRepository;

  @Override
  public void accept(Grade grade) {
    if (grade == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "grade cannot be null");
    }
    if (grade.getValue() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "grade value is required");
    }
    if (grade.getStudent() == null || grade.getStudent().getId() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "student is required");
    }
    if (grade.getExam() == null || grade.getExam().getId() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "exam is required");
    }
    if (!studentRepository.existsById(grade.getStudent().getId())) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "student not found");
    }
    if (!examRepository.existsById(grade.getExam().getId())) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "exam not found");
    }
    gradeRepository
        .findByStudent_IdAndExam_Id(grade.getStudent().getId(), grade.getExam().getId())
        .ifPresent(
            existing -> {
              boolean isSame = grade.getId() != null && existing.getId().equals(grade.getId());
              if (!isSame) {
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "grade already exists for this student and exam");
              }
            });
  }
}
