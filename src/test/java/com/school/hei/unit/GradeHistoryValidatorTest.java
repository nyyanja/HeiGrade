package com.school.hei.unit;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.school.hei.model.Grade;
import com.school.hei.model.GradeHistory;
import com.school.hei.model.User;
import com.school.hei.repository.GradeRepository;
import com.school.hei.repository.UserRepository;
import com.school.hei.validator.GradeHistoryValidator;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class GradeHistoryValidatorTest {

  @Mock private GradeRepository gradeRepository;

  @Mock private UserRepository userRepository;

  private GradeHistoryValidator validator;

  private UUID gradeId;
  private UUID userId;

  @BeforeEach
  void setUp() {
    validator = new GradeHistoryValidator(gradeRepository, userRepository);

    gradeId = UUID.randomUUID();
    userId = UUID.randomUUID();
  }

  private GradeHistory validHistory() {
    return GradeHistory.builder()
        .id(UUID.randomUUID())
        .oldValue(10.0)
        .newValue(15.0)
        .reason("Correction")
        .grade(Grade.builder().id(gradeId).build())
        .modifiedBy(User.builder().id(userId).build())
        .build();
  }

  @Test
  void should_accept_valid_grade_history() {
    GradeHistory history = validHistory();

    when(gradeRepository.existsById(gradeId)).thenReturn(true);
    when(userRepository.existsById(userId)).thenReturn(true);

    assertThatCode(() -> validator.accept(history)).doesNotThrowAnyException();

    verify(gradeRepository).existsById(gradeId);
    verify(userRepository).existsById(userId);
  }

  @Test
  void should_reject_null_grade_history() {
    assertThatThrownBy(() -> validator.accept(null))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("grade history cannot be null");

    verifyNoInteractions(gradeRepository, userRepository);
  }

  @Test
  void should_reject_null_reason() {
    GradeHistory history = validHistory();
    history.setReason(null);

    assertThatThrownBy(() -> validator.accept(history))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("reason is required for grade history");

    verifyNoInteractions(gradeRepository, userRepository);
  }

  @Test
  void should_reject_blank_reason() {
    GradeHistory history = validHistory();
    history.setReason("   ");

    assertThatThrownBy(() -> validator.accept(history))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("reason is required for grade history");

    verifyNoInteractions(gradeRepository, userRepository);
  }

  @Test
  void should_reject_null_old_value() {
    GradeHistory history = validHistory();
    history.setOldValue(null);

    assertThatThrownBy(() -> validator.accept(history))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("old value is required");

    verifyNoInteractions(gradeRepository, userRepository);
  }

  @Test
  void should_reject_null_new_value() {
    GradeHistory history = validHistory();
    history.setNewValue(null);

    assertThatThrownBy(() -> validator.accept(history))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("new value is required");

    verifyNoInteractions(gradeRepository, userRepository);
  }

  @Test
  void should_reject_same_old_and_new_value() {
    GradeHistory history = validHistory();
    history.setNewValue(history.getOldValue());

    assertThatThrownBy(() -> validator.accept(history))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("new value must be different from old value");

    verifyNoInteractions(gradeRepository, userRepository);
  }

  @Test
  void should_reject_null_grade() {
    GradeHistory history = validHistory();
    history.setGrade(null);

    assertThatThrownBy(() -> validator.accept(history))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("grade is required");

    verifyNoInteractions(gradeRepository, userRepository);
  }

  @Test
  void should_reject_grade_without_id() {
    GradeHistory history = validHistory();
    history.setGrade(Grade.builder().build());

    assertThatThrownBy(() -> validator.accept(history))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("grade is required");

    verifyNoInteractions(gradeRepository, userRepository);
  }

  @Test
  void should_reject_null_modifier() {
    GradeHistory history = validHistory();
    history.setModifiedBy(null);

    assertThatThrownBy(() -> validator.accept(history))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("modifier is required");

    verifyNoInteractions(gradeRepository, userRepository);
  }

  @Test
  void should_reject_modifier_without_id() {
    GradeHistory history = validHistory();
    history.setModifiedBy(User.builder().build());

    assertThatThrownBy(() -> validator.accept(history))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("modifier is required");

    verifyNoInteractions(gradeRepository, userRepository);
  }

  @Test
  void should_reject_when_grade_does_not_exist() {
    GradeHistory history = validHistory();

    when(gradeRepository.existsById(gradeId)).thenReturn(false);

    assertThatThrownBy(() -> validator.accept(history))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("grade not found");

    verify(gradeRepository).existsById(gradeId);
    verifyNoInteractions(userRepository);
  }

  @Test
  void should_reject_when_modifier_does_not_exist() {
    GradeHistory history = validHistory();

    when(gradeRepository.existsById(gradeId)).thenReturn(true);
    when(userRepository.existsById(userId)).thenReturn(false);

    assertThatThrownBy(() -> validator.accept(history))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("modifier not found");

    verify(gradeRepository).existsById(gradeId);
    verify(userRepository).existsById(userId);
  }
}

