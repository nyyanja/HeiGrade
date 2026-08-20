package com.school.hei.unit;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.school.hei.entity.JGroup;
import com.school.hei.entity.JSpeciality;
import com.school.hei.entity.JStudent;
import com.school.hei.entity.JStudentGroupHistory;
import com.school.hei.enums.GroupSpeciality;
import com.school.hei.repository.StudentGroupHistoryRepository;
import com.school.hei.validator.SpecialityChangeValidator;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class SpecialityChangeValidatorTest {

  @Mock private StudentGroupHistoryRepository studentGroupHistoryRepository;

  private SpecialityChangeValidator validator;

  private UUID studentId;

  @BeforeEach
  void setUp() {
    validator = new SpecialityChangeValidator(studentGroupHistoryRepository);
    studentId = UUID.randomUUID();
  }

  private JGroup groupWithSpeciality(String name) {
    JSpeciality speciality = new JSpeciality();
    speciality.setName(name);
    JGroup group = new JGroup();
    group.setSpeciality(speciality);
    return group;
  }

  private JStudentGroupHistory historyEntry(JGroup group, LocalDate start) {
    JStudentGroupHistory h = new JStudentGroupHistory();
    h.setGroup(group);
    h.setStartDate(start);
    return h;
  }

  @Test
  void should_reject_null_student() {
    assertThatThrownBy(() -> validator.accept(null, groupWithSpeciality("EL")))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("student is required");
  }

  @Test
  void should_reject_null_group() {
    JStudent student = JStudent.builder().id(studentId).build();

    assertThatThrownBy(() -> validator.accept(student, null))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("group is required");
  }

  @Test
  void should_reject_group_without_speciality() {
    JStudent student = JStudent.builder().id(studentId).build();
    JGroup group = new JGroup();

    assertThatThrownBy(() -> validator.accept(student, group))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("group has no speciality");
  }

  @Test
  void should_always_accept_common_part() {
    JStudent student = JStudent.builder().id(studentId).build();

    assertThatCode(
            () ->
                validator.accept(student, groupWithSpeciality(GroupSpeciality.COMMON_PART.name())))
        .doesNotThrowAnyException();
  }

  @Test
  void should_accept_first_specialization() {
    JStudent student = JStudent.builder().id(studentId).build();
    when(studentGroupHistoryRepository.findByStudent_Id(studentId)).thenReturn(List.of());

    assertThatCode(() -> validator.accept(student, groupWithSpeciality("EL")))
        .doesNotThrowAnyException();
  }

  @Test
  void should_accept_single_speciality_change() {
    JStudent student = JStudent.builder().id(studentId).build();
    JStudentGroupHistory past =
        historyEntry(groupWithSpeciality("EL"), LocalDate.now().minusYears(1));
    when(studentGroupHistoryRepository.findByStudent_Id(studentId)).thenReturn(List.of(past));

    assertThatCode(() -> validator.accept(student, groupWithSpeciality("TN")))
        .doesNotThrowAnyException();
  }

  @Test
  void should_reject_second_speciality_change() {
    JStudent student = JStudent.builder().id(studentId).build();
    JStudentGroupHistory first =
        historyEntry(groupWithSpeciality("EL"), LocalDate.now().minusYears(2));
    JStudentGroupHistory second =
        historyEntry(groupWithSpeciality("TN"), LocalDate.now().minusYears(1));
    when(studentGroupHistoryRepository.findByStudent_Id(studentId))
        .thenReturn(List.of(first, second));

    assertThatThrownBy(() -> validator.accept(student, groupWithSpeciality("EL")))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("speciality change not allowed");
  }
}
