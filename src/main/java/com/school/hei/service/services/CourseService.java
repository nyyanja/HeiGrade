package com.school.hei.service.services;

import com.school.hei.entity.JCourse;
import com.school.hei.mapper.CourseMapper;
import com.school.hei.model.Course;
import com.school.hei.repository.CourseRepository;
import com.school.hei.validator.CourseValidator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CourseService {

  private final CourseRepository courseRepository;
  private final CourseValidator courseValidator;

  public List<Course> findAll() {
    return courseRepository.findAll().stream().map(CourseMapper::toModel).toList();
  }

  public Course findById(UUID id) {
    return courseRepository
        .findById(id)
        .map(CourseMapper::toModel)
        .orElseThrow(
            () ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "course not found with id " + id));
  }
  public Course save(Course course) {


    courseValidator.accept(course);

    // =========================================================
    // 2. VALIDATION DES PROFESSEURS RESPONSABLES
    // =========================================================
    validateTeachers(course);

    // =========================================================
    // 3. VALIDATION DES SPECIALITES
    // =========================================================
    validateSpecialities(course);
    // =========================================================
    // AUCUN SAVE N'A ETE FAIT JUSQU'ICI
    // =========================================================

    // =========================================================
    // 4. CREATION DU COURS
    // =========================================================
    JCourse savedCourse = courseRepository.save(
            CourseMapper.toEntity(course)
    );

    // =========================================================
    // 5. CREATION DES ASSOCIATIONS TEACHER_COURSE
    // =========================================================
    saveTeacherCourses(course, savedCourse);

    // =========================================================
    // 6. CREATION DES ASSOCIATIONS SPECIALITY_COURSE
    // =========================================================
    saveSpecialityCourses(course, savedCourse);

    return CourseMapper.toModel(savedCourse);
  }
    private void validateTeachers(Course course) {

    if (course.getTeachers() == null) {
      return;
    }

    for (var teacher : course.getTeachers()) {

      TeacherCourse teacherCourse =
              TeacherCourse.builder()
                      .teacher(teacher)
                      .course(course)
                      .build();

      teacherCourseValidator.accept(teacherCourse);
    }
  }
  private void validateSpecialities(Course course) {

    if (course.getSpecialities() == null) {
      return;
    }

    for (var speciality : course.getSpecialities()) {

      SpecialityCourse specialityCourse =
              SpecialityCourse.builder()
                      .speciality(speciality)
                      .course(course)
                      .build();

      specialityCourseValidator.accept(specialityCourse);
    }
  }
  private void saveTeacherCourses(Course course, JCourse savedCourse) {

    if (course.getTeachers() == null) {
      return;
    }

    for (var teacher : course.getTeachers()) {

      JTeacherCourse teacherCourse =
              JTeacherCourse.builder()
                      .teacher(
                              teacherRepository.getReferenceById(teacher.getId())
                      )
                      .course(savedCourse)
                      .build();

      teacherCourseRepository.save(teacherCourse);
    }
  }
  private void saveSpecialityCourses(Course course, JCourse savedCourse) {

    if (course.getSpecialities() == null) {
      return;
    }

    for (var speciality : course.getSpecialities()) {

      JSpecialityCourse specialityCourse =
              JSpecialityCourse.builder()
                      .speciality(
                              specialityRepository.getReferenceById(speciality.getId())
                      )
                      .course(savedCourse)
                      .build();

      specialityCourseRepository.save(specialityCourse);
    }
  }

  public Course update(UUID id, Course course) {
    findById(id);

    course.setId(id);
    courseValidator.accept(course);

    JCourse entity = CourseMapper.toEntity(course);
    return CourseMapper.toModel(courseRepository.save(entity));
  }

  public void delete(UUID id) {
    if (!courseRepository.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "course not found with id " + id);
    }

    courseRepository.deleteById(id);
  }
}
