package com.school.hei.unit;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.school.hei.entity.JGrade;
import com.school.hei.entity.JGroup;
import com.school.hei.entity.JGroupExam;
import com.school.hei.entity.JStudent;
import com.school.hei.model.Exam;
import com.school.hei.model.Grade;
import com.school.hei.model.Student;
import com.school.hei.repository.ExamRepository;
import com.school.hei.repository.GradeRepository;
import com.school.hei.repository.GroupExamRepository;
import com.school.hei.repository.StudentRepository;
import com.school.hei.validator.GradeValidator;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class GradeValidatorTest {

  @Mock private StudentRepository studentRepository;
  @Mock private ExamRepository examRepository;
  @Mock private GradeRepository gradeRepository;
  @Mock private GroupExamRepository groupExamRepository;

  private GradeValidator validator;

  private UUID studentId;
  private UUID examId;
  private UUID groupId;

  @BeforeEach
  void setUp() {
    validator =
        new GradeValidator(studentRepository, examRepository, gradeRepository, groupExamRepository);
    studentId = UUID.randomUUID();
    examId = UUID.randomUUID();
    groupId = UUID.randomUUID();
  }

  private Grade validGrade() {
    return Grade.builder()
        .value(15.0)
        .student(Student.builder().id(studentId).build())
        .exam(Exam.builder().id(examId).build())
        .build();
  }

  private JStudent jStudentWithGroup() {
    JGroup group = new JGroup();
    group.setId(groupId);
    return JStudent.builder().id(studentId).group(group).build();
  }

  @Test
  void should_accept_valid_grade() {
    Grade grade = validGrade();
    when(studentRepository.findById(studentId)).thenReturn(Optional.of(jStudentWithGroup()));
    when(examRepository.existsById(examId)).thenReturn(true);
    when(groupExamRepository.findByGroup_IdAndExam_Id(groupId, examId))
        .thenReturn(Optional.of(new JGroupExam()));
    when(gradeRepository.findByStudent_IdAndExam_Id(studentId, examId))
        .thenReturn(Optional.empty());

    assertThatCode(() -> validator.accept(grade)).doesNotThrowAnyException();
  }

  @Test
  void should_reject_null_grade() {
    assertThatThrownBy(() -> validator.accept(null))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("grade cannot be null");
  }

  @Test
  void should_reject_null_value() {
    Grade grade = validGrade();
    grade.setValue(null);

    assertThatThrownBy(() -> validator.accept(grade))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("grade value is required");
  }

  @Test
  void should_reject_null_student() {
    Grade grade = validGrade();
    grade.setStudent(null);

    assertThatThrownBy(() -> validator.accept(grade))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("student is required");
  }

  @Test
  void should_reject_null_exam() {
    Grade grade = validGrade();
    grade.setExam(null);

    assertThatThrownBy(() -> validator.accept(grade))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("exam is required");
  }

  @Test
  void should_reject_when_student_not_found() {
    Grade grade = validGrade();
    when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> validator.accept(grade))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("student not found");
  }

  @Test
  void should_reject_when_exam_not_found() {
    Grade grade = validGrade();
    when(studentRepository.findById(studentId)).thenReturn(Optional.of(jStudentWithGroup()));
    when(examRepository.existsById(examId)).thenReturn(false);

    assertThatThrownBy(() -> validator.accept(grade))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("exam not found");
  }

  @Test
  void should_reject_when_student_has_no_group() {
    Grade grade = validGrade();
    JStudent student = JStudent.builder().id(studentId).group(null).build();
    when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
    when(examRepository.existsById(examId)).thenReturn(true);

    assertThatThrownBy(() -> validator.accept(grade))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("student has no group");
  }

  @Test
  void should_reject_when_group_not_authorized_for_exam() {
    Grade grade = validGrade();
    when(studentRepository.findById(studentId)).thenReturn(Optional.of(jStudentWithGroup()));
    when(examRepository.existsById(examId)).thenReturn(true);
    when(groupExamRepository.findByGroup_IdAndExam_Id(groupId, examId))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> validator.accept(grade))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("not authorized for this exam");
  }

  @Test
  void should_reject_when_grade_already_exists() {
    Grade grade = validGrade();
    when(studentRepository.findById(studentId)).thenReturn(Optional.of(jStudentWithGroup()));
    when(examRepository.existsById(examId)).thenReturn(true);
    when(groupExamRepository.findByGroup_IdAndExam_Id(groupId, examId))
        .thenReturn(Optional.of(new JGroupExam()));
    JGrade existing = new JGrade();
    existing.setId(UUID.randomUUID());
    when(gradeRepository.findByStudent_IdAndExam_Id(studentId, examId))
        .thenReturn(Optional.of(existing));

    assertThatThrownBy(() -> validator.accept(grade))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("grade already exists for this student and exam");
  }
}
