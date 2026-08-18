package com.school.hei.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.school.hei.entity.JCourse;
import com.school.hei.entity.JExam;
import com.school.hei.entity.JGrade;
import com.school.hei.entity.JGradeHistory;
import com.school.hei.entity.JGroup;
import com.school.hei.entity.JSpeciality;
import com.school.hei.entity.JStudent;
import com.school.hei.entity.JUser;
import com.school.hei.enums.Role;
import com.school.hei.enums.Sex;
import com.school.hei.mapper.GradeMapper;
import com.school.hei.model.Grade;
import com.school.hei.repository.CourseRepository;
import com.school.hei.repository.ExamRepository;
import com.school.hei.repository.GradeHistoryRepository;
import com.school.hei.repository.GradeRepository;
import com.school.hei.repository.GroupRepository;
import com.school.hei.repository.SpecialityRepository;
import com.school.hei.repository.StudentRepository;
import com.school.hei.repository.UserRepository;
import com.school.hei.security.CourseAccessService;
import com.school.hei.service.services.GradeService;
import com.school.hei.validator.GradeHistoryValidator;
import com.school.hei.validator.GradeValidator;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
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
class GradeServiceTest {

  @Mock private GradeRepository gradeRepository;
  @Mock private GradeHistoryRepository gradeHistoryRepository;
  @Mock private GradeValidator gradeValidator;
  @Mock private GradeHistoryValidator gradeHistoryValidator;
  @Mock private ExamRepository examRepository;
  @Mock private StudentRepository studentRepository;
  @Mock private CourseRepository courseRepository;
  @Mock private GroupRepository groupRepository;
  @Mock private SpecialityRepository specialityRepository;
  @Mock private UserRepository userRepository;
  @Mock private CourseAccessService courseAccessService;

  @InjectMocks private GradeService gradeService;

  private UUID gradeId;
  private UUID studentId;
  private UUID examId;
  private UUID courseId;
  private UUID groupId;
  private UUID specialityId;
  private UUID userId;

  private JStudent student;
  private JExam exam;
  private JCourse course;
  private JGrade gradeEntity;

  @BeforeEach
  void setUp() {
    gradeId = UUID.randomUUID();
    studentId = UUID.randomUUID();
    examId = UUID.randomUUID();
    courseId = UUID.randomUUID();
    groupId = UUID.randomUUID();
    specialityId = UUID.randomUUID();
    userId = UUID.randomUUID();

    student =
            JStudent.builder()
                    .id(studentId)
                    .firstName("John")
                    .lastName("Doe")
                    .email("john.doe@gmail.com")
                    .sex(Sex.MALE)
                    .role(Role.STUDENT)
                    .build();

    course =
            JCourse.builder()
                    .id(courseId)
                    .reference("CS101")
                    .title("Programming")
                    .credit(6)
                    .level(1)
                    .build();

    exam =
            JExam.builder()
                    .id(examId)
                    .date(LocalDate.of(2026, 8, 1))
                    .coeff(1.0)
                    .title("Exam 1")
                    .course(course)
                    .build();

    gradeEntity =
            JGrade.builder()
                    .id(gradeId)
                    .value(15.0)
                    .date(LocalDate.of(2026, 8, 1))
                    .student(student)
                    .exam(exam)
                    .build();

    lenient().when(courseAccessService.isAdmin()).thenReturn(true);
    lenient().when(examRepository.findById(examId)).thenReturn(Optional.of(exam));
  }

  // ---------------------------------------------------------------------------
  // FIND ALL
  // ---------------------------------------------------------------------------

  @Test
  void should_find_all_grades() {
    when(gradeRepository.findAll()).thenReturn(List.of(gradeEntity));

    List<Grade> result = gradeService.findAll();

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getId()).isEqualTo(gradeId);
    assertThat(result.get(0).getValue()).isEqualTo(15.0);

