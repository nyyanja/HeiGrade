package com.school.hei.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.school.hei.entity.JGrade;
import com.school.hei.entity.JGradeHistory;
import com.school.hei.entity.JUser;
import com.school.hei.enums.Role;
import com.school.hei.enums.Sex;
import com.school.hei.mapper.GradeHistoryMapper;
import com.school.hei.model.GradeHistory;
import com.school.hei.repository.GradeHistoryRepository;
import com.school.hei.service.services.GradeHistoryService;
import com.school.hei.validator.GradeHistoryValidator;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class GradeHistoryServiceTest {

  @Mock private GradeHistoryRepository gradeHistoryRepository;

  @Mock private GradeHistoryValidator gradeHistoryValidator;

  @InjectMocks private GradeHistoryService gradeHistoryService;

  private UUID historyId;
  private UUID gradeId;
  private UUID userId;

  private JGradeHistory historyEntity;

  @BeforeEach
  void setUp() {
    historyId = UUID.randomUUID();
    gradeId = UUID.randomUUID();
    userId = UUID.randomUUID();

    JGrade grade = JGrade.builder().id(gradeId).value(15.0).build();

    JUser user =
        JUser.builder()
            .id(userId)
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@gmail.com")
            .sex(Sex.MALE)
            .role(Role.TEACHER)
            .build();

    historyEntity =
        JGradeHistory.builder()
            .id(historyId)
            .date(LocalDateTime.of(2026, 8, 1, 10, 30))
            .oldValue(10.0)
            .newValue(15.0)
            .reason("Correction")
            .grade(grade)
            .modifiedBy(user)
            .build();
  }

  @Test
  void should_find_all_grade_histories() {
    when(gradeHistoryRepository.findAll()).thenReturn(List.of(historyEntity));

    List<GradeHistory> result = gradeHistoryService.findAll();

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getId()).isEqualTo(historyId);
    assertThat(result.get(0).getOldValue()).isEqualTo(10.0);
    assertThat(result.get(0).getNewValue()).isEqualTo(15.0);
    assertThat(result.get(0).getReason()).isEqualTo("Correction");

    verify(gradeHistoryRepository).findAll();
  }

  @Test
  void should_return_empty_list_when_no_grade_histories() {
    when(gradeHistoryRepository.findAll()).thenReturn(List.of());

    List<GradeHistory> result = gradeHistoryService.findAll();

    assertThat(result).isEmpty();

    verify(gradeHistoryRepository).findAll();
  }

  @Test
  void should_find_grade_history_by_id() {
    when(gradeHistoryRepository.findById(historyId)).thenReturn(Optional.of(historyEntity));

    GradeHistory result = gradeHistoryService.findById(historyId);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(historyId);
    assertThat(result.getOldValue()).isEqualTo(10.0);
    assertThat(result.getNewValue()).isEqualTo(15.0);
    assertThat(result.getReason()).isEqualTo("Correction");

    verify(gradeHistoryRepository).findById(historyId);
  }

  @Test
  void should_throw_when_grade_history_does_not_exist() {
    when(gradeHistoryRepository.findById(historyId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> gradeHistoryService.findById(historyId))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("grade history not found");

    verify(gradeHistoryRepository).findById(historyId);
  }

  @Test
  void should_save_grade_history() {
    GradeHistory history = GradeHistoryMapper.toModel(historyEntity);

    when(gradeHistoryRepository.save(any(JGradeHistory.class))).thenReturn(historyEntity);

    GradeHistory result = gradeHistoryService.save(history);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(historyId);
    assertThat(result.getOldValue()).isEqualTo(10.0);
    assertThat(result.getNewValue()).isEqualTo(15.0);
    assertThat(result.getReason()).isEqualTo("Correction");

    verify(gradeHistoryValidator).accept(history);
    verify(gradeHistoryRepository).save(any(JGradeHistory.class));
  }

  @Test
  void should_set_current_date_when_saving_history_without_date() {
    GradeHistory history = GradeHistoryMapper.toModel(historyEntity);

    history.setDate(null);

    when(gradeHistoryRepository.save(any(JGradeHistory.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    GradeHistory result = gradeHistoryService.save(history);

    assertThat(result.getDate()).isNotNull();
    assertThat(result.getDate()).isBeforeOrEqualTo(LocalDateTime.now());
    assertThat(result.getDate()).isAfter(LocalDateTime.now().minusSeconds(5));

    verify(gradeHistoryValidator).accept(history);
    verify(gradeHistoryRepository).save(any(JGradeHistory.class));
  }

  @Test
  void should_delete_grade_history() {
    when(gradeHistoryRepository.existsById(historyId)).thenReturn(true);

    gradeHistoryService.delete(historyId);

    verify(gradeHistoryRepository).existsById(historyId);
    verify(gradeHistoryRepository).deleteById(historyId);
  }

  @Test
  void should_throw_when_deleting_non_existing_grade_history() {
    when(gradeHistoryRepository.existsById(historyId)).thenReturn(false);

    assertThatThrownBy(() -> gradeHistoryService.delete(historyId))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("grade history not found");

    verify(gradeHistoryRepository).existsById(historyId);
    verify(gradeHistoryRepository, never()).deleteById(any());
  }
}


