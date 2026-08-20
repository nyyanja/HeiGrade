package com.school.hei.validator;

import com.school.hei.model.Exam;
import com.school.hei.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class ExamValidator implements SaveValidator<Exam> {

  private final CourseRepository courseRepository;

  @Override
  public void accept(Exam exam) {
    if (exam == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "exam cannot be null");
    }
    if (exam.getTitle() == null || exam.getTitle().isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "exam title is required");
    }
    if (exam.getDate() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "exam date is required");
    }
    if (exam.getCoeff() == null || exam.getCoeff() <= 0) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "exam coeff must be greater than 0");
    }
    if (exam.getCourse() == null || exam.getCourse().getId() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "exam course is required");
    }
    if (!courseRepository.existsById(exam.getCourse().getId())) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "course not found");
    }
  }
}
