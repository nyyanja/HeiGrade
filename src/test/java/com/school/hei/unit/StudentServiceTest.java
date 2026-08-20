package com.school.hei.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.school.hei.entity.JGroup;
import com.school.hei.entity.JStudent;
import com.school.hei.entity.JStudentGroupHistory;
import com.school.hei.model.Group;
import com.school.hei.model.Student;
import com.school.hei.repository.GroupRepository;
import com.school.hei.repository.SpecialityRepository;
import com.school.hei.repository.StudentGroupHistoryRepository;
import com.school.hei.repository.StudentRepository;
import com.school.hei.security.CourseAccessService;
import com.school.hei.service.services.StudentService;
import com.school.hei.validator.SpecialityChangeValidator;
import com.school.hei.validator.StudentValidator;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

  @Mock private StudentRepository studentRepository;
  @Mock private StudentValidator studentValidator;
  @Mock private GroupRepository groupRepository;
  @Mock private SpecialityRepository specialityRepository;
  @Mock private StudentGroupHistoryRepository studentGroupHistoryRepository;
  @Mock private SpecialityChangeValidator specialityChangeValidator;
  @Mock private CourseAccessService courseAccessService;
  @Mock private PasswordEncoder passwordEncoder;

  @InjectMocks private StudentService studentService;

  @BeforeEach
  void setUp() {
    lenient().when(courseAccessService.isAdmin()).thenReturn(true);
  }

  @Test
  void should_find_all_students() {
    JStudent student1 = createStudentEntity(UUID.randomUUID(), "STU001");
    JStudent student2 = createStudentEntity(UUID.randomUUID(), "STU002");

    when(studentRepository.findAll()).thenReturn(List.of(student1, student2));

    List<Student> result = studentService.findAll();

    assertThat(result).hasSize(2);
    assertThat(result).extracting(Student::getReference).containsExactly("STU001", "STU002");
    verify(studentRepository).findAll();
  }

  @Test
  void should_find_student_by_id() {
    UUID id = UUID.randomUUID();
    JStudent student = createStudentEntity(id, "STU001");

    when(studentRepository.findById(id)).thenReturn(Optional.of(student));

    Student result = studentService.findById(id);

    assertThat(result.getId()).isEqualTo(id);
    assertThat(result.getReference()).isEqualTo("STU001");
    verify(studentRepository).findById(id);
  }

  @Test
  void should_throw_when_student_is_not_found_by_id() {
    UUID id = UUID.randomUUID();
    when(studentRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(ResponseStatusException.class, () -> studentService.findById(id));
    verify(studentRepository).findById(id);
  }

  @Test
  void should_find_student_by_reference() {
    JStudent student = createStudentEntity(UUID.randomUUID(), "STU001");
    when(studentRepository.findByReference("STU001")).thenReturn(Optional.of(student));

    Student result = studentService.findByReference("STU001");

    assertThat(result.getReference()).isEqualTo("STU001");
    verify(studentRepository).findByReference("STU001");
  }

  @Test
  void should_throw_when_student_is_not_found_by_reference() {
    when(studentRepository.findByReference("UNKNOWN")).thenReturn(Optional.empty());
    assertThrows(ResponseStatusException.class, () -> studentService.findByReference("UNKNOWN"));
  }

  @Test
  void should_find_students_by_name() {
    JStudent student1 = createStudentEntity(UUID.randomUUID(), "STU001");
    JStudent student2 = createStudentEntity(UUID.randomUUID(), "STU002");

    when(studentRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            "John", "John"))
        .thenReturn(List.of(student1, student2));

    List<Student> result = studentService.findByName("John");

    assertThat(result).hasSize(2);
    verify(studentRepository)
        .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("John", "John");
  }

  @Test
  void should_throw_when_search_name_is_blank() {
    assertThrows(ResponseStatusException.class, () -> studentService.findByName(""));
    verifyNoInteractions(studentRepository);
  }

  @Test
  void should_throw_when_search_name_is_null() {
    assertThrows(ResponseStatusException.class, () -> studentService.findByName(null));
    verifyNoInteractions(studentRepository);
  }

  @Test
  void should_find_students_by_group() {
    UUID groupId = UUID.randomUUID();
    JStudent student1 = createStudentEntity(UUID.randomUUID(), "STU001");
    JStudent student2 = createStudentEntity(UUID.randomUUID(), "STU002");

    when(groupRepository.existsById(groupId)).thenReturn(true);
    when(studentRepository.findByGroup_Id(groupId)).thenReturn(List.of(student1, student2));

    List<Student> result = studentService.findByGroup(groupId);

    assertThat(result).hasSize(2);
    verify(groupRepository).existsById(groupId);
    verify(studentRepository).findByGroup_Id(groupId);
  }

  @Test
  void should_throw_when_group_is_not_found() {
    UUID groupId = UUID.randomUUID();
    when(groupRepository.existsById(groupId)).thenReturn(false);

    assertThrows(ResponseStatusException.class, () -> studentService.findByGroup(groupId));
    verify(groupRepository).existsById(groupId);
    verify(studentRepository, never()).findByGroup_Id(groupId);
  }

  @Test
  void should_find_students_by_speciality() {
    UUID specialityId = UUID.randomUUID();
    JStudent student1 = createStudentEntity(UUID.randomUUID(), "STU001");
    JStudent student2 = createStudentEntity(UUID.randomUUID(), "STU002");

    when(specialityRepository.existsById(specialityId)).thenReturn(true);
    when(studentRepository.findByGroup_Speciality_Id(specialityId))
        .thenReturn(List.of(student1, student2));

    List<Student> result = studentService.findBySpeciality(specialityId);

    assertThat(result).hasSize(2);
    verify(specialityRepository).existsById(specialityId);
    verify(studentRepository).findByGroup_Speciality_Id(specialityId);
  }

  @Test
  void should_throw_when_speciality_is_not_found() {
    UUID specialityId = UUID.randomUUID();
    when(specialityRepository.existsById(specialityId)).thenReturn(false);

    assertThrows(
        ResponseStatusException.class, () -> studentService.findBySpeciality(specialityId));
    verify(specialityRepository).existsById(specialityId);
    verify(studentRepository, never()).findByGroup_Speciality_Id(specialityId);
  }

  @Test
  void should_save_student_with_group() {
    UUID studentId = UUID.randomUUID();
    UUID groupId = UUID.randomUUID();

    Student student = createStudentModel("STU001", groupId);
    student.setPassword("plainPassword123");

    JGroup group = createGroupEntity(groupId);

    JStudent savedStudent =
        JStudent.builder()
            .id(studentId)
            .firstName("John")
            .lastName("Doe")
            .reference("STU001")
            .group(group)
            .password("encodedPassword")
            .build();

    when(passwordEncoder.encode("plainPassword123")).thenReturn("encodedPassword");
    when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
    when(studentRepository.save(any(JStudent.class))).thenReturn(savedStudent);
    when(studentGroupHistoryRepository.findByStudent_IdAndEndDateIsNull(studentId))
        .thenReturn(Optional.empty());

    Student result = studentService.save(student);

    assertThat(result.getId()).isEqualTo(studentId);
    assertThat(result.getReference()).isEqualTo("STU001");

    verify(studentValidator).accept(student);
    verify(passwordEncoder).encode("plainPassword123");
    verify(groupRepository).findById(groupId);
    verify(specialityChangeValidator).accept(any(JStudent.class), eq(group));
    verify(studentRepository).save(any(JStudent.class));
    verify(studentGroupHistoryRepository).findByStudent_IdAndEndDateIsNull(studentId);
    verify(studentGroupHistoryRepository).save(any(JStudentGroupHistory.class));
  }

  @Test
  void should_save_student_without_group() {
    UUID studentId = UUID.randomUUID();

    Student student = Student.builder().reference("STU001").password("plainPassword123").build();

    JStudent savedStudent =
        JStudent.builder()
            .id(studentId)
            .reference("STU001")
            .group(null)
            .password("encodedPassword")
            .build();

    when(passwordEncoder.encode("plainPassword123")).thenReturn("encodedPassword");
    when(studentRepository.save(any(JStudent.class))).thenReturn(savedStudent);

    Student result = studentService.save(student);

    assertThat(result.getId()).isEqualTo(studentId);
    assertThat(result.getReference()).isEqualTo("STU001");

    verify(studentValidator).accept(student);
    verify(passwordEncoder).encode("plainPassword123");
    verify(studentRepository).save(any(JStudent.class));
    verifyNoInteractions(specialityChangeValidator);
    verifyNoInteractions(studentGroupHistoryRepository);
  }

  @Test
  void should_throw_when_password_is_missing_on_save() {
    Student student = Student.builder().reference("STU001").build(); // no password

    assertThrows(ResponseStatusException.class, () -> studentService.save(student));
    verify(passwordEncoder, never()).encode(anyString());
    verify(studentRepository, never()).save(any());
  }

  @Test
  void should_not_create_duplicate_initial_group_history() {
    UUID studentId = UUID.randomUUID();
    UUID groupId = UUID.randomUUID();

    Student student = createStudentModel("STU001", groupId);
    student.setPassword("plainPassword123");

    JGroup group = createGroupEntity(groupId);

    JStudent savedStudent =
        JStudent.builder().id(studentId).reference("STU001").group(group).build();

    JStudentGroupHistory existingHistory =
        JStudentGroupHistory.builder()
            .id(UUID.randomUUID())
            .student(savedStudent)
            .group(group)
            .startDate(LocalDate.now().minusDays(10))
            .build();

    when(passwordEncoder.encode(anyString())).thenReturn("encoded");
    when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
    when(studentRepository.save(any(JStudent.class))).thenReturn(savedStudent);
    when(studentGroupHistoryRepository.findByStudent_IdAndEndDateIsNull(studentId))
        .thenReturn(Optional.of(existingHistory));

    studentService.save(student);

    verify(studentGroupHistoryRepository).findByStudent_IdAndEndDateIsNull(studentId);
    verify(studentGroupHistoryRepository, never()).save(any(JStudentGroupHistory.class));
  }

  @Test
  void should_save_multiple_students() {
    UUID groupId = UUID.randomUUID();

    Student student1 = createStudentModel("STU001", groupId);
    student1.setPassword("pass1");
    Student student2 = createStudentModel("STU002", groupId);
    student2.setPassword("pass2");

    JGroup group = createGroupEntity(groupId);

    JStudent savedStudent1 =
        JStudent.builder().id(UUID.randomUUID()).reference("STU001").group(group).build();
    JStudent savedStudent2 =
        JStudent.builder().id(UUID.randomUUID()).reference("STU002").group(group).build();

    when(passwordEncoder.encode(anyString())).thenReturn("encoded");
    when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
    when(studentRepository.saveAll(anyList())).thenReturn(List.of(savedStudent1, savedStudent2));
    when(studentGroupHistoryRepository.findByStudent_IdAndEndDateIsNull(any(UUID.class)))
        .thenReturn(Optional.empty());

    List<Student> result = studentService.saveAll(List.of(student1, student2));

    assertThat(result).hasSize(2);
    assertThat(result).extracting(Student::getReference).containsExactly("STU001", "STU002");

    verify(studentValidator, times(2)).accept(any(Student.class));
    verify(passwordEncoder, times(2)).encode(anyString());
    verify(studentRepository).saveAll(anyList());
    verify(studentGroupHistoryRepository, times(2)).save(any(JStudentGroupHistory.class));
  }

  @Test
  void should_throw_when_saving_empty_student_list() {
    assertThrows(ResponseStatusException.class, () -> studentService.saveAll(List.of()));
    verifyNoInteractions(studentRepository);
    verifyNoInteractions(studentValidator);
  }

  @Test
  void should_throw_when_saving_null_student_list() {
    assertThrows(ResponseStatusException.class, () -> studentService.saveAll(null));
    verifyNoInteractions(studentRepository);
  }

  @Test
  void should_update_student_without_group_change() {
    UUID studentId = UUID.randomUUID();
    UUID groupId = UUID.randomUUID();

    JGroup group = createGroupEntity(groupId);

    JStudent existingStudent =
        JStudent.builder()
            .id(studentId)
            .reference("STU001")
            .group(group)
            .password("oldEncodedPassword")
            .build();

    Student updatedModel = createStudentModel("STU001-UPDATED", groupId);

    JStudent updatedEntity =
        JStudent.builder()
            .id(studentId)
            .reference("STU001-UPDATED")
            .group(group)
            .password("oldEncodedPassword")
            .build();

    when(studentRepository.findById(studentId)).thenReturn(Optional.of(existingStudent));
    when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
    when(studentRepository.save(any(JStudent.class))).thenReturn(updatedEntity);

    Student result = studentService.update(studentId, updatedModel);

    assertThat(result.getId()).isEqualTo(studentId);
    assertThat(result.getReference()).isEqualTo("STU001-UPDATED");

    verify(studentValidator).accept(updatedModel);
    verify(passwordEncoder, never()).encode(anyString()); // important
    verify(groupRepository).findById(groupId);
    verify(studentRepository).save(any(JStudent.class));
    verify(specialityChangeValidator, never()).accept(any(JStudent.class), any(JGroup.class));
    verifyNoInteractions(studentGroupHistoryRepository);
  }

  @Test
  void should_update_student_and_change_password_when_provided() {
    UUID studentId = UUID.randomUUID();
    UUID groupId = UUID.randomUUID();

    JGroup group = createGroupEntity(groupId);

    JStudent existingStudent =
        JStudent.builder()
            .id(studentId)
            .reference("STU001")
            .group(group)
            .password("oldEncoded")
            .build();

    Student updatedModel = createStudentModel("STU001", groupId);
    updatedModel.setPassword("newPlainPassword");

    when(studentRepository.findById(studentId)).thenReturn(Optional.of(existingStudent));
    when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
    when(passwordEncoder.encode("newPlainPassword")).thenReturn("newEncoded");
    when(studentRepository.save(any(JStudent.class))).thenAnswer(inv -> inv.getArgument(0));

    Student result = studentService.update(studentId, updatedModel);

    assertThat(result.getId()).isEqualTo(studentId);
    verify(passwordEncoder).encode("newPlainPassword");
    verify(studentRepository).save(any(JStudent.class));
  }

  @Test
  void should_update_student_when_group_changes() {
    UUID studentId = UUID.randomUUID();
    UUID oldGroupId = UUID.randomUUID();
    UUID newGroupId = UUID.randomUUID();

    JGroup oldGroup = createGroupEntity(oldGroupId);
    JGroup newGroup = createGroupEntity(newGroupId);

    JStudent existingStudent =
        JStudent.builder().id(studentId).reference("STU001").group(oldGroup).build();

    Student updatedModel = createStudentModel("STU001", newGroupId);

    JStudent updatedStudent =
        JStudent.builder().id(studentId).reference("STU001").group(newGroup).build();

    JStudentGroupHistory currentHistory =
        JStudentGroupHistory.builder()
            .id(UUID.randomUUID())
            .student(existingStudent)
            .group(oldGroup)
            .startDate(LocalDate.now().minusDays(30))
            .endDate(null)
            .build();

    when(studentRepository.findById(studentId)).thenReturn(Optional.of(existingStudent));
    when(groupRepository.findById(newGroupId)).thenReturn(Optional.of(newGroup));
    when(studentRepository.save(any(JStudent.class))).thenReturn(updatedStudent);
    when(studentGroupHistoryRepository.findByStudent_IdAndEndDateIsNull(studentId))
        .thenReturn(Optional.of(currentHistory));

    Student result = studentService.update(studentId, updatedModel);

    assertThat(result.getGroup().getId()).isEqualTo(newGroupId);
    verify(studentGroupHistoryRepository).save(currentHistory);
    verify(studentGroupHistoryRepository, times(2)).save(any(JStudentGroupHistory.class));
  }

  @Test
  void should_throw_when_updating_unknown_student() {
    UUID studentId = UUID.randomUUID();
    Student student = Student.builder().reference("STU001").build();

    when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

    assertThrows(ResponseStatusException.class, () -> studentService.update(studentId, student));
    verify(studentRepository).findById(studentId);
    verify(studentRepository, never()).save(any(JStudent.class));
    verifyNoInteractions(studentValidator);
  }

  @Test
  void should_delete_student() {
    UUID studentId = UUID.randomUUID();
    when(studentRepository.existsById(studentId)).thenReturn(true);

    studentService.delete(studentId);

    verify(studentRepository).existsById(studentId);
    verify(studentRepository).deleteById(studentId);
  }

  @Test
  void should_throw_when_deleting_unknown_student() {
    UUID studentId = UUID.randomUUID();
    when(studentRepository.existsById(studentId)).thenReturn(false);

    assertThrows(ResponseStatusException.class, () -> studentService.delete(studentId));
    verify(studentRepository).existsById(studentId);
    verify(studentRepository, never()).deleteById(studentId);
  }

  private Student createStudentModel(String reference, UUID groupId) {
    return Student.builder()
        .firstName("John")
        .lastName("Doe")
        .reference(reference)
        .group(Group.builder().id(groupId).name("Group A").build())
        .build();
  }

  private JStudent createStudentEntity(UUID id, String reference) {
    return JStudent.builder().id(id).firstName("John").lastName("Doe").reference(reference).build();
  }

  private JGroup createGroupEntity(UUID id) {
    return JGroup.builder().id(id).name("Group A").build();
  }
}
