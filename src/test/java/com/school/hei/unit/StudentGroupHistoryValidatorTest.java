package com.school.hei.unit;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.school.hei.model.Group;
import com.school.hei.model.Student;
import com.school.hei.model.StudentGroupHistory;
import com.school.hei.validator.StudentGroupHistoryValidator;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class StudentGroupHistoryValidatorTest {

  private StudentGroupHistoryValidator validator;

  @BeforeEach
  void setUp() {
    validator = new StudentGroupHistoryValidator();
  }

  private StudentGroupHistory validHistory() {
    return StudentGroupHistory.builder()
        .student(Student.builder().id(UUID.randomUUID()).build())
        .group(Group.builder().id(UUID.randomUUID()).build())
        .startDate(LocalDate.now().minusMonths(6))
        .endDate(LocalDate.now())
        .build();
  }

  @Test
  void should_accept_valid_history() {
    assertThatCode(() -> validator.accept(validHistory())).doesNotThrowAnyException();
  }

  @Test
  void should_accept_history_without_end_date() {
    StudentGroupHistory history = validHistory();
    history.setEndDate(null);

    assertThatCode(() -> validator.accept(history)).doesNotThrowAnyException();
  }

  @Test
  void should_reject_null_history() {
    assertThatThrownBy(() -> validator.accept(null))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("student group history is required");
  }

  @Test
  void should_reject_null_student() {
    StudentGroupHistory history = validHistory();
    history.setStudent(null);

    assertThatThrownBy(() -> validator.accept(history))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("student is required");
  }

  @Test
  void should_reject_null_group() {
    StudentGroupHistory history = validHistory();
    history.setGroup(null);

    assertThatThrownBy(() -> validator.accept(history))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("group is required");
  }

  @Test
  void should_reject_null_start_date() {
    StudentGroupHistory history = validHistory();
    history.setStartDate(null);

    assertThatThrownBy(() -> validator.accept(history))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("start date is required");
  }

  @Test
  void should_reject_end_date_before_start_date() {
    StudentGroupHistory history = validHistory();
    history.setStartDate(LocalDate.now());
    history.setEndDate(LocalDate.now().minusDays(1));

    assertThatThrownBy(() -> validator.accept(history))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("end date cannot be before start date");
  }
}
