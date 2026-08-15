package com.school.hei.validator;

import com.school.hei.entity.JStudent;
import com.school.hei.model.Grade;
import com.school.hei.repository.ExamRepository;
import com.school.hei.repository.GradeRepository;
import com.school.hei.repository.GroupExamRepository;
import com.school.hei.repository.StudentRepository;
import java.util.UUID;
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
  private final GroupExamRepository groupExamRepository;

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

    UUID studentId = grade.getStudent().getId();
    UUID examId = grade.getExam().getId();

    JStudent student =
            studentRepository
                    .findById(studentId)
                    .orElseThrow(
                            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "student not found"));

    if (!examRepository.existsById(examId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "exam not found");
    }
    if (student.getGroup() == null || student.getGroup().getId() == null) {
      throw new ResponseStatusException(
              HttpStatus.BAD_REQUEST, "student has no group and therefore cannot receive a grade");
    }

    UUID groupId = student.getGroup().getId();
    boolean isAuthorized =
            groupExamRepository.findByGroup_IdAndExam_Id(groupId, examId).isPresent();

    if (!isAuthorized) {
      throw new ResponseStatusException(
              HttpStatus.FORBIDDEN,
              "student's group is not authorized for this exam (no group-exam association)");
    }

    gradeRepository
            .findByStudent_IdAndExam_Id(studentId, examId)
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