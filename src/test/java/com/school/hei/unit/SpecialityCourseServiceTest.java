package com.school.hei.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.school.hei.entity.JCourse;
import com.school.hei.entity.JSpeciality;
import com.school.hei.entity.JSpecialityCourse;
import com.school.hei.enums.GroupSpeciality;
import com.school.hei.model.Course;
import com.school.hei.model.Speciality;
import com.school.hei.model.SpecialityCourse;
import com.school.hei.repository.SpecialityCourseRepository;
import com.school.hei.service.services.SpecialityCourseService;
import com.school.hei.validator.SpecialityCourseValidator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class SpecialityCourseServiceTest {

  @Mock private SpecialityCourseRepository specialityCourseRepository;

  @Mock private SpecialityCourseValidator specialityCourseValidator;

  @InjectMocks private SpecialityCourseService specialityCourseService;

  private UUID specialityId;
  private UUID courseId;
  private UUID associationId;

  private JSpeciality specialityEntity;
  private JCourse courseEntity;
  private JSpecialityCourse specialityCourseEntity;

  private Speciality speciality;
  private Course course;
  private SpecialityCourse specialityCourse;

  @BeforeEach
  void setUp() {
    specialityId = UUID.randomUUID();
    courseId = UUID.randomUUID();
    associationId = UUID.randomUUID();

    specialityEntity = JSpeciality.builder().id(specialityId).name("EL").build();

    courseEntity =
        JCourse.builder()
            .id(courseId)
            .reference("PROG1")
            .title("Programming")
            .credit(6)
            .level(1)
            .build();

    specialityCourseEntity =
        JSpecialityCourse.builder()
            .id(associationId)
            .speciality(specialityEntity)
            .course(courseEntity)
            .build();

    speciality = Speciality.builder().id(specialityId).name(GroupSpeciality.EL).build();

    course =
        Course.builder()
            .id(courseId)
            .reference("PROG1")
            .title("Programming")
            .credit(6)
            .level(1)
            .build();

    specialityCourse =
        SpecialityCourse.builder().id(associationId).speciality(speciality).course(course).build();
  }

  @Test
  void should_find_all_speciality_courses() {
    when(specialityCourseRepository.findAll()).thenReturn(List.of(specialityCourseEntity));

    List<SpecialityCourse> result = specialityCourseService.findAll();

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getId()).isEqualTo(associationId);
    assertThat(result.get(0).getSpeciality().getId()).isEqualTo(specialityId);
    assertThat(result.get(0).getCourse().getId()).isEqualTo(courseId);

    verify(specialityCourseRepository).findAll();
  }

  @Test
  void should_find_speciality_course_by_id() {
    when(specialityCourseRepository.findById(associationId))
        .thenReturn(Optional.of(specialityCourseEntity));

    SpecialityCourse result = specialityCourseService.findById(associationId);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(associationId);
    assertThat(result.getSpeciality().getId()).isEqualTo(specialityId);
    assertThat(result.getCourse().getId()).isEqualTo(courseId);

    verify(specialityCourseRepository).findById(associationId);
  }

  @Test
  void should_throw_not_found_when_speciality_course_does_not_exist() {
    when(specialityCourseRepository.findById(associationId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> specialityCourseService.findById(associationId))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            exception ->
                assertThat(((ResponseStatusException) exception).getStatusCode())
                    .isEqualTo(HttpStatus.NOT_FOUND))
        .hasMessageContaining("speciality course not found with id");

    verify(specialityCourseRepository).findById(associationId);
  }

  @Test
  void should_save_speciality_course() {
    when(specialityCourseRepository.save(any(JSpecialityCourse.class)))
        .thenReturn(specialityCourseEntity);

    SpecialityCourse result = specialityCourseService.save(specialityCourse);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(associationId);
    assertThat(result.getSpeciality().getId()).isEqualTo(specialityId);
    assertThat(result.getCourse().getId()).isEqualTo(courseId);

    verify(specialityCourseValidator).accept(specialityCourse);
    verify(specialityCourseRepository).save(any(JSpecialityCourse.class));
  }

  @Test
  void should_update_speciality_course() {
    when(specialityCourseRepository.findById(associationId))
        .thenReturn(Optional.of(specialityCourseEntity));

    when(specialityCourseRepository.save(any(JSpecialityCourse.class)))
        .thenReturn(specialityCourseEntity);

    SpecialityCourse result = specialityCourseService.update(associationId, specialityCourse);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(associationId);
    assertThat(specialityCourse.getId()).isEqualTo(associationId);

    verify(specialityCourseRepository).findById(associationId);
    verify(specialityCourseValidator).accept(specialityCourse);
    verify(specialityCourseRepository).save(any(JSpecialityCourse.class));
  }

  @Test
  void should_not_update_when_speciality_course_does_not_exist() {
    when(specialityCourseRepository.findById(associationId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> specialityCourseService.update(associationId, specialityCourse))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            exception ->
                assertThat(((ResponseStatusException) exception).getStatusCode())
                    .isEqualTo(HttpStatus.NOT_FOUND));

    verify(specialityCourseRepository).findById(associationId);
    verify(specialityCourseValidator, never()).accept(any());
    verify(specialityCourseRepository, never()).save(any());
  }

  @Test
  void should_delete_speciality_course() {
    when(specialityCourseRepository.existsById(associationId)).thenReturn(true);

    specialityCourseService.delete(associationId);

    verify(specialityCourseRepository).existsById(associationId);
    verify(specialityCourseRepository).deleteById(associationId);
  }

  @Test
  void should_not_delete_when_speciality_course_does_not_exist() {
    when(specialityCourseRepository.existsById(associationId)).thenReturn(false);

    assertThatThrownBy(() -> specialityCourseService.delete(associationId))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            exception ->
                assertThat(((ResponseStatusException) exception).getStatusCode())
                    .isEqualTo(HttpStatus.NOT_FOUND))
        .hasMessageContaining("speciality course not found with id");

    verify(specialityCourseRepository).existsById(associationId);
    verify(specialityCourseRepository, never()).deleteById(any());
  }

  @Test
  void should_find_speciality_courses_by_course() {
    when(specialityCourseRepository.findByCourse_Id(courseId))
        .thenReturn(List.of(specialityCourseEntity));

    List<SpecialityCourse> result = specialityCourseService.findByCourse(courseId);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getCourse().getId()).isEqualTo(courseId);
    assertThat(result.get(0).getSpeciality().getId()).isEqualTo(specialityId);

    verify(specialityCourseRepository).findByCourse_Id(courseId);
  }

  @Test
  void should_return_empty_list_when_no_speciality_course_exists_for_course() {
    when(specialityCourseRepository.findByCourse_Id(courseId)).thenReturn(List.of());

    List<SpecialityCourse> result = specialityCourseService.findByCourse(courseId);

    assertThat(result).isEmpty();

    verify(specialityCourseRepository).findByCourse_Id(courseId);
  }

  @Test
  void should_find_speciality_courses_by_speciality() {
    when(specialityCourseRepository.findBySpeciality_Id(specialityId))
        .thenReturn(List.of(specialityCourseEntity));

    List<SpecialityCourse> result = specialityCourseService.findBySpeciality(specialityId);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getSpeciality().getId()).isEqualTo(specialityId);
    assertThat(result.get(0).getCourse().getId()).isEqualTo(courseId);

    verify(specialityCourseRepository).findBySpeciality_Id(specialityId);
  }

  @Test
  void should_return_empty_list_when_no_course_exists_for_speciality() {
    when(specialityCourseRepository.findBySpeciality_Id(specialityId)).thenReturn(List.of());

    List<SpecialityCourse> result = specialityCourseService.findBySpeciality(specialityId);

    assertThat(result).isEmpty();

    verify(specialityCourseRepository).findBySpeciality_Id(specialityId);
  }
}
