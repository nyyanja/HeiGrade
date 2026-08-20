package com.school.hei.unit;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.school.hei.model.Course;
import com.school.hei.model.Exam;
import com.school.hei.repository.CourseRepository;
import com.school.hei.validator.ExamValidator;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ExamValidatorTest {

  @Mock private CourseRepository courseRepository;

  private ExamValidator validator;

  private UUID courseId;

  @BeforeEach
  void setUp() {
    validator = new ExamValidator(courseRepository);
    courseId = UUID.randomUUID();
  }

  private Exam validExam() {
    return Exam.builder()
        .title("Midterm")
        .date(LocalDate.now())
        .coeff(2.0)
        .course(Course.builder().id(courseId).build())
        .build();
  }

  @Test
  void should_accept_valid_exam() {
    Exam exam = validExam();
    when(courseRepository.existsById(courseId)).thenReturn(true);

    assertThatCode(() -> validator.accept(exam)).doesNotThrowAnyException();
  }

  @Test
  void should_reject_null_exam() {
    assertThatThrownBy(() -> validator.accept(null))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("exam cannot be null");
  }

  @Test
  void should_reject_blank_title() {
    Exam exam = validExam();
    exam.setTitle("   ");

    assertThatThrownBy(() -> validator.accept(exam))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("exam title is required");
  }

  @Test
  void should_reject_null_date() {
    Exam exam = validExam();
    exam.setDate(null);

    assertThatThrownBy(() -> validator.accept(exam))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("exam date is required");
  }

  @Test
  void should_reject_null_coeff() {
    Exam exam = validExam();
    exam.setCoeff(null);

    assertThatThrownBy(() -> validator.accept(exam))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("exam coeff must be greater than 0");
  }

  @Test
  void should_reject_negative_coeff() {
    Exam exam = validExam();
    exam.setCoeff(-1.0);

    assertThatThrownBy(() -> validator.accept(exam))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("exam coeff must be greater than 0");
  }

  @Test
  void should_reject_null_course() {
    Exam exam = validExam();
    exam.setCourse(null);

    assertThatThrownBy(() -> validator.accept(exam))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("exam course is required");
  }

  @Test
  void should_reject_when_course_not_found() {
    Exam exam = validExam();
    when(courseRepository.existsById(courseId)).thenReturn(false);

    assertThatThrownBy(() -> validator.accept(exam))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("course not found");
  }
}
