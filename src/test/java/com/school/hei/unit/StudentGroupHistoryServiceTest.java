package com.school.hei.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.school.hei.entity.JGroup;
import com.school.hei.entity.JStudent;
import com.school.hei.entity.JStudentGroupHistory;
import com.school.hei.model.StudentGroupHistory;
import com.school.hei.repository.StudentGroupHistoryRepository;
import com.school.hei.service.services.StudentGroupHistoryService;
import java.time.LocalDate;
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
class StudentGroupHistoryServiceTest {

  @Mock private StudentGroupHistoryRepository studentGroupHistoryRepository;

  @InjectMocks private StudentGroupHistoryService studentGroupHistoryService;

  private UUID historyId;
  private UUID studentId;
  private UUID groupId;

  private JStudent student;
  private JGroup group;
  private JStudentGroupHistory historyEntity;

  @BeforeEach
  void setUp() {
    historyId = UUID.randomUUID();
    studentId = UUID.randomUUID();
    groupId = UUID.randomUUID();

    student =
        JStudent.builder()
            .id(studentId)
            .reference("STD-1255")
            .firstName("Jane")
            .lastName("Doe")
            .build();

    group = JGroup.builder().id(groupId).name("K3").build();

    historyEntity =
        JStudentGroupHistory.builder()
            .id(historyId)
            .student(student)
            .group(group)
            .startDate(LocalDate.of(2026, 1, 1))
            .endDate(null)
            .build();
  }

  @Test
  void should_find_history_by_student() {
    when(studentGroupHistoryRepository.findByStudent_Id(studentId))
        .thenReturn(List.of(historyEntity));

    List<StudentGroupHistory> result = studentGroupHistoryService.findByStudent(studentId);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getId()).isEqualTo(historyId);
    assertThat(result.get(0).getStudent().getId()).isEqualTo(studentId);
    assertThat(result.get(0).getGroup().getId()).isEqualTo(groupId);
  }

  @Test
  void should_return_empty_list_when_student_has_no_history() {
    when(studentGroupHistoryRepository.findByStudent_Id(studentId)).thenReturn(List.of());

    List<StudentGroupHistory> result = studentGroupHistoryService.findByStudent(studentId);

    assertThat(result).isEmpty();
  }

  @Test
  void should_find_history_by_group() {
    when(studentGroupHistoryRepository.findByGroup_Id(groupId)).thenReturn(List.of(historyEntity));

    List<StudentGroupHistory> result = studentGroupHistoryService.findByGroup(groupId);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getGroup().getId()).isEqualTo(groupId);
  }

  @Test
  void should_find_history_by_id() {
    when(studentGroupHistoryRepository.findById(historyId)).thenReturn(Optional.of(historyEntity));

    StudentGroupHistory result = studentGroupHistoryService.findById(historyId);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(historyId);
    assertThat(result.getStartDate()).isEqualTo(LocalDate.of(2026, 1, 1));
    assertThat(result.getEndDate()).isNull();
  }

  @Test
  void should_throw_when_history_not_found_by_id() {
    when(studentGroupHistoryRepository.findById(historyId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> studentGroupHistoryService.findById(historyId))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("student group history not found with id " + historyId);
  }

  @Test
  void should_find_student_group_at_date() {
    LocalDate date = LocalDate.of(2026, 3, 15);

    when(studentGroupHistoryRepository.findStudentGroupAtDate(studentId, date))
        .thenReturn(Optional.of(historyEntity));

    StudentGroupHistory result = studentGroupHistoryService.findStudentGroupAtDate(studentId, date);

    assertThat(result).isNotNull();
    assertThat(result.getGroup().getId()).isEqualTo(groupId);
  }

  @Test
  void should_return_null_when_no_group_at_date() {
    LocalDate date = LocalDate.of(2025, 12, 1);

    when(studentGroupHistoryRepository.findStudentGroupAtDate(studentId, date))
        .thenReturn(Optional.empty());

    StudentGroupHistory result = studentGroupHistoryService.findStudentGroupAtDate(studentId, date);

    assertThat(result).isNull();
  }

  @Test
  void should_cover_std1255_changing_group_mid_year() {
    JGroup k5 = JGroup.builder().id(UUID.randomUUID()).name("K5").build();

    JStudentGroupHistory inK3 =
        JStudentGroupHistory.builder()
            .id(UUID.randomUUID())
            .student(student)
            .group(group) // K3
            .startDate(LocalDate.of(2026, 1, 1))
            .endDate(LocalDate.of(2026, 6, 30))
            .build();

    JStudentGroupHistory inK5 =
        JStudentGroupHistory.builder()
            .id(UUID.randomUUID())
            .student(student)
            .group(k5)
            .startDate(LocalDate.of(2026, 7, 1))
            .endDate(null)
            .build();

    when(studentGroupHistoryRepository.findByStudent_Id(studentId)).thenReturn(List.of(inK3, inK5));

    List<StudentGroupHistory> result = studentGroupHistoryService.findByStudent(studentId);

    assertThat(result).hasSize(2);
    assertThat(result.get(0).getGroup().getName()).isEqualTo("K3");
    assertThat(result.get(1).getGroup().getName()).isEqualTo("K5");
  }
}
