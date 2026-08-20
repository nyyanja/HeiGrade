package com.school.hei.unit;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.school.hei.entity.JTeacherCourse;
import com.school.hei.model.Course;
import com.school.hei.model.Teacher;
import com.school.hei.model.TeacherCourse;
import com.school.hei.repository.CourseRepository;
import com.school.hei.repository.TeacherCourseRepository;
import com.school.hei.repository.TeacherRepository;
import com.school.hei.validator.TeacherCourseValidator;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class TeacherCourseValidatorTest {

  @Mock private TeacherRepository teacherRepository;
  @Mock private CourseRepository courseRepository;
  @Mock private TeacherCourseRepository teacherCourseRepository;

  private TeacherCourseValidator validator;

  private UUID teacherId;
  private UUID courseId;

  @BeforeEach
  void setUp() {
    validator =
        new TeacherCourseValidator(teacherRepository, courseRepository, teacherCourseRepository);
    teacherId = UUID.randomUUID();
    courseId = UUID.randomUUID();
  }

  private TeacherCourse validTeacherCourse() {
    return TeacherCourse.builder()
        .teacher(Teacher.builder().id(teacherId).build())
        .course(Course.builder().id(courseId).build())
        .build();
  }

  @Test
  void should_accept_valid_teacher_course() {
    TeacherCourse tc = validTeacherCourse();
    when(teacherRepository.existsById(teacherId)).thenReturn(true);
    when(courseRepository.existsById(courseId)).thenReturn(true);
    when(teacherCourseRepository.findByTeacher_IdAndCourse_Id(teacherId, courseId))
        .thenReturn(Optional.empty());

    assertThatCode(() -> validator.accept(tc)).doesNotThrowAnyException();
  }

  @Test
  void should_reject_null_teacher_course() {
    assertThatThrownBy(() -> validator.accept(null))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("it cannot be saved");
  }

  @Test
  void should_reject_null_teacher() {
    TeacherCourse tc = validTeacherCourse();
    tc.setTeacher(null);

    assertThatThrownBy(() -> validator.accept(tc))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("teacher affectation");
  }

  @Test
  void should_reject_null_course() {
    TeacherCourse tc = validTeacherCourse();
    tc.setCourse(null);

    assertThatThrownBy(() -> validator.accept(tc))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("course affectation");
  }

  @Test
  void should_reject_when_teacher_not_found() {
    TeacherCourse tc = validTeacherCourse();
    when(teacherRepository.existsById(teacherId)).thenReturn(false);

    assertThatThrownBy(() -> validator.accept(tc))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("teacher not found");
  }

  @Test
  void should_reject_when_course_not_found() {
    TeacherCourse tc = validTeacherCourse();
    when(teacherRepository.existsById(teacherId)).thenReturn(true);
    when(courseRepository.existsById(courseId)).thenReturn(false);

    assertThatThrownBy(() -> validator.accept(tc))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("course not found");
  }

  @Test
  void should_reject_when_link_already_exists() {
    TeacherCourse tc = validTeacherCourse();
    JTeacherCourse existing = new JTeacherCourse();
    existing.setId(UUID.randomUUID());
    when(teacherRepository.existsById(teacherId)).thenReturn(true);
    when(courseRepository.existsById(courseId)).thenReturn(true);
    when(teacherCourseRepository.findByTeacher_IdAndCourse_Id(teacherId, courseId))
        .thenReturn(Optional.of(existing));

    assertThatThrownBy(() -> validator.accept(tc))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("this teacher is already saved to teach this course");
  }
}
