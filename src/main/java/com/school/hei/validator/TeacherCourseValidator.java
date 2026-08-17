package com.school.hei.validator;

import com.school.hei.model.TeacherCourse;
import com.school.hei.repository.CourseRepository;
import com.school.hei.repository.TeacherCourseRepository;
import com.school.hei.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class TeacherCourseValidator implements SaveValidator<TeacherCourse> {

  private final UserRepository userRepository;
  private final CourseRepository courseRepository;
  private final TeacherCourseRepository teacherCourseRepository;

  @Override
  public void accept(TeacherCourse teacherCourse) {
    if (teacherCourse == null) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "the affectation for one course is null it cannot be saved");
    }
    if (teacherCourse.getTeacher() == null || teacherCourse.getTeacher().getId() == null) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "the teacher affectation for one course is null it cannot be saved");
    }
    if (teacherCourse.getCourse() == null || teacherCourse.getCourse().getId() == null) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "the course affectation for one course teaching is null it cannot be saved");
    }
    if (!userRepository.existsById(teacherCourse.getTeacher().getId())) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "teacher not found");
    }
    if (!courseRepository.existsById(teacherCourse.getCourse().getId())) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "course not found");
    }

    teacherCourseRepository
        .findByTeacher_IdAndCourse_Id(
            teacherCourse.getTeacher().getId(), teacherCourse.getCourse().getId())
        .ifPresent(
            existing -> {
              boolean isSameLink =
                  teacherCourse.getId() != null && existing.getId().equals(teacherCourse.getId());
              if (!isSameLink) {
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "this teacher is already saved to teach this course");
              }
            });
  }
}


