package com.school.hei.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.school.hei.entity.JExam;
import com.school.hei.entity.JGroup;
import com.school.hei.entity.JGroupExam;
import com.school.hei.model.Course;
import com.school.hei.model.Exam;
import com.school.hei.model.Group;
import com.school.hei.repository.CourseRepository;
import com.school.hei.repository.ExamRepository;
import com.school.hei.repository.GroupExamRepository;
import com.school.hei.repository.GroupRepository;
import com.school.hei.repository.SpecialityRepository;
import com.school.hei.service.services.ExamService;
import com.school.hei.validator.ExamValidator;
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
class ExamServiceTest {

    @Mock
    private ExamRepository examRepository;

    @Mock
    private ExamValidator examValidator;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private SpecialityRepository specialityRepository;

    @Mock
    private GroupExamRepository groupExamRepository;

    @InjectMocks
    private ExamService examService;

    @Test
    void should_find_all_exams() {
        UUID examId1 = UUID.randomUUID();
        UUID examId2 = UUID.randomUUID();

        JExam exam1 = createExamEntity(examId1, "Mathematics Exam", 0.4);
        JExam exam2 = createExamEntity(examId2, "Programming Exam", 0.6);

        when(examRepository.findAll())
                .thenReturn(List.of(exam1, exam2));

        when(groupExamRepository.findByExam_Id(examId1))
                .thenReturn(List.of());

        when(groupExamRepository.findByExam_Id(examId2))
                .thenReturn(List.of());

        List<Exam> result = examService.findAll();

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Exam::getTitle)
                .containsExactly("Mathematics Exam", "Programming Exam");

