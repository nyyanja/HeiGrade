package com.school.hei.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.school.hei.entity.JCourse;
import com.school.hei.entity.JTeacher;
import com.school.hei.entity.JTeacherCourse;
import com.school.hei.model.Course;
import com.school.hei.model.Teacher;
import com.school.hei.model.TeacherCourse;
import com.school.hei.repository.TeacherCourseRepository;
import com.school.hei.service.services.TeacherCourseService;
import com.school.hei.validator.TeacherCourseValidator;
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
class TeacherCourseServiceTest {

    @Mock private TeacherCourseRepository teacherCourseRepository;

    @Mock private TeacherCourseValidator teacherCourseValidator;

    @InjectMocks private TeacherCourseService teacherCourseService;

    private UUID teacherCourseId;
    private UUID teacherId;
    private UUID courseId;

    private JTeacherCourse teacherCourseEntity;
    private TeacherCourse teacherCourse;

    @BeforeEach
    void setUp() {
        teacherCourseId = UUID.randomUUID();
        teacherId = UUID.randomUUID();
        courseId = UUID.randomUUID();

        JTeacher teacherEntity =
                JTeacher.builder()
                        .id(teacherId)
                        .firstName("John")
                        .lastName("Doe")
                        .email("john.doe@test.com")
                        .build();

        JCourse courseEntity =
                JCourse.builder()
                        .id(courseId)
                        .reference("CS101")
                        .title("Programming")
                        .credit(6)
                        .level(1)
                        .build();

        teacherCourseEntity =
                JTeacherCourse.builder()
                        .id(teacherCourseId)
                        .teacher(teacherEntity)
                        .course(courseEntity)
                        .build();

        Teacher teacher =
                Teacher.builder()
                        .id(teacherId)
                        .firstName("John")
                        .lastName("Doe")
                        .build();

        Course course =
                Course.builder()
                        .id(courseId)
                        .reference("CS101")
                        .title("Programming")
                        .credit(6)
                        .level(1)
                        .build();

        teacherCourse =
                TeacherCourse.builder()
                        .id(teacherCourseId)
                        .teacher(teacher)
                        .course(course)
                        .build();
    }

    @Test
    void should_find_all_teacher_courses() {
        when(teacherCourseRepository.findAll()).thenReturn(List.of(teacherCourseEntity));

        List<TeacherCourse> result = teacherCourseService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(teacherCourseId);
        assertThat(result.get(0).getTeacher().getId()).isEqualTo(teacherId);
        assertThat(result.get(0).getCourse().getId()).isEqualTo(courseId);

        verify(teacherCourseRepository).findAll();
    }

    @Test
    void should_find_teacher_course_by_id() {
        when(teacherCourseRepository.findById(teacherCourseId))
                .thenReturn(Optional.of(teacherCourseEntity));

        TeacherCourse result = teacherCourseService.findById(teacherCourseId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(teacherCourseId);

        verify(teacherCourseRepository).findById(teacherCourseId);
    }

    @Test
    void should_throw_not_found_when_teacher_course_does_not_exist() {
        when(teacherCourseRepository.findById(teacherCourseId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teacherCourseService.findById(teacherCourseId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("teacher course not found");

        verify(teacherCourseRepository).findById(teacherCourseId);
    }

    @Test
    void should_save_teacher_course() {
        TeacherCourse newTeacherCourse =
                TeacherCourse.builder()
                        .teacher(teacherCourse.getTeacher())
                        .course(teacherCourse.getCourse())
                        .build();

        when(teacherCourseRepository.save(any(JTeacherCourse.class)))
                .thenReturn(teacherCourseEntity);

        TeacherCourse result = teacherCourseService.save(newTeacherCourse);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(teacherCourseId);
        assertThat(result.getTeacher().getId()).isEqualTo(teacherId);
        assertThat(result.getCourse().getId()).isEqualTo(courseId);

        verify(teacherCourseValidator).accept(newTeacherCourse);
        verify(teacherCourseRepository).save(any(JTeacherCourse.class));
    }

    @Test
    void should_update_teacher_course() {
        TeacherCourse updatedTeacherCourse =
                TeacherCourse.builder()
                        .teacher(teacherCourse.getTeacher())
                        .course(teacherCourse.getCourse())
                        .build();

        when(teacherCourseRepository.findById(teacherCourseId))
                .thenReturn(Optional.of(teacherCourseEntity));

        when(teacherCourseRepository.save(any(JTeacherCourse.class)))
                .thenReturn(teacherCourseEntity);

        TeacherCourse result =
                teacherCourseService.update(teacherCourseId, updatedTeacherCourse);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(teacherCourseId);
        assertThat(updatedTeacherCourse.getId()).isEqualTo(teacherCourseId);

        verify(teacherCourseRepository).findById(teacherCourseId);
        verify(teacherCourseValidator).accept(updatedTeacherCourse);
        verify(teacherCourseRepository).save(any(JTeacherCourse.class));
    }

    @Test
    void should_throw_not_found_when_updating_non_existing_teacher_course() {
        when(teacherCourseRepository.findById(teacherCourseId)).thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> teacherCourseService.update(teacherCourseId, teacherCourse))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("teacher course not found");

        verify(teacherCourseRepository).findById(teacherCourseId);
        verify(teacherCourseRepository, never()).save(any());
        verify(teacherCourseValidator, never()).accept(any());
    }

    @Test
    void should_delete_teacher_course() {
        when(teacherCourseRepository.existsById(teacherCourseId)).thenReturn(true);

        teacherCourseService.delete(teacherCourseId);

        verify(teacherCourseRepository).existsById(teacherCourseId);
        verify(teacherCourseRepository).deleteById(teacherCourseId);
    }

    @Test
    void should_throw_not_found_when_deleting_non_existing_teacher_course() {
        when(teacherCourseRepository.existsById(teacherCourseId)).thenReturn(false);

        assertThatThrownBy(() -> teacherCourseService.delete(teacherCourseId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("teacher course not found");

        verify(teacherCourseRepository).existsById(teacherCourseId);
        verify(teacherCourseRepository, never()).deleteById(any());
    }
}