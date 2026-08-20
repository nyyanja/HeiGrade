package com.school.hei.unit;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.school.hei.entity.JCourse;
import com.school.hei.model.Course;
import com.school.hei.repository.CourseRepository;
import com.school.hei.validator.CourseValidator;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class CourseValidatorTest {

  @Mock private CourseRepository courseRepository;

  private CourseValidator validator;

  @BeforeEach
  void setUp() {
    validator = new CourseValidator(courseRepository);
  }

  private Course validCourse() {
    return Course.builder().reference("REF-1").title("Algo").credit(3).level(1).build();
  }

  @Test
  void should_accept_new_course() {
    Course course = validCourse();
    when(courseRepository.findByReferenceIgnoreCase("REF-1")).thenReturn(Optional.empty());

    assertThatCode(() -> validator.accept(course)).doesNotThrowAnyException();
  }

  @Test
  void should_reject_null_course() {
    assertThatThrownBy(() -> validator.accept(null))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("course cannot be null");
  }

  @Test
  void should_reject_blank_reference() {
    Course course = validCourse();
    course.setReference("  ");

    assertThatThrownBy(() -> validator.accept(course))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("course reference is required");
  }

  @Test
  void should_reject_blank_title() {
    Course course = validCourse();
    course.setTitle("  ");

    assertThatThrownBy(() -> validator.accept(course))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("course title is required");
  }

  @Test
  void should_reject_null_credit() {
    Course course = validCourse();
    course.setCredit(null);

    assertThatThrownBy(() -> validator.accept(course))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("course credit must be greater than 0");
  }

  @Test
  void should_reject_negative_credit() {
    Course course = validCourse();
    course.setCredit(-2);

    assertThatThrownBy(() -> validator.accept(course))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("course credit must be greater than 0");
  }

  @Test
  void should_reject_null_level() {
    Course course = validCourse();
    course.setLevel(null);

    assertThatThrownBy(() -> validator.accept(course))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("course level is required");
  }

  @Test
  void should_reject_invalid_level() {
    Course course = validCourse();
    course.setLevel(5);

    assertThatThrownBy(() -> validator.accept(course))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("course level must be 1, 2 or 3");
  }

  @Test
  void should_reject_when_reference_already_used() {
    Course course = validCourse();
    JCourse existing = new JCourse();
    existing.setId(UUID.randomUUID());
    when(courseRepository.findByReferenceIgnoreCase("REF-1")).thenReturn(Optional.of(existing));

    assertThatThrownBy(() -> validator.accept(course))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("course reference already used");
  }

  @Test
  void should_accept_when_existing_is_same_course() {
    Course course = validCourse();
    course.setId(UUID.randomUUID());
    JCourse existing = new JCourse();
    existing.setId(course.getId());
    when(courseRepository.findByReferenceIgnoreCase("REF-1")).thenReturn(Optional.of(existing));

    assertThatCode(() -> validator.accept(course)).doesNotThrowAnyException();
  }
}
