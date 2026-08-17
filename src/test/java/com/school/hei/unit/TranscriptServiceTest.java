package com.school.hei.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.school.hei.entity.JCourse;
import com.school.hei.entity.JExam;
import com.school.hei.entity.JGrade;
import com.school.hei.entity.JGroup;
import com.school.hei.entity.JSpeciality;
import com.school.hei.entity.JStudent;
import com.school.hei.entity.JStudentGroupHistory;
import com.school.hei.security.CourseAccessService;
import com.school.hei.model.Transcript;
import com.school.hei.repository.CourseRepository;
import com.school.hei.repository.ExamRepository;
import com.school.hei.repository.GradeRepository;
import com.school.hei.repository.GroupExamRepository;
import com.school.hei.repository.GroupRepository;
import com.school.hei.repository.StudentGroupHistoryRepository;
import com.school.hei.repository.StudentRepository;
import com.school.hei.security.CourseAccessService;
import com.school.hei.service.services.TranscriptService;
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
class TranscriptServiceTest {

  @Mock private StudentRepository studentRepository;

  @Mock private CourseRepository courseRepository;

  @Mock private GradeRepository gradeRepository;

  @Mock private StudentGroupHistoryRepository studentGroupHistoryRepository;

  @Mock private GroupExamRepository groupExamRepository;

  @Mock private ExamRepository examRepository;

  @Mock private GroupRepository groupRepository;

  @Mock private CourseAccessService courseAccessService;

  @InjectMocks private TranscriptService transcriptService;

  private UUID studentId;
  private UUID groupId;
  private UUID specialityId;
  private UUID courseId;
  private UUID examId;

  private JStudent student;
  private JSpeciality speciality;
  private JGroup group;
  private JCourse course;
  private JExam exam;
  private JGrade grade;
  private JStudentGroupHistory history;

  @BeforeEach
  void setUp() {
    studentId = UUID.randomUUID();
    groupId = UUID.randomUUID();
    specialityId = UUID.randomUUID();
    courseId = UUID.randomUUID();
    examId = UUID.randomUUID();

    student =
            JStudent.builder()
                    .id(studentId)
                    .reference("STD-001")
                    .firstName("John")
                    .lastName("Doe")
                    .build();

    speciality = JSpeciality.builder().id(specialityId).build();

    group = JGroup.builder().id(groupId).name("Group K1").speciality(speciality).build();

    course =
            JCourse.builder()
                    .id(courseId)
                    .reference("CS101")
                    .title("Programming")
                    .credit(60)
                    .level(1)
                    .build();

    exam =
            JExam.builder()
                    .id(examId)
                    .date(LocalDate.of(2026, 8, 1))
                    .coeff(1.0)
                    .title("Final Exam")
                    .course(course)
                    .build();

    grade =
            JGrade.builder()
                    .id(UUID.randomUUID())
                    .value(15.0)
                    .date(LocalDate.of(2026, 8, 1))
                    .student(student)
                    .exam(exam)
                    .build();

    history =
            JStudentGroupHistory.builder()
                    .id(UUID.randomUUID())
                    .student(student)
                    .group(group)
                    .startDate(LocalDate.of(2026, 1, 1))
                    .endDate(null)
                    .build();
    lenient().when(courseAccessService.isAdmin()).thenReturn(true);
  }

  @Test
  void should_reject_null_level() {
    assertThatThrownBy(() -> transcriptService.getStudentTranscript(studentId, null))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("level must be 1, 2 or 3");

    verifyNoInteractions(studentRepository);
  }

  @Test
  void should_reject_invalid_level() {
    assertThatThrownBy(() -> transcriptService.getStudentTranscript(studentId, 4))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("level must be 1, 2 or 3");

    verifyNoInteractions(studentRepository);
  }

  @Test
  void should_reject_level_zero() {
    assertThatThrownBy(() -> transcriptService.getStudentTranscript(studentId, 0))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("level must be 1, 2 or 3");
  }

  @Test
  void should_throw_when_student_does_not_exist() {
    when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> transcriptService.getStudentTranscript(studentId, 1))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("student not found");

