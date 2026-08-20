package com.school.hei.validator;

import com.school.hei.model.Course;
import com.school.hei.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class CourseValidator implements SaveValidator<Course> {

  private final CourseRepository courseRepository;

  @Override
  public void accept(Course course) {
    if (course == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "course cannot be null");
    }
    if (course.getReference() == null || course.getReference().isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "course reference is required");
    }
    if (course.getTitle() == null || course.getTitle().isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "course title is required");
    }
    if (course.getCredit() == null || course.getCredit() <= 0) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "course credit must be greater than 0");
    }
    if (course.getLevel() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "course level is required");
    }

    if (course.getLevel() < 1 || course.getLevel() > 3) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "course level must be 1, 2 or 3");
    }

    courseRepository
        .findByReferenceIgnoreCase(course.getReference())
        .ifPresent(
            existing -> {
              boolean isSameCourse =
                  course.getId() != null && existing.getId().equals(course.getId());
              if (!isSameCourse) {
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "course reference already used");
              }
            });
  }
}