    verify(gradeRepository).findAll();
  }

  @Test
  void should_return_empty_list_when_no_grades() {
    when(gradeRepository.findAll()).thenReturn(List.of());

    List<Grade> result = gradeService.findAll();

    assertThat(result).isEmpty();
  }

  // ---------------------------------------------------------------------------
  // FIND BY ID
  // ---------------------------------------------------------------------------

  @Test
  void should_find_grade_by_id() {
    when(gradeRepository.findById(gradeId)).thenReturn(Optional.of(gradeEntity));

    Grade result = gradeService.findById(gradeId);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(gradeId);
    assertThat(result.getValue()).isEqualTo(15.0);

    verify(gradeRepository).findById(gradeId);
  }

  @Test
  void should_throw_when_grade_id_is_null() {
    assertThatThrownBy(() -> gradeService.findById(null))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("grade id is required");
  }

  @Test
  void should_throw_when_grade_does_not_exist() {
    when(gradeRepository.findById(gradeId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> gradeService.findById(gradeId))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("grade not found");
  }

  // ---------------------------------------------------------------------------
  // SAVE
  // ---------------------------------------------------------------------------

  @Test
  void should_save_grade() {
    Grade grade = GradeMapper.toModel(gradeEntity);

    when(gradeRepository.save(any(JGrade.class))).thenReturn(gradeEntity);

    Grade result = gradeService.save(grade);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(gradeId);
    assertThat(result.getValue()).isEqualTo(15.0);

    verify(gradeValidator).accept(grade);
    verify(gradeRepository).save(any(JGrade.class));
  }

  @Test
  void should_set_current_date_when_saving_grade_without_date() {
    Grade grade =
            Grade.builder()
                    .value(15.0)
                    .student(GradeMapper.toModel(gradeEntity).getStudent())
                    .exam(GradeMapper.toModel(gradeEntity).getExam())
                    .build();

    when(gradeRepository.save(any(JGrade.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

    Grade result = gradeService.save(grade);

    assertThat(result.getDate()).isEqualTo(LocalDate.now());

    verify(gradeValidator).accept(grade);
    verify(gradeRepository).save(any(JGrade.class));
  }

  @Test
  void should_throw_when_saving_null_grade() {
    assertThatThrownBy(() -> gradeService.save(null))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("grade cannot be null");

    verifyNoInteractions(gradeRepository);
  }

  // ---------------------------------------------------------------------------
  // SAVE ALL
  // ---------------------------------------------------------------------------

  @Test
  void should_save_all_grades() {
    Grade grade1 = GradeMapper.toModel(gradeEntity);

    JGrade secondEntity =
            JGrade.builder()
                    .id(UUID.randomUUID())
                    .value(12.0)
                    .date(LocalDate.of(2026, 8, 2))
                    .student(student)
                    .exam(exam)
                    .build();

    Grade grade2 = GradeMapper.toModel(secondEntity);

    when(gradeRepository.save(any(JGrade.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

    List<Grade> result = gradeService.saveAll(List.of(grade1, grade2));

    assertThat(result).hasSize(2);

    verify(gradeValidator, times(2)).accept(any(Grade.class));
    verify(gradeRepository, times(2)).save(any(JGrade.class));
  }

  @Test
  void should_throw_when_saving_empty_grade_list() {
    assertThatThrownBy(() -> gradeService.saveAll(List.of()))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("at least one grade is required");
  }

  @Test
  void should_throw_when_saving_null_grade_list() {
    assertThatThrownBy(() -> gradeService.saveAll(null))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("at least one grade is required");
  }

  // ---------------------------------------------------------------------------
  // UPDATE
  // ---------------------------------------------------------------------------

  @Test
  void should_update_grade_and_create_history_when_value_changes() {
    Grade updatedGrade = GradeMapper.toModel(gradeEntity);
    updatedGrade.setValue(17.0);

    JUser modifier =
            JUser.builder()
                    .id(userId)
                    .firstName("Teacher")
                    .lastName("Modifier")
                    .email("teacher@test.com")
                    .role(Role.TEACHER)
                    .build();

    when(gradeRepository.findById(gradeId)).thenReturn(Optional.of(gradeEntity));
    when(userRepository.findById(userId)).thenReturn(Optional.of(modifier));

    when(gradeRepository.save(any(JGrade.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

    when(gradeHistoryRepository.save(any(JGradeHistory.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

    Grade result =
            gradeService.update(
                    gradeId,
                    updatedGrade,
                    "Correction of the examination grade",
                    userId);

    assertThat(result).isNotNull();
    assertThat(result.getValue()).isEqualTo(17.0);

    verify(gradeRepository).findById(gradeId);
    verify(userRepository).findById(userId);
    verify(gradeHistoryValidator).accept(any());
    verify(gradeHistoryRepository).save(any(JGradeHistory.class));
    verify(gradeRepository).save(any(JGrade.class));
  }

  @Test
  void should_update_grade_without_history_when_value_does_not_change() {
    Grade updatedGrade = GradeMapper.toModel(gradeEntity);

    JUser modifier =
            JUser.builder()
                    .id(userId)
                    .firstName("Teacher")
                    .lastName("Modifier")
                    .email("teacher@test.com")
                    .role(Role.TEACHER)
                    .build();

    when(gradeRepository.findById(gradeId)).thenReturn(Optional.of(gradeEntity));
    when(userRepository.findById(userId)).thenReturn(Optional.of(modifier));

    when(gradeRepository.save(any(JGrade.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

    Grade result =
            gradeService.update(
                    gradeId,
                    updatedGrade,
                    "Correction",
                    userId);

    assertThat(result).isNotNull();
    assertThat(result.getValue()).isEqualTo(15.0);

    verify(userRepository).findById(userId);
    verify(gradeRepository).save(any(JGrade.class));
    verify(gradeHistoryRepository, never()).save(any());
    verify(gradeHistoryValidator, never()).accept(any());
  }

  @Test
  void should_throw_when_update_id_is_null() {
    Grade grade = GradeMapper.toModel(gradeEntity);

    assertThatThrownBy(
            () -> gradeService.update(null, grade, "Correction", userId))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("grade id is required");
  }

  @Test
  void should_throw_when_update_reason_is_null() {
    Grade grade = GradeMapper.toModel(gradeEntity);

    assertThatThrownBy(
            () -> gradeService.update(gradeId, grade, null, userId))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("reason is required");
  }

  @Test
  void should_throw_when_update_reason_is_blank() {
    Grade grade = GradeMapper.toModel(gradeEntity);

    assertThatThrownBy(
            () -> gradeService.update(gradeId, grade, "   ", userId))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("reason is required");
  }

  @Test
  void should_throw_when_modifier_is_null() {
    Grade grade = GradeMapper.toModel(gradeEntity);

    assertThatThrownBy(
            () -> gradeService.update(gradeId, grade, "Correction", null))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("modifier is required");
  }

  @Test
  void should_throw_when_updated_grade_is_null() {
    assertThatThrownBy(
            () -> gradeService.update(gradeId, null, "Correction", userId))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("grade cannot be null");

    verifyNoInteractions(gradeRepository);
  }

  @Test
  void should_throw_when_grade_to_update_does_not_exist() {
    Grade grade = GradeMapper.toModel(gradeEntity);

    when(gradeRepository.findById(gradeId)).thenReturn(Optional.empty());

    assertThatThrownBy(
            () -> gradeService.update(gradeId, grade, "Correction", userId))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("grade not found");
  }

  @Test
  void should_throw_when_modifier_does_not_exist() {
    Grade grade = GradeMapper.toModel(gradeEntity);

    when(gradeRepository.findById(gradeId)).thenReturn(Optional.of(gradeEntity));
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThatThrownBy(
            () -> gradeService.update(gradeId, grade, "Correction", userId))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("modifier not found");

    verify(userRepository).findById(userId);
    verify(gradeRepository, never()).save(any());
  }

  // ---------------------------------------------------------------------------
  // DELETE
  // ---------------------------------------------------------------------------

  @Test
  void should_delete_grade() {
    when(gradeRepository.findById(gradeId)).thenReturn(Optional.of(gradeEntity));

    gradeService.delete(gradeId);

    verify(gradeRepository).findById(gradeId);
    verify(courseAccessService).assertCanAccessCourse(courseId);
    verify(gradeRepository).deleteById(gradeId);
  }

  @Test
  void should_throw_when_deleting_null_id() {
    assertThatThrownBy(() -> gradeService.delete(null))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("grade id is required");
  }

  @Test
  void should_throw_when_deleting_non_existing_grade() {
    when(gradeRepository.findById(gradeId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> gradeService.delete(gradeId))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("grade not found");

    verify(gradeRepository).findById(gradeId);
    verify(gradeRepository, never()).deleteById(any());
  }

  // ---------------------------------------------------------------------------
  // FIND BY EXAM
  // ---------------------------------------------------------------------------

  @Test
  void should_find_grades_by_exam() {
    when(examRepository.existsById(examId)).thenReturn(true);
    when(gradeRepository.findByExam_Id(examId))
            .thenReturn(List.of(gradeEntity));

    List<Grade> result = gradeService.findByExam(examId);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getId()).isEqualTo(gradeId);
  }

  @Test
  void should_throw_when_exam_does_not_exist() {
    when(examRepository.existsById(examId)).thenReturn(false);

    assertThatThrownBy(() -> gradeService.findByExam(examId))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("exam not found");
  }

  // ---------------------------------------------------------------------------
  // FIND BY STUDENT
  // ---------------------------------------------------------------------------

  @Test
  void should_find_grades_by_student() {
    when(studentRepository.existsById(studentId)).thenReturn(true);
    when(gradeRepository.findByStudent_Id(studentId))
            .thenReturn(List.of(gradeEntity));

    List<Grade> result = gradeService.findByStudent(studentId);

    assertThat(result).hasSize(1);
  }

  @Test
  void should_throw_when_student_does_not_exist() {
    when(studentRepository.existsById(studentId)).thenReturn(false);

    assertThatThrownBy(() -> gradeService.findByStudent(studentId))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("student not found");
  }

  // ---------------------------------------------------------------------------
  // FIND BY COURSE
  // ---------------------------------------------------------------------------

  @Test
  void should_find_grades_by_course() {
    when(courseRepository.existsById(courseId)).thenReturn(true);
    when(gradeRepository.findByCourseId(courseId))
            .thenReturn(List.of(gradeEntity));

    List<Grade> result = gradeService.findByCourse(courseId);

    assertThat(result).hasSize(1);
  }

  @Test
  void should_throw_when_course_does_not_exist() {
    when(courseRepository.existsById(courseId)).thenReturn(false);

    assertThatThrownBy(() -> gradeService.findByCourse(courseId))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("course not found");
  }

  // ---------------------------------------------------------------------------
  // FIND BY STUDENT + COURSE
  // ---------------------------------------------------------------------------

  @Test
  void should_find_grades_by_student_and_course() {
    when(studentRepository.existsById(studentId)).thenReturn(true);
    when(courseRepository.existsById(courseId)).thenReturn(true);
    when(gradeRepository.findByStudentIdAndCourseId(studentId, courseId))
            .thenReturn(List.of(gradeEntity));

    List<Grade> result =
            gradeService.findByStudentAndCourse(studentId, courseId);

    assertThat(result).hasSize(1);
  }

  // ---------------------------------------------------------------------------
  // FIND BY GROUP
  // ---------------------------------------------------------------------------

  @Test
  void should_find_grades_by_group() {
    when(groupRepository.existsById(groupId)).thenReturn(true);
    when(gradeRepository.findByGroupId(groupId))
            .thenReturn(List.of(gradeEntity));

    List<Grade> result = gradeService.findByGroup(groupId);

    assertThat(result).hasSize(1);
  }

  @Test
  void should_find_grades_by_group_and_exam() {
    when(groupRepository.existsById(groupId)).thenReturn(true);
    when(examRepository.existsById(examId)).thenReturn(true);
    when(gradeRepository.findByGroupIdAndExamId(groupId, examId))
            .thenReturn(List.of(gradeEntity));

    List<Grade> result =
            gradeService.findByGroupAndExam(groupId, examId);

    assertThat(result).hasSize(1);
  }

  @Test
  void should_find_grades_by_group_and_course() {
    when(groupRepository.existsById(groupId)).thenReturn(true);
    when(courseRepository.existsById(courseId)).thenReturn(true);
    when(gradeRepository.findByGroupIdAndCourseId(groupId, courseId))
            .thenReturn(List.of(gradeEntity));

    List<Grade> result =
            gradeService.findByGroupAndCourse(groupId, courseId);

    assertThat(result).hasSize(1);
  }

  // ---------------------------------------------------------------------------
  // FIND BY SPECIALITY
  // ---------------------------------------------------------------------------

  @Test
  void should_find_grades_by_speciality() {
    when(specialityRepository.existsById(specialityId)).thenReturn(true);
    when(gradeRepository.findBySpecialityId(specialityId))
            .thenReturn(List.of(gradeEntity));

    List<Grade> result = gradeService.findBySpeciality(specialityId);

    assertThat(result).hasSize(1);
  }

  @Test
  void should_find_grades_by_speciality_and_exam() {
    when(specialityRepository.existsById(specialityId)).thenReturn(true);
    when(examRepository.existsById(examId)).thenReturn(true);
    when(gradeRepository.findBySpecialityIdAndExamId(
            specialityId, examId))
            .thenReturn(List.of(gradeEntity));

    List<Grade> result =
            gradeService.findBySpecialityAndExam(specialityId, examId);

    assertThat(result).hasSize(1);
  }

  @Test
  void should_find_grades_by_speciality_and_course() {
    when(specialityRepository.existsById(specialityId)).thenReturn(true);
    when(courseRepository.existsById(courseId)).thenReturn(true);
    when(gradeRepository.findBySpecialityIdAndCourseId(
            specialityId, courseId))
            .thenReturn(List.of(gradeEntity));

    List<Grade> result =
            gradeService.findBySpecialityAndCourse(specialityId, courseId);

    assertThat(result).hasSize(1);
  }

  // ---------------------------------------------------------------------------
  // FIND BY STUDENT + EXAM
  // ---------------------------------------------------------------------------

  @Test
  void should_find_grade_by_student_and_exam() {
    when(studentRepository.existsById(studentId)).thenReturn(true);
    when(examRepository.existsById(examId)).thenReturn(true);
    when(gradeRepository.findByStudent_IdAndExam_Id(
            studentId, examId))
            .thenReturn(Optional.of(gradeEntity));

    Grade result =
            gradeService.findByStudentAndExam(studentId, examId);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(gradeId);
  }

  @Test
  void should_throw_when_grade_not_found_for_student_and_exam() {
    when(studentRepository.existsById(studentId)).thenReturn(true);
    when(examRepository.existsById(examId)).thenReturn(true);
    when(gradeRepository.findByStudent_IdAndExam_Id(
            studentId, examId))
            .thenReturn(Optional.empty());

    assertThatThrownBy(
            () -> gradeService.findByStudentAndExam(studentId, examId))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining(
                    "grade not found for this student and exam");
  }

  // ---------------------------------------------------------------------------
  // FIND BY DATE
  // ---------------------------------------------------------------------------

  @Test
  void should_find_grades_by_date() {
    LocalDate date = LocalDate.of(2026, 8, 1);

    when(gradeRepository.findByDate(date))
            .thenReturn(List.of(gradeEntity));

    List<Grade> result = gradeService.findByDate(date);

    assertThat(result).hasSize(1);
  }

  @Test
  void should_throw_when_date_is_null() {
    assertThatThrownBy(() -> gradeService.findByDate(null))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("date is required");
  }

  // ---------------------------------------------------------------------------
  // FIND BY MIN VALUE
  // ---------------------------------------------------------------------------

  @Test
  void should_find_grades_by_min_value() {
    when(gradeRepository.findByValueGreaterThanEqual(10.0))
            .thenReturn(List.of(gradeEntity));

    List<Grade> result = gradeService.findByMinValue(10.0);

    assertThat(result).hasSize(1);
  }

  @Test
  void should_throw_when_min_value_is_null() {
    assertThatThrownBy(() -> gradeService.findByMinValue(null))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("minValue is required");
  }

  @Test
  void should_throw_when_min_value_is_negative() {
    assertThatThrownBy(() -> gradeService.findByMinValue(-1.0))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("minValue cannot be negative");
  }

  // ---------------------------------------------------------------------------
  // COURSE AVERAGE
  // ---------------------------------------------------------------------------

  @Test
  void should_compute_student_course_average() {
    JExam exam1 =
            JExam.builder()
                    .id(UUID.randomUUID())
                    .coeff(0.4)
                    .course(course)
                    .build();

    JExam exam2 =
            JExam.builder()
                    .id(UUID.randomUUID())
                    .coeff(0.6)
                    .course(course)
                    .build();

    JGrade grade1 =
            JGrade.builder()
                    .id(UUID.randomUUID())
                    .value(10.0)
                    .date(LocalDate.now())
                    .student(student)
                    .exam(exam1)
                    .build();

    JGrade grade2 =
            JGrade.builder()
                    .id(UUID.randomUUID())
                    .value(16.0)
                    .date(LocalDate.now())
                    .student(student)
                    .exam(exam2)
                    .build();

    when(studentRepository.existsById(studentId)).thenReturn(true);
    when(courseRepository.existsById(courseId)).thenReturn(true);
    when(gradeRepository.findByStudentIdAndCourseId(
            studentId, courseId))
            .thenReturn(List.of(grade1, grade2));

    Double average =
            gradeService.computeStudentCourseAverage(
                    studentId, courseId);

    assertThat(average).isEqualTo(13.6);
  }

  @Test
  void should_return_null_when_student_has_no_grades() {
    when(studentRepository.existsById(studentId)).thenReturn(true);
    when(courseRepository.existsById(courseId)).thenReturn(true);
    when(gradeRepository.findByStudentIdAndCourseId(
            studentId, courseId))
            .thenReturn(List.of());

    Double average =
            gradeService.computeStudentCourseAverage(
                    studentId, courseId);

    assertThat(average).isNull();
  }

  @Test
  void should_return_null_when_exam_coefficients_sum_to_zero() {
    JExam zeroCoeffExam =
            JExam.builder()
                    .id(UUID.randomUUID())
                    .coeff(0.0)
                    .course(course)
                    .build();

    JGrade zeroCoeffGrade =
            JGrade.builder()
                    .id(UUID.randomUUID())
                    .value(15.0)
                    .student(student)
                    .exam(zeroCoeffExam)
                    .build();

    when(studentRepository.existsById(studentId)).thenReturn(true);
    when(courseRepository.existsById(courseId)).thenReturn(true);
    when(gradeRepository.findByStudentIdAndCourseId(
            studentId, courseId))
            .thenReturn(List.of(zeroCoeffGrade));

    Double average =
            gradeService.computeStudentCourseAverage(
                    studentId, courseId);

    assertThat(average).isNull();
  }

  // ---------------------------------------------------------------------------
  // GROUP YEAR COMPLETENESS
  // ---------------------------------------------------------------------------

  @Test
  void should_reject_invalid_level() {
    assertThatThrownBy(
            () -> gradeService.isGroupYearComplete(groupId, 4))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("level must be 1, 2 or 3");
  }

  @Test
  void should_reject_null_level() {
    assertThatThrownBy(
            () -> gradeService.isGroupYearComplete(groupId, null))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("level must be 1, 2 or 3");
  }

  @Test
  void should_return_false_when_group_has_no_speciality() {
    JGroup group =
            JGroup.builder()
                    .id(groupId)
                    .name("Group K1")
                    .build();

    when(groupRepository.findById(groupId))
            .thenReturn(Optional.of(group));

    assertThatThrownBy(
            () -> gradeService.isGroupYearComplete(groupId, 1))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("group has no speciality");
  }

  @Test
  void should_return_false_when_no_courses_exist_for_level() {
    JSpeciality speciality =
            JSpeciality.builder()
                    .id(specialityId)
                    .build();

    JGroup group =
            JGroup.builder()
                    .id(groupId)
                    .name("Group K1")
                    .speciality(speciality)
                    .build();

    when(groupRepository.findById(groupId))
            .thenReturn(Optional.of(group));

    when(courseRepository.findBySpecialityIdAndLevel(
            specialityId, 1))
            .thenReturn(List.of());

    boolean result =
            gradeService.isGroupYearComplete(groupId, 1);

    assertThat(result).isFalse();
  }

  @Test
  void should_return_false_when_total_credits_are_not_60() {
    JSpeciality speciality =
            JSpeciality.builder()
                    .id(specialityId)
                    .build();

    JGroup group =
            JGroup.builder()
                    .id(groupId)
                    .name("Group K1")
                    .speciality(speciality)
                    .build();

    JCourse course1 =
            JCourse.builder()
                    .id(courseId)
                    .reference("CS101")
                    .title("Programming")
                    .credit(6)
                    .level(1)
                    .build();

    when(groupRepository.findById(groupId))
            .thenReturn(Optional.of(group));

    when(courseRepository.findBySpecialityIdAndLevel(
            specialityId, 1))
            .thenReturn(List.of(course1));

    boolean result =
            gradeService.isGroupYearComplete(groupId, 1);

    assertThat(result).isFalse();
  }

  // ---------------------------------------------------------------------------
  // GROUP YEAR STATUS
  // ---------------------------------------------------------------------------

  @Test
  void should_get_group_year_status() {
    JSpeciality speciality =
            JSpeciality.builder()
                    .id(specialityId)
                    .build();

    JGroup group =
            JGroup.builder()
                    .id(groupId)
                    .name("Group K1")
                    .speciality(speciality)
                    .build();

    when(groupRepository.findById(groupId))
            .thenReturn(Optional.of(group));

    when(courseRepository.findBySpecialityIdAndLevel(
            specialityId, 1))
            .thenReturn(List.of());

    Map<String, Object> result =
            gradeService.getGroupYearStatus(groupId, 1);

    assertThat(result.get("groupId")).isEqualTo(groupId);
    assertThat(result.get("groupName")).isEqualTo("Group K1");
    assertThat(result.get("specialityId")).isEqualTo(specialityId);
    assertThat(result.get("level")).isEqualTo(1);
    assertThat(result.get("totalCredits")).isEqualTo(0);
    assertThat(result.get("requiredCredits")).isEqualTo(60);
    assertThat(result.get("creditsComplete")).isEqualTo(false);
    assertThat(result.get("allCoeffsComplete")).isEqualTo(true);
    assertThat(result.get("yearComplete")).isEqualTo(false);
  }

  @Test
  void should_throw_when_group_does_not_exist() {
    when(groupRepository.findById(groupId))
            .thenReturn(Optional.empty());

    assertThatThrownBy(
            () -> gradeService.getGroupYearStatus(groupId, 1))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("group not found");
  }

  // ---------------------------------------------------------------------------
  // FIND ALL GRADES FOR GROUP YEAR
  // ---------------------------------------------------------------------------

  @Test
  void should_reject_find_all_grades_when_year_is_incomplete() {
    JSpeciality speciality =
            JSpeciality.builder()
                    .id(specialityId)
                    .build();

    JGroup group =
            JGroup.builder()
                    .id(groupId)
                    .name("Group K1")
                    .speciality(speciality)
                    .build();

    when(groupRepository.findById(groupId))
            .thenReturn(Optional.of(group));

    when(courseRepository.findBySpecialityIdAndLevel(
            specialityId, 1))
            .thenReturn(List.of());

    assertThatThrownBy(
            () -> gradeService.findAllGradesForGroupYear(groupId, 1))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("group year is not complete");
  }
}