    verify(studentRepository).findById(studentId);
  }

  @Test
  void should_throw_when_student_has_no_group_history() {
    when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));

    when(studentGroupHistoryRepository.findByStudent_Id(studentId)).thenReturn(List.of());

    assertThatThrownBy(() -> transcriptService.getStudentTranscript(studentId, 1))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("student has no group history");
  }

  @Test
  void should_throw_when_student_group_year_is_not_complete() {
    when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));

    when(studentGroupHistoryRepository.findByStudent_Id(studentId)).thenReturn(List.of(history));

    when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));

    when(courseRepository.findBySpecialityIdAndLevel(specialityId, 1)).thenReturn(List.of());

    assertThatThrownBy(() -> transcriptService.getStudentTranscript(studentId, 1))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("no group of this student has completed the year for level 1");
  }

  @Test
  void should_return_false_when_group_has_no_speciality() {
    JGroup groupWithoutSpeciality = JGroup.builder().id(groupId).name("Group K1").build();

    when(groupRepository.findById(groupId)).thenReturn(Optional.of(groupWithoutSpeciality));

    boolean result = transcriptService.isGroupYearCompleteForLevel(groupId, 1);

    assertThat(result).isFalse();
  }

  @Test
  void should_return_false_when_group_has_no_courses() {
    when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));

    when(courseRepository.findBySpecialityIdAndLevel(specialityId, 1)).thenReturn(List.of());

    boolean result = transcriptService.isGroupYearCompleteForLevel(groupId, 1);

    assertThat(result).isFalse();
  }

  @Test
  void should_return_false_when_group_courses_do_not_total_60_credits() {
    JCourse smallCourse =
            JCourse.builder()
                    .id(courseId)
                    .reference("CS101")
                    .title("Programming")
                    .credit(30)
                    .level(1)
                    .build();

    when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));

    when(courseRepository.findBySpecialityIdAndLevel(specialityId, 1))
            .thenReturn(List.of(smallCourse));

    boolean result = transcriptService.isGroupYearCompleteForLevel(groupId, 1);

    assertThat(result).isFalse();
  }

  @Test
  void should_return_false_when_exam_coefficients_are_not_complete() {
    when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));

    when(courseRepository.findBySpecialityIdAndLevel(specialityId, 1)).thenReturn(List.of(course));

    JExam incompleteExam =
            JExam.builder()
                    .id(examId)
                    .date(LocalDate.of(2026, 8, 1))
                    .coeff(0.5)
                    .title("Partial Exam")
                    .course(course)
                    .build();

    when(examRepository.findByGroupId(groupId)).thenReturn(List.of(incompleteExam));

    boolean result = transcriptService.isGroupYearCompleteForLevel(groupId, 1);

    assertThat(result).isFalse();
  }

  @Test
  void should_return_true_when_group_year_is_complete() {
    when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));

    when(courseRepository.findBySpecialityIdAndLevel(specialityId, 1)).thenReturn(List.of(course));

    when(examRepository.findByGroupId(groupId)).thenReturn(List.of(exam));

    boolean result = transcriptService.isGroupYearCompleteForLevel(groupId, 1);

    assertThat(result).isTrue();
  }

  @Test
  void should_generate_student_transcript() {
    when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));

    when(studentGroupHistoryRepository.findByStudent_Id(studentId)).thenReturn(List.of(history));

    when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));

    when(courseRepository.findBySpecialityIdAndLevel(specialityId, 1)).thenReturn(List.of(course));

    when(examRepository.findByGroupId(groupId)).thenReturn(List.of(exam));

    when(gradeRepository.findByStudent_Id(studentId)).thenReturn(List.of(grade));

    when(studentGroupHistoryRepository.findStudentGroupAtDate(studentId, exam.getDate()))
            .thenReturn(Optional.of(history));

    when(groupExamRepository.findByGroup_IdAndExam_Id(groupId, examId))
            .thenReturn(Optional.of(mock(com.school.hei.entity.JGroupExam.class)));

    Transcript result = transcriptService.getStudentTranscript(studentId, 1);

    assertThat(result).isNotNull();
    assertThat(result.getStudentId()).isEqualTo(studentId);
    assertThat(result.getReference()).isEqualTo("STD-001");
    assertThat(result.getFirstName()).isEqualTo("John");
    assertThat(result.getLastName()).isEqualTo("Doe");
    assertThat(result.getLevel()).isEqualTo(1);

    assertThat(result.getCourses()).hasSize(1);
    assertThat(result.getCourses().get(0).getCourseId()).isEqualTo(courseId);

    assertThat(result.getCourses().get(0).getAverage()).isEqualTo(15.0);

    assertThat(result.getCourses().get(0).getObtainedCredit()).isEqualTo(60);

    assertThat(result.getGeneralAverage()).isEqualTo(15.0);

    assertThat(result.getTotalCredit()).isEqualTo(60);
  }

  @Test
  void should_throw_when_no_course_exists_for_student_path() {
    when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));

    when(studentGroupHistoryRepository.findByStudent_Id(studentId)).thenReturn(List.of(history));

    when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));

    when(courseRepository.findBySpecialityIdAndLevel(specialityId, 1)).thenReturn(List.of());

    assertThatThrownBy(() -> transcriptService.getStudentTranscript(studentId, 1))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("no group of this student has completed the year for level 1");
  }

  @Test
  void should_throw_when_group_does_not_exist() {
    when(groupRepository.findById(groupId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> transcriptService.isGroupYearCompleteForLevel(groupId, 1))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("group not found");
  }

  @Test
  void should_return_empty_list_when_no_students() {
    when(studentRepository.findAll()).thenReturn(List.of());

    List<Transcript> result = transcriptService.getAllTranscripts(1);

    assertThat(result).isEmpty();

    verify(studentRepository).findAll();
  }

  @Test
  void should_ignore_students_without_valid_transcript() {
    when(studentRepository.findAll()).thenReturn(List.of(student));

    when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));

    when(studentGroupHistoryRepository.findByStudent_Id(studentId)).thenReturn(List.of());

    List<Transcript> result = transcriptService.getAllTranscripts(1);

    assertThat(result).isEmpty();
  }

  @Test
  void should_reject_invalid_level_for_all_transcripts() {
    assertThatThrownBy(() -> transcriptService.getAllTranscripts(4))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("level must be 1, 2 or 3");

    verifyNoInteractions(studentRepository);
  }

  @Test
  void should_allow_admin_to_access_any_transcript() {
    when(courseAccessService.isAdmin()).thenReturn(true);

    assertThatCode(() -> transcriptService.assertCanAccessTranscript(studentId))
            .doesNotThrowAnyException();

    verifyNoInteractions(studentRepository);
  }

  @Test
  void should_allow_teacher_to_access_any_transcript() {
    when(courseAccessService.isAdmin()).thenReturn(false);
    when(courseAccessService.isTeacher()).thenReturn(true);

    assertThatCode(() -> transcriptService.assertCanAccessTranscript(studentId))
            .doesNotThrowAnyException();

    verify(courseAccessService, never()).currentUserId();
  }

  @Test
  void should_allow_student_to_access_own_transcript() {
    when(courseAccessService.isAdmin()).thenReturn(false);
    when(courseAccessService.isTeacher()).thenReturn(false);
    when(courseAccessService.isStudent()).thenReturn(true);
    when(courseAccessService.currentUserId()).thenReturn(studentId);

    assertThatCode(() -> transcriptService.assertCanAccessTranscript(studentId))
            .doesNotThrowAnyException();
  }

  @Test
  void should_forbid_student_from_accessing_another_students_transcript() {
    when(courseAccessService.isAdmin()).thenReturn(false);
    when(courseAccessService.isTeacher()).thenReturn(false);
    when(courseAccessService.isStudent()).thenReturn(true);
    when(courseAccessService.currentUserId()).thenReturn(UUID.randomUUID());

    assertThatThrownBy(() -> transcriptService.assertCanAccessTranscript(studentId))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("not your transcript");
  }

  @Test
  void should_forbid_access_when_user_has_no_recognized_role() {
    when(courseAccessService.isAdmin()).thenReturn(false);
    when(courseAccessService.isTeacher()).thenReturn(false);
    when(courseAccessService.isStudent()).thenReturn(false);

    assertThatThrownBy(() -> transcriptService.assertCanAccessTranscript(studentId))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("access denied");

    verify(courseAccessService, never()).currentUserId();
  }

  @Test
  void should_forbid_student_from_reading_another_students_transcript() {
    when(courseAccessService.isAdmin()).thenReturn(false);
    when(courseAccessService.isTeacher()).thenReturn(false);
    when(courseAccessService.isStudent()).thenReturn(true);
    when(courseAccessService.currentUserId()).thenReturn(UUID.randomUUID());

    assertThatThrownBy(() -> transcriptService.getStudentTranscript(studentId, 1))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("not your transcript");

    verifyNoInteractions(studentRepository);
  }
}