        verify(examRepository).findAll();
        verify(groupExamRepository).findByExam_Id(examId1);
        verify(groupExamRepository).findByExam_Id(examId2);
    }

    @Test
    void should_find_exam_by_id() {
        UUID examId = UUID.randomUUID();

        JExam exam = createExamEntity(examId, "Programming Exam", 0.5);

        when(examRepository.findById(examId))
                .thenReturn(Optional.of(exam));

        when(groupExamRepository.findByExam_Id(examId))
                .thenReturn(List.of());

        Exam result = examService.findById(examId);

        assertThat(result.getId()).isEqualTo(examId);
        assertThat(result.getTitle()).isEqualTo("Programming Exam");
        assertThat(result.getCoeff()).isEqualTo(0.5);

        verify(examRepository).findById(examId);
        verify(groupExamRepository).findByExam_Id(examId);
    }

    @Test
    void should_throw_when_exam_is_not_found() {
        UUID examId = UUID.randomUUID();

        when(examRepository.findById(examId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResponseStatusException.class,
                () -> examService.findById(examId));

        verify(examRepository).findById(examId);
        verifyNoInteractions(groupExamRepository);
    }

    @Test
    void should_find_exams_by_course() {
        UUID courseId = UUID.randomUUID();

        JExam exam = createExamEntity(UUID.randomUUID(), "Programming Exam", 0.5);

        when(courseRepository.existsById(courseId))
                .thenReturn(true);

        when(examRepository.findByCourse_Id(courseId))
                .thenReturn(List.of(exam));

        List<Exam> result = examService.findByCourse(courseId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle())
                .isEqualTo("Programming Exam");

        verify(courseRepository).existsById(courseId);
        verify(examRepository).findByCourse_Id(courseId);
    }

    @Test
    void should_throw_when_course_is_not_found() {
        UUID courseId = UUID.randomUUID();

        when(courseRepository.existsById(courseId))
                .thenReturn(false);

        assertThrows(
                ResponseStatusException.class,
                () -> examService.findByCourse(courseId));

        verify(courseRepository).existsById(courseId);
        verify(examRepository, never()).findByCourse_Id(courseId);
    }

    @Test
    void should_find_exams_by_title() {
        JExam exam =
                createExamEntity(
                        UUID.randomUUID(),
                        "Mathematics Final Exam",
                        0.5);

        when(examRepository.findByTitleContainingIgnoreCase("math"))
                .thenReturn(List.of(exam));

        List<Exam> result = examService.findByTitle("math");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle())
                .isEqualTo("Mathematics Final Exam");

        verify(examRepository)
                .findByTitleContainingIgnoreCase("math");
    }

    @Test
    void should_throw_when_exam_title_is_blank() {
        assertThrows(
                ResponseStatusException.class,
                () -> examService.findByTitle(""));

        verifyNoInteractions(examRepository);
    }

    @Test
    void should_throw_when_exam_title_is_null() {
        assertThrows(
                ResponseStatusException.class,
                () -> examService.findByTitle(null));

        verifyNoInteractions(examRepository);
    }

    @Test
    void should_find_exams_by_date() {
        LocalDate date = LocalDate.of(2026, 8, 20);

        JExam exam =
                JExam.builder()
                        .id(UUID.randomUUID())
                        .date(date)
                        .coeff(0.5)
                        .title("Programming Exam")
                        .build();

        when(examRepository.findByDate(date))
                .thenReturn(List.of(exam));

        List<Exam> result = examService.findByDate(date);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDate()).isEqualTo(date);

        verify(examRepository).findByDate(date);
    }

    @Test
    void should_throw_when_exam_date_is_null() {
        assertThrows(
                ResponseStatusException.class,
                () -> examService.findByDate(null));

        verifyNoInteractions(examRepository);
    }

    @Test
    void should_find_exams_by_coefficient() {
        JExam exam =
                createExamEntity(
                        UUID.randomUUID(),
                        "Programming Exam",
                        0.5);

        when(examRepository.findByCoeff(0.5))
                .thenReturn(List.of(exam));

        List<Exam> result = examService.findByCoeff(0.5);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCoeff()).isEqualTo(0.5);

        verify(examRepository).findByCoeff(0.5);
    }

    @Test
    void should_throw_when_coefficient_is_null() {
        assertThrows(
                ResponseStatusException.class,
                () -> examService.findByCoeff(null));

        verifyNoInteractions(examRepository);
    }

    @Test
    void should_throw_when_coefficient_is_zero() {
        assertThrows(
                ResponseStatusException.class,
                () -> examService.findByCoeff(0.0));

        verifyNoInteractions(examRepository);
    }

    @Test
    void should_throw_when_coefficient_is_negative() {
        assertThrows(
                ResponseStatusException.class,
                () -> examService.findByCoeff(-0.5));

        verifyNoInteractions(examRepository);
    }

    @Test
    void should_find_exams_by_group() {
        UUID groupId = UUID.randomUUID();

        JExam exam =
                createExamEntity(
                        UUID.randomUUID(),
                        "Programming Exam",
                        0.5);

        when(groupRepository.existsById(groupId))
                .thenReturn(true);

        when(examRepository.findByGroupId(groupId))
                .thenReturn(List.of(exam));

        List<Exam> result = examService.findByGroup(groupId);

        assertThat(result).hasSize(1);

        verify(groupRepository).existsById(groupId);
        verify(examRepository).findByGroupId(groupId);
    }

    @Test
    void should_throw_when_group_is_not_found_for_exam_search() {
        UUID groupId = UUID.randomUUID();

        when(groupRepository.existsById(groupId))
                .thenReturn(false);

        assertThrows(
                ResponseStatusException.class,
                () -> examService.findByGroup(groupId));

        verify(groupRepository).existsById(groupId);
        verify(examRepository, never()).findByGroupId(groupId);
    }

    @Test
    void should_find_exams_by_speciality() {
        UUID specialityId = UUID.randomUUID();

        JExam exam =
                createExamEntity(
                        UUID.randomUUID(),
                        "Programming Exam",
                        0.5);

        when(specialityRepository.existsById(specialityId))
                .thenReturn(true);

        when(examRepository.findBySpecialityId(specialityId))
                .thenReturn(List.of(exam));

        List<Exam> result = examService.findBySpeciality(specialityId);

        assertThat(result).hasSize(1);

        verify(specialityRepository).existsById(specialityId);
        verify(examRepository).findBySpecialityId(specialityId);
    }

    @Test
    void should_throw_when_speciality_is_not_found_for_exam_search() {
        UUID specialityId = UUID.randomUUID();

        when(specialityRepository.existsById(specialityId))
                .thenReturn(false);

        assertThrows(
                ResponseStatusException.class,
                () -> examService.findBySpeciality(specialityId));

        verify(specialityRepository).existsById(specialityId);
        verify(examRepository, never())
                .findBySpecialityId(specialityId);
    }

    @Test
    void should_return_remaining_coefficient() {
        UUID courseId = UUID.randomUUID();

        JExam exam1 =
                createExamEntity(
                        UUID.randomUUID(),
                        "Exam 1",
                        0.3);

        JExam exam2 =
                createExamEntity(
                        UUID.randomUUID(),
                        "Exam 2",
                        0.2);

        when(courseRepository.existsById(courseId))
                .thenReturn(true);

        when(examRepository.findByCourse_Id(courseId))
                .thenReturn(List.of(exam1, exam2));

        Double result = examService.remainingCoeff(courseId);

        assertThat(result).isEqualTo(0.5);

        verify(courseRepository).existsById(courseId);
        verify(examRepository).findByCourse_Id(courseId);
    }

    @Test
    void should_throw_when_calculating_remaining_coefficient_for_unknown_course() {
        UUID courseId = UUID.randomUUID();

        when(courseRepository.existsById(courseId))
                .thenReturn(false);

        assertThrows(
                ResponseStatusException.class,
                () -> examService.remainingCoeff(courseId));

        verify(courseRepository).existsById(courseId);
        verify(examRepository, never()).findByCourse_Id(courseId);
    }

    @Test
    void should_save_exam_with_groups() {
        UUID examId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();

        Exam exam = createExamModel(courseId, groupId, 0.4);

        JExam savedExam =
                createExamEntity(
                        examId,
                        "Programming Exam",
                        0.4);

        JGroup group = createGroupEntity(groupId);

        when(groupRepository.existsById(groupId))
                .thenReturn(true);

        when(examRepository.findByCourse_Id(courseId))
                .thenReturn(List.of());

        when(examRepository.save(any(JExam.class)))
                .thenReturn(savedExam);

        when(groupRepository.getReferenceById(groupId))
                .thenReturn(group);

        when(groupExamRepository.findByExam_Id(examId))
                .thenReturn(List.of());

        Exam result = examService.save(exam);

        assertThat(result.getId()).isEqualTo(examId);
        assertThat(result.getTitle()).isEqualTo("Programming Exam");
        assertThat(result.getGroups()).isEmpty();

        verify(examValidator).accept(exam);
        verify(groupRepository).existsById(groupId);
        verify(examRepository).findByCourse_Id(courseId);
        verify(examRepository).save(any(JExam.class));
        verify(groupRepository).getReferenceById(groupId);
        verify(groupExamRepository).save(any(JGroupExam.class));
        verify(groupExamRepository).findByExam_Id(examId);
    }

    @Test
    void should_throw_when_saving_exam_without_groups() {
        UUID courseId = UUID.randomUUID();

        Exam exam =
                Exam.builder()
                        .course(createCourse(courseId))
                        .title("Programming Exam")
                        .coeff(0.5)
                        .date(LocalDate.now())
                        .groups(List.of())
                        .build();

        assertThrows(
                ResponseStatusException.class,
                () -> examService.save(exam));

        verify(examValidator).accept(exam);
        verifyNoInteractions(groupRepository);
        verify(examRepository, never()).save(any(JExam.class));
    }

    @Test
    void should_throw_when_group_id_is_missing() {
        UUID courseId = UUID.randomUUID();

        Group group =
                Group.builder()
                        .id(null)
                        .name("Group A")
                        .build();

        Exam exam =
                Exam.builder()
                        .course(createCourse(courseId))
                        .title("Programming Exam")
                        .coeff(0.5)
                        .date(LocalDate.now())
                        .groups(List.of(group))
                        .build();

        assertThrows(
                ResponseStatusException.class,
                () -> examService.save(exam));

        verify(examValidator).accept(exam);
        verifyNoInteractions(examRepository);
    }

    @Test
    void should_throw_when_group_does_not_exist() {
        UUID courseId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();

        Exam exam = createExamModel(courseId, groupId, 0.5);

        when(groupRepository.existsById(groupId))
                .thenReturn(false);

        assertThrows(
                ResponseStatusException.class,
                () -> examService.save(exam));

        verify(groupRepository).existsById(groupId);
        verify(examRepository, never()).save(any(JExam.class));
    }

    @Test
    void should_throw_when_exam_coefficients_exceed_one() {
        UUID courseId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();

        Exam exam = createExamModel(courseId, groupId, 0.6);

        JExam existingExam =
                createExamEntity(
                        UUID.randomUUID(),
                        "Existing Exam",
                        0.5);

        when(groupRepository.existsById(groupId))
                .thenReturn(true);

        when(examRepository.findByCourse_Id(courseId))
                .thenReturn(List.of(existingExam));

        assertThrows(
                ResponseStatusException.class,
                () -> examService.save(exam));

        verify(examRepository).findByCourse_Id(courseId);
        verify(examRepository, never()).save(any(JExam.class));
    }

    @Test
    void should_update_exam() {
        UUID examId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();

        Exam exam = createExamModel(courseId, groupId, 0.5);

        JExam existingExam =
                createExamEntity(
                        examId,
                        "Old Exam",
                        0.4);

        JExam updatedExam =
                createExamEntity(
                        examId,
                        "Updated Exam",
                        0.5);

        JGroup group = createGroupEntity(groupId);

        when(examRepository.findById(examId))
                .thenReturn(Optional.of(existingExam));

        when(groupExamRepository.findByExam_Id(examId))
                .thenReturn(List.of());

        when(groupRepository.existsById(groupId))
                .thenReturn(true);

        when(examRepository.findByCourse_Id(courseId))
                .thenReturn(List.of(existingExam));

        when(examRepository.save(any(JExam.class)))
                .thenReturn(updatedExam);

        when(groupRepository.getReferenceById(groupId))
                .thenReturn(group);

        Exam result = examService.update(examId, exam);

        assertThat(result.getId()).isEqualTo(examId);
        assertThat(result.getTitle()).isEqualTo("Updated Exam");
        assertThat(exam.getId()).isEqualTo(examId);

        verify(examRepository).findById(examId);
        verify(examValidator).accept(exam);
        verify(groupRepository).existsById(groupId);
        verify(examRepository).save(any(JExam.class));
        verify(groupExamRepository).deleteByExam_Id(examId);
        verify(groupExamRepository).save(any(JGroupExam.class));
    }

    @Test
    void should_throw_when_updating_unknown_exam() {
        UUID examId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();

        Exam exam = createExamModel(courseId, groupId, 0.5);

        when(examRepository.findById(examId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResponseStatusException.class,
                () -> examService.update(examId, exam));

        verify(examRepository).findById(examId);
        verify(examRepository, never()).save(any(JExam.class));
        verify(examValidator, never()).accept(any(Exam.class));
    }

    @Test
    void should_delete_exam() {
        UUID examId = UUID.randomUUID();

        when(examRepository.existsById(examId))
                .thenReturn(true);

        examService.delete(examId);

        verify(examRepository).existsById(examId);
        verify(groupExamRepository).deleteByExam_Id(examId);
        verify(examRepository).deleteById(examId);
    }

    @Test
    void should_throw_when_deleting_unknown_exam() {
        UUID examId = UUID.randomUUID();

        when(examRepository.existsById(examId))
                .thenReturn(false);

        assertThrows(
                ResponseStatusException.class,
                () -> examService.delete(examId));

        verify(examRepository).existsById(examId);
        verify(groupExamRepository, never()).deleteByExam_Id(examId);
        verify(examRepository, never()).deleteById(examId);
    }

    private JExam createExamEntity(
            UUID id,
            String title,
            double coeff) {

        return JExam.builder()
                .id(id)
                .date(LocalDate.of(2026, 8, 20))
                .coeff(coeff)
                .title(title)
                .build();
    }

    private Exam createExamModel(
            UUID courseId,
            UUID groupId,
            double coeff) {

        return Exam.builder()
                .course(createCourse(courseId))
                .title("Programming Exam")
                .coeff(coeff)
                .date(LocalDate.of(2026, 8, 20))
                .groups(
                        List.of(
                                Group.builder()
                                        .id(groupId)
                                        .name("Group A")
                                        .build()))
                .build();
    }

    private Course createCourse(UUID courseId) {
        return Course.builder()
                .id(courseId)
                .reference("COURSE-001")
                .title("Programming")
                .credit(4)
                .level(2)
                .build();
    }

    private JGroup createGroupEntity(UUID groupId) {
        return JGroup.builder()
                .id(groupId)
                .name("Group A")
                .build();
    }
}