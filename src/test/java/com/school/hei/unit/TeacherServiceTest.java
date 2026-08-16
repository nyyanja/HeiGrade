package com.school.hei.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.school.hei.entity.JTeacher;
import com.school.hei.model.Teacher;
import com.school.hei.repository.CourseRepository;
import com.school.hei.repository.TeacherRepository;
import com.school.hei.service.services.TeacherService;
import com.school.hei.validator.TeacherValidator;
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
class TeacherServiceTest {

    @Mock private TeacherRepository teacherRepository;

    @Mock private TeacherValidator teacherValidator;

    @Mock private CourseRepository courseRepository;

    @InjectMocks private TeacherService teacherService;

    private UUID teacherId;
    private UUID courseId;

    private JTeacher teacherEntity;
    private Teacher teacher;

    @BeforeEach
    void setUp() {
        teacherId = UUID.randomUUID();
        courseId = UUID.randomUUID();

        teacherEntity =
                JTeacher.builder()
                        .id(teacherId)
                        .firstName("John")
                        .lastName("Doe")
                        .email("john.doe@test.com")
                        .speciality("Computer Science")
                        .build();

        teacher =
                Teacher.builder()
                        .id(teacherId)
                        .firstName("John")
                        .lastName("Doe")
                        .email("john.doe@test.com")
                        .speciality("Computer Science")
                        .build();
    }

    @Test
    void should_find_all_teachers() {
        when(teacherRepository.findAll()).thenReturn(List.of(teacherEntity));

        List<Teacher> result = teacherService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(teacherId);
        assertThat(result.get(0).getFirstName()).isEqualTo("John");
        assertThat(result.get(0).getLastName()).isEqualTo("Doe");
        assertThat(result.get(0).getSpeciality()).isEqualTo("Computer Science");

        verify(teacherRepository).findAll();
    }

    @Test
    void should_find_teacher_by_id() {
        when(teacherRepository.findById(teacherId)).thenReturn(Optional.of(teacherEntity));

        Teacher result = teacherService.findById(teacherId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(teacherId);
        assertThat(result.getSpeciality()).isEqualTo("Computer Science");

        verify(teacherRepository).findById(teacherId);
    }

    @Test
    void should_throw_not_found_when_teacher_does_not_exist() {
        when(teacherRepository.findById(teacherId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teacherService.findById(teacherId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("teacher not found");

        verify(teacherRepository).findById(teacherId);
    }

    @Test
    void should_find_teachers_by_speciality() {
        when(teacherRepository.findBySpecialityContainingIgnoreCase("Computer"))
                .thenReturn(List.of(teacherEntity));

        List<Teacher> result = teacherService.findBySpeciality("Computer");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSpeciality()).isEqualTo("Computer Science");

        verify(teacherRepository).findBySpecialityContainingIgnoreCase("Computer");
    }

    @Test
    void should_reject_blank_speciality() {
        assertThatThrownBy(() -> teacherService.findBySpeciality(" "))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("speciality is required");

        verify(teacherRepository, never()).findBySpecialityContainingIgnoreCase(anyString());
    }

    @Test
    void should_reject_null_speciality() {
        assertThatThrownBy(() -> teacherService.findBySpeciality(null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("speciality is required");

        verify(teacherRepository, never()).findBySpecialityContainingIgnoreCase(anyString());
    }

    @Test
    void should_find_teachers_by_name() {
        when(teacherRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                        "John", "John"))
                .thenReturn(List.of(teacherEntity));

        List<Teacher> result = teacherService.findByName("John");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFirstName()).isEqualTo("John");

        verify(teacherRepository)
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("John", "John");
    }

    @Test
    void should_reject_blank_name() {
        assertThatThrownBy(() -> teacherService.findByName(" "))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("name is required");

        verify(
                teacherRepository,
                never())
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                        anyString(), anyString());
    }

    @Test
    void should_reject_null_name() {
        assertThatThrownBy(() -> teacherService.findByName(null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("name is required");

        verify(
                teacherRepository,
                never())
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                        anyString(), anyString());
    }

    @Test
    void should_find_teachers_by_course() {
        when(courseRepository.existsById(courseId)).thenReturn(true);
        when(teacherRepository.findByCourseId(courseId)).thenReturn(List.of(teacherEntity));

        List<Teacher> result = teacherService.findByCourse(courseId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(teacherId);

        verify(courseRepository).existsById(courseId);
        verify(teacherRepository).findByCourseId(courseId);
    }

    @Test
    void should_throw_not_found_when_course_does_not_exist() {
        when(courseRepository.existsById(courseId)).thenReturn(false);

        assertThatThrownBy(() -> teacherService.findByCourse(courseId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("course not found");

        verify(courseRepository).existsById(courseId);
        verify(teacherRepository, never()).findByCourseId(any());
    }

    @Test
    void should_save_teacher() {
        Teacher newTeacher =
                Teacher.builder()
                        .firstName("John")
                        .lastName("Doe")
                        .email("john.doe@test.com")
                        .speciality("Computer Science")
                        .build();

        when(teacherRepository.save(any(JTeacher.class))).thenReturn(teacherEntity);

        Teacher result = teacherService.save(newTeacher);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(teacherId);
        assertThat(result.getSpeciality()).isEqualTo("Computer Science");

        verify(teacherValidator).accept(newTeacher);
        verify(teacherRepository).save(any(JTeacher.class));
    }

    @Test
    void should_update_teacher() {
        Teacher updatedTeacher =
                Teacher.builder()
                        .firstName("Jane")
                        .lastName("Doe")
                        .email("jane.doe@test.com")
                        .speciality("Data Science")
                        .build();

        JTeacher updatedEntity =
                JTeacher.builder()
                        .id(teacherId)
                        .firstName("Jane")
                        .lastName("Doe")
                        .email("jane.doe@test.com")
                        .speciality("Data Science")
                        .build();

        when(teacherRepository.findById(teacherId)).thenReturn(Optional.of(teacherEntity));
        when(teacherRepository.save(any(JTeacher.class))).thenReturn(updatedEntity);

        Teacher result = teacherService.update(teacherId, updatedTeacher);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(teacherId);
        assertThat(result.getFirstName()).isEqualTo("Jane");
        assertThat(result.getSpeciality()).isEqualTo("Data Science");
        assertThat(updatedTeacher.getId()).isEqualTo(teacherId);

        verify(teacherRepository).findById(teacherId);
        verify(teacherValidator).accept(updatedTeacher);
        verify(teacherRepository).save(any(JTeacher.class));
    }

    @Test
    void should_throw_not_found_when_updating_non_existing_teacher() {
        when(teacherRepository.findById(teacherId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teacherService.update(teacherId, teacher))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("teacher not found");

        verify(teacherRepository).findById(teacherId);
        verify(teacherRepository, never()).save(any());
        verify(teacherValidator, never()).accept(any());
    }

    @Test
    void should_delete_teacher() {
        when(teacherRepository.existsById(teacherId)).thenReturn(true);

        teacherService.delete(teacherId);

        verify(teacherRepository).existsById(teacherId);
        verify(teacherRepository).deleteById(teacherId);
    }

    @Test
    void should_throw_not_found_when_deleting_non_existing_teacher() {
        when(teacherRepository.existsById(teacherId)).thenReturn(false);

        assertThatThrownBy(() -> teacherService.delete(teacherId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("teacher not found");

        verify(teacherRepository).existsById(teacherId);
        verify(teacherRepository, never()).deleteById(any());
    }
}