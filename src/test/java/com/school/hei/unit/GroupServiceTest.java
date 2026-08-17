package com.school.hei.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.school.hei.entity.JGroup;
import com.school.hei.entity.JSpeciality;
import com.school.hei.entity.JStudent;
import com.school.hei.entity.JStudentGroupHistory;
import com.school.hei.enums.GroupSpeciality;
import com.school.hei.model.Group;
import com.school.hei.model.Speciality;
import com.school.hei.model.Student;
import com.school.hei.repository.CourseRepository;
import com.school.hei.repository.ExamRepository;
import com.school.hei.repository.GroupRepository;
import com.school.hei.repository.SpecialityRepository;
import com.school.hei.repository.StudentGroupHistoryRepository;
import com.school.hei.repository.StudentRepository;
import com.school.hei.service.services.GroupService;
import com.school.hei.validator.GroupValidator;
import com.school.hei.validator.SpecialityChangeValidator;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

  @Mock private GroupRepository groupRepository;

  @Mock private GroupValidator groupValidator;

  @Mock private StudentRepository studentRepository;

  @Mock private SpecialityRepository specialityRepository;

  @Mock private ExamRepository examRepository;

  @Mock private CourseRepository courseRepository;

  @Mock private StudentGroupHistoryRepository studentGroupHistoryRepository;

  @Mock private SpecialityChangeValidator specialityChangeValidator;

  @InjectMocks private GroupService groupService;

  @Test
  void should_find_all_groups() {
    UUID groupId = UUID.randomUUID();
    UUID specialityId = UUID.randomUUID();

    JSpeciality speciality = JSpeciality.builder().id(specialityId).name("EL").build();

    JGroup group = JGroup.builder().id(groupId).name("G1").speciality(speciality).build();

    when(groupRepository.findAll()).thenReturn(List.of(group));
    when(studentRepository.findByGroup_Id(groupId)).thenReturn(List.of());

    List<Group> result = groupService.findAll();

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getId()).isEqualTo(groupId);
    assertThat(result.get(0).getName()).isEqualTo("G1");

    verify(groupRepository).findAll();
    verify(studentRepository).findByGroup_Id(groupId);
  }

  @Test
  void should_find_group_by_id() {
    UUID groupId = UUID.randomUUID();

    JGroup group = JGroup.builder().id(groupId).name("G1").build();

    when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
    when(studentRepository.findByGroup_Id(groupId)).thenReturn(List.of());

    Group result = groupService.findById(groupId);

    assertThat(result.getId()).isEqualTo(groupId);
    assertThat(result.getName()).isEqualTo("G1");
  }

  @Test
  void should_throw_when_group_not_found() {
    UUID groupId = UUID.randomUUID();

    when(groupRepository.findById(groupId)).thenReturn(Optional.empty());

    assertThrows(ResponseStatusException.class, () -> groupService.findById(groupId));
  }

  @Test
  void should_save_group_without_students() {
    UUID groupId = UUID.randomUUID();
    UUID specialityId = UUID.randomUUID();

    Speciality speciality = Speciality.builder().id(specialityId).name(GroupSpeciality.EL).build();

    Group group = Group.builder().name("G1").speciality(speciality).students(List.of()).build();

    JGroup savedGroup = JGroup.builder().id(groupId).name("G1").build();

    when(groupRepository.save(any(JGroup.class))).thenReturn(savedGroup);
    when(studentRepository.findByGroup_Id(groupId)).thenReturn(List.of());

    Group result = groupService.save(group);

    assertThat(result.getId()).isEqualTo(groupId);
    assertThat(result.getName()).isEqualTo("G1");

    verify(groupValidator).accept(group);
    verify(groupRepository).save(any(JGroup.class));
  }

  @Test
  void should_update_group() {
    UUID groupId = UUID.randomUUID();

    Group group = Group.builder().name("Updated Group").students(List.of()).build();

    JGroup existing = JGroup.builder().id(groupId).name("Old Group").build();

    JGroup updated = JGroup.builder().id(groupId).name("Updated Group").build();

    when(groupRepository.findById(groupId)).thenReturn(Optional.of(existing));
    when(studentRepository.findByGroup_Id(groupId)).thenReturn(List.of());
    when(groupRepository.save(any(JGroup.class))).thenReturn(updated);

    Group result = groupService.update(groupId, group);

    assertThat(result.getId()).isEqualTo(groupId);
    assertThat(result.getName()).isEqualTo("Updated Group");
    assertThat(group.getId()).isEqualTo(groupId);

    verify(groupValidator).accept(group);
    verify(groupRepository).save(any(JGroup.class));
  }

  @Test
  void should_delete_group() {
    UUID groupId = UUID.randomUUID();

    JStudent student = JStudent.builder().id(UUID.randomUUID()).build();

    when(groupRepository.existsById(groupId)).thenReturn(true);
    when(studentRepository.findByGroup_Id(groupId)).thenReturn(List.of(student));

    groupService.delete(groupId);

    assertThat(student.getGroup()).isNull();

    verify(studentRepository).save(student);
    verify(groupRepository).deleteById(groupId);
  }

  @Test
  void should_throw_when_deleting_unknown_group() {
    UUID groupId = UUID.randomUUID();

    when(groupRepository.existsById(groupId)).thenReturn(false);

    assertThrows(ResponseStatusException.class, () -> groupService.delete(groupId));

    verify(groupRepository, never()).deleteById(groupId);
  }

  @Test
  void should_find_groups_by_speciality() {
    UUID specialityId = UUID.randomUUID();
    UUID groupId = UUID.randomUUID();

    JGroup group = JGroup.builder().id(groupId).name("EL Group").build();

    when(specialityRepository.existsById(specialityId)).thenReturn(true);
    when(groupRepository.findBySpeciality_Id(specialityId)).thenReturn(List.of(group));
    when(studentRepository.findByGroup_Id(groupId)).thenReturn(List.of());

    List<Group> result = groupService.findBySpeciality(specialityId);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getName()).isEqualTo("EL Group");
  }

  @Test
  void should_throw_when_speciality_not_found() {
    UUID specialityId = UUID.randomUUID();

    when(specialityRepository.existsById(specialityId)).thenReturn(false);

    assertThrows(ResponseStatusException.class, () -> groupService.findBySpeciality(specialityId));
  }

  @Test
  void should_find_groups_by_exam() {
    UUID examId = UUID.randomUUID();
    UUID groupId = UUID.randomUUID();

    JGroup group = JGroup.builder().id(groupId).name("Exam Group").build();

    when(examRepository.existsById(examId)).thenReturn(true);
    when(groupRepository.findByExamId(examId)).thenReturn(List.of(group));
    when(studentRepository.findByGroup_Id(groupId)).thenReturn(List.of());

    List<Group> result = groupService.findByExam(examId);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getName()).isEqualTo("Exam Group");
  }

  @Test
  void should_throw_when_exam_not_found() {
    UUID examId = UUID.randomUUID();

    when(examRepository.existsById(examId)).thenReturn(false);

    assertThrows(ResponseStatusException.class, () -> groupService.findByExam(examId));
  }

  @Test
  void should_find_groups_by_course() {
    UUID courseId = UUID.randomUUID();
    UUID groupId = UUID.randomUUID();

    JGroup group = JGroup.builder().id(groupId).name("Course Group").build();

    when(courseRepository.existsById(courseId)).thenReturn(true);
    when(groupRepository.findByCourseId(courseId)).thenReturn(List.of(group));
    when(studentRepository.findByGroup_Id(groupId)).thenReturn(List.of());

    List<Group> result = groupService.findByCourse(courseId);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getName()).isEqualTo("Course Group");
  }

  @Test
  void should_throw_when_course_not_found() {
    UUID courseId = UUID.randomUUID();

    when(courseRepository.existsById(courseId)).thenReturn(false);

    assertThrows(ResponseStatusException.class, () -> groupService.findByCourse(courseId));
  }

  @Test
  void should_find_groups_by_name() {
    UUID groupId = UUID.randomUUID();

    JGroup group = JGroup.builder().id(groupId).name("Computer Science").build();

    when(groupRepository.findByNameContainingIgnoreCase("Computer")).thenReturn(List.of(group));
    when(studentRepository.findByGroup_Id(groupId)).thenReturn(List.of());

    List<Group> result = groupService.findByName("Computer");

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getName()).isEqualTo("Computer Science");
  }

  @Test
  void should_throw_when_group_name_is_blank() {
    assertThrows(ResponseStatusException.class, () -> groupService.findByName(" "));
  }

  @Test
  void should_assign_student_to_group_and_create_history() {
    UUID groupId = UUID.randomUUID();
    UUID studentId = UUID.randomUUID();

    JGroup group = JGroup.builder().id(groupId).name("G1").build();

    JStudent student = JStudent.builder().id(studentId).build();

    Student studentModel = Student.builder().id(studentId).build();

    Group groupModel = Group.builder().name("G1").students(List.of(studentModel)).build();

    JGroup savedGroup = JGroup.builder().id(groupId).name("G1").build();

    when(groupRepository.save(any(JGroup.class))).thenReturn(savedGroup);
    when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
    when(studentGroupHistoryRepository.findByStudent_Id(studentId)).thenReturn(List.of());
    when(studentRepository.findByGroup_Id(groupId)).thenReturn(List.of());

    Group result = groupService.save(groupModel);

    assertThat(result.getId()).isEqualTo(groupId);

    verify(specialityChangeValidator).accept(student, savedGroup);
    verify(studentGroupHistoryRepository).save(any(JStudentGroupHistory.class));
    verify(studentRepository).save(student);

    assertThat(student.getGroup()).isEqualTo(savedGroup);
  }

  @Test
  void should_close_old_history_when_student_changes_group() {
    UUID groupId = UUID.randomUUID();
    UUID oldGroupId = UUID.randomUUID();
    UUID studentId = UUID.randomUUID();

    JGroup oldGroup = JGroup.builder().id(oldGroupId).name("Old Group").build();

    JGroup newGroup = JGroup.builder().id(groupId).name("New Group").build();

    JStudent student = JStudent.builder().id(studentId).group(oldGroup).build();

    JStudentGroupHistory oldHistory =
        JStudentGroupHistory.builder()
            .id(UUID.randomUUID())
            .student(student)
            .group(oldGroup)
            .startDate(LocalDate.now().minusMonths(1))
            .endDate(null)
            .build();

    Student studentModel = Student.builder().id(studentId).build();

    Group groupModel = Group.builder().name("New Group").students(List.of(studentModel)).build();

    when(groupRepository.save(any(JGroup.class))).thenReturn(newGroup);
    when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
    when(studentGroupHistoryRepository.findByStudent_Id(studentId)).thenReturn(List.of(oldHistory));
    when(studentRepository.findByGroup_Id(groupId)).thenReturn(List.of());

    groupService.save(groupModel);

    assertThat(oldHistory.getEndDate()).isEqualTo(LocalDate.now().minusDays(1));

    verify(studentGroupHistoryRepository, times(2)).save(any(JStudentGroupHistory.class));

    verify(studentRepository).save(student);
  }
}


