package com.school.hei.unit;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.school.hei.entity.JStudent;
import com.school.hei.enums.Role;
import com.school.hei.model.Group;
import com.school.hei.model.Student;
import com.school.hei.repository.GroupRepository;
import com.school.hei.repository.StudentRepository;
import com.school.hei.validator.StudentValidator;
import com.school.hei.validator.UserValidator;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class StudentValidatorTest {

  @Mock private UserValidator userValidator;
  @Mock private StudentRepository studentRepository;
  @Mock private GroupRepository groupRepository;

  private StudentValidator validator;

  private UUID groupId;

  @BeforeEach
  void setUp() {
    validator = new StudentValidator(userValidator, studentRepository, groupRepository);
    groupId = UUID.randomUUID();
  }

  private Student validStudent() {
    return Student.builder()
        .reference("STU-1")
        .role(Role.STUDENT)
        .group(Group.builder().id(groupId).build())
        .build();
  }

  @Test
  void should_accept_valid_student() {
    Student student = validStudent();
    doNothing().when(userValidator).validateCommonFields(student);
    when(studentRepository.findByReference("STU-1")).thenReturn(Optional.empty());
    when(groupRepository.existsById(groupId)).thenReturn(true);

    assertThatCode(() -> validator.accept(student)).doesNotThrowAnyException();
  }

  @Test
  void should_reject_null_reference() {
    Student student = validStudent();
    student.setReference(null);
    doNothing().when(userValidator).validateCommonFields(student);

    assertThatThrownBy(() -> validator.accept(student))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("student reference is required");
  }

  @Test
  void should_reject_wrong_role() {
    Student student = validStudent();
    student.setRole(Role.TEACHER);
    doNothing().when(userValidator).validateCommonFields(student);

    assertThatThrownBy(() -> validator.accept(student))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("student role must be STUDENT");
  }

  @Test
  void should_reject_when_reference_already_used() {
    Student student = validStudent();
    doNothing().when(userValidator).validateCommonFields(student);
    JStudent existing = JStudent.builder().id(UUID.randomUUID()).build();
    when(studentRepository.findByReference("STU-1")).thenReturn(Optional.of(existing));

    assertThatThrownBy(() -> validator.accept(student))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("student reference already used");
  }

  @Test
  void should_accept_when_existing_reference_is_same_student() {
    Student student = validStudent();
    student.setId(UUID.randomUUID());
    doNothing().when(userValidator).validateCommonFields(student);
    JStudent existing = JStudent.builder().id(student.getId()).build();
    when(studentRepository.findByReference("STU-1")).thenReturn(Optional.of(existing));
    when(groupRepository.existsById(groupId)).thenReturn(true);

    assertThatCode(() -> validator.accept(student)).doesNotThrowAnyException();
  }

  @Test
  void should_reject_null_group() {
    Student student = validStudent();
    student.setGroup(null);
    doNothing().when(userValidator).validateCommonFields(student);
    when(studentRepository.findByReference("STU-1")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> validator.accept(student))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("student group is required");
  }

  @Test
  void should_reject_when_group_not_found() {
    Student student = validStudent();
    doNothing().when(userValidator).validateCommonFields(student);
    when(studentRepository.findByReference("STU-1")).thenReturn(Optional.empty());
    when(groupRepository.existsById(groupId)).thenReturn(false);

    assertThatThrownBy(() -> validator.accept(student))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("group not found");
  }
}
