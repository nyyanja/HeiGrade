package com.school.hei.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.school.hei.entity.JCourse;
import com.school.hei.entity.JGroup;
import com.school.hei.entity.JSpeciality;
import com.school.hei.entity.JSpecialityCourse;
import com.school.hei.entity.JTeacher;
import com.school.hei.entity.JTeacherCourse;
import com.school.hei.model.Course;
import com.school.hei.model.Speciality;
import com.school.hei.model.Teacher;
import com.school.hei.repository.CourseRepository;
import com.school.hei.repository.GroupRepository;
import com.school.hei.repository.SpecialityCourseRepository;
import com.school.hei.repository.SpecialityRepository;
import com.school.hei.repository.TeacherCourseRepository;
import com.school.hei.repository.TeacherRepository;
import com.school.hei.security.CourseAccessService;
import com.school.hei.service.services.CourseService;
import com.school.hei.validator.CourseValidator;
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
class CourseServiceTest {

  @Mock private CourseRepository courseRepository;

  @Mock private CourseValidator courseValidator;

  @Mock private TeacherRepository teacherRepository;

  @Mock private SpecialityRepository specialityRepository;

  @Mock private TeacherCourseRepository teacherCourseRepository;

  @Mock private SpecialityCourseRepository specialityCourseRepository;

  @Mock private GroupRepository groupRepository;

  @InjectMocks private CourseService courseService;
  @Mock private CourseAccessService courseAccessService;

  @BeforeEach
  void setUp() {
    lenient().when(courseAccessService.isAdmin()).thenReturn(true);
  }

  @Test
  void should_find_all_courses() {
    UUID courseId = UUID.randomUUID();

    JCourse course =
        JCourse.builder()
            .id(courseId)
            .reference("PROG1")
            .title("Programming")
            .credit(6)
            .level(1)
            .build();

    when(courseRepository.findAll()).thenReturn(List.of(course));
    when(teacherCourseRepository.findByCourse_Id(courseId)).thenReturn(List.of());
    when(specialityCourseRepository.findByCourse_Id(courseId)).thenReturn(List.of());

    List<Course> result = courseService.findAll();

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getId()).isEqualTo(courseId);
    assertThat(result.get(0).getReference()).isEqualTo("PROG1");
    assertThat(result.get(0).getTitle()).isEqualTo("Programming");

    verify(courseRepository).findAll();
  }

  @Test
  void should_find_course_by_id() {
    UUID courseId = UUID.randomUUID();

    JCourse course =
        JCourse.builder()
            .id(courseId)
            .reference("PROG1")
            .title("Programming")
            .credit(6)
            .level(1)
            .build();

    when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
    when(teacherCourseRepository.findByCourse_Id(courseId)).thenReturn(List.of());
    when(specialityCourseRepository.findByCourse_Id(courseId)).thenReturn(List.of());

    Course result = courseService.findById(courseId);

    assertThat(result.getId()).isEqualTo(courseId);
    assertThat(result.getReference()).isEqualTo("PROG1");
    assertThat(result.getTitle()).isEqualTo("Programming");
  }

  @Test
  void should_throw_when_course_not_found() {
    UUID courseId = UUID.randomUUID();

    when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

    assertThrows(ResponseStatusException.class, () -> courseService.findById(courseId));
  }

  @Test
  void should_save_course() {
    UUID courseId = UUID.randomUUID();
    UUID teacherId = UUID.randomUUID();
    UUID specialityId = UUID.randomUUID();

    Teacher teacher = Teacher.builder().id(teacherId).build();

    Speciality speciality = Speciality.builder().id(specialityId).build();

    Course course =
        Course.builder()
            .reference("PROG1")
            .title("Programming")
            .credit(6)
            .level(1)
            .teachers(List.of(teacher))
            .specialities(List.of(speciality))
            .build();

    JCourse savedCourse =
        JCourse.builder()
            .id(courseId)
            .reference("PROG1")
            .title("Programming")
            .credit(6)
            .level(1)
            .build();

    JTeacher jTeacher = JTeacher.builder().id(teacherId).build();

    JSpeciality jSpeciality = JSpeciality.builder().id(specialityId).name("EL").build();

    when(teacherRepository.existsById(teacherId)).thenReturn(true);
    when(specialityRepository.existsById(specialityId)).thenReturn(true);
    when(courseRepository.findBySpecialityIdAndLevel(specialityId, 1)).thenReturn(List.of());

    when(courseRepository.save(any(JCourse.class))).thenReturn(savedCourse);

    when(teacherRepository.getReferenceById(teacherId)).thenReturn(jTeacher);
    when(specialityRepository.getReferenceById(specialityId)).thenReturn(jSpeciality);

    when(teacherCourseRepository.findByCourse_Id(courseId)).thenReturn(List.of());
    when(specialityCourseRepository.findByCourse_Id(courseId)).thenReturn(List.of());

    Course result = courseService.save(course);

    assertThat(result.getId()).isEqualTo(courseId);
    assertThat(result.getTitle()).isEqualTo("Programming");
    assertThat(course.getId()).isEqualTo(courseId);

    verify(courseValidator).accept(course);
    verify(courseRepository).save(any(JCourse.class));
    verify(teacherCourseRepository).save(any(JTeacherCourse.class));
    verify(specialityCourseRepository).save(any(JSpecialityCourse.class));
  }

  @Test
  void should_throw_when_no_teacher_is_provided() {
    Course course =
        Course.builder()
            .reference("PROG1")
            .title("Programming")
            .credit(6)
            .level(1)
            .teachers(List.of())
            .specialities(List.of(Speciality.builder().id(UUID.randomUUID()).build()))
            .build();

    assertThrows(ResponseStatusException.class, () -> courseService.save(course));

    verify(courseRepository, never()).save(any());
  }

  @Test
  void should_throw_when_teacher_not_found() {
    UUID teacherId = UUID.randomUUID();

    Course course =
        Course.builder()
            .reference("PROG1")
            .title("Programming")
            .credit(6)
            .level(1)
            .teachers(List.of(Teacher.builder().id(teacherId).build()))
            .specialities(List.of(Speciality.builder().id(UUID.randomUUID()).build()))
            .build();

    when(teacherRepository.existsById(teacherId)).thenReturn(false);

    assertThrows(ResponseStatusException.class, () -> courseService.save(course));

    verify(courseRepository, never()).save(any());
  }

  @Test
  void should_throw_when_no_speciality_is_provided() {
    UUID teacherId = UUID.randomUUID();

    Course course =
        Course.builder()
            .reference("PROG1")
            .title("Programming")
            .credit(6)
            .level(1)
            .teachers(List.of(Teacher.builder().id(teacherId).build()))
            .specialities(List.of())
            .build();

    when(teacherRepository.existsById(teacherId)).thenReturn(true);

    assertThrows(ResponseStatusException.class, () -> courseService.save(course));

    verify(courseRepository, never()).save(any());
  }

  @Test
  void should_throw_when_speciality_not_found() {
    UUID teacherId = UUID.randomUUID();
    UUID specialityId = UUID.randomUUID();

    Course course =
        Course.builder()
            .reference("PROG1")
            .title("Programming")
            .credit(6)
            .level(1)
            .teachers(List.of(Teacher.builder().id(teacherId).build()))
            .specialities(List.of(Speciality.builder().id(specialityId).build()))
            .build();

    when(teacherRepository.existsById(teacherId)).thenReturn(true);
    when(specialityRepository.existsById(specialityId)).thenReturn(false);

    assertThrows(ResponseStatusException.class, () -> courseService.save(course));

    verify(courseRepository, never()).save(any());
  }

  @Test
  void should_throw_when_credits_exceed_sixty_per_speciality_and_level() {
    UUID teacherId = UUID.randomUUID();
    UUID specialityId = UUID.randomUUID();

    Course course =
        Course.builder()
            .reference("PROG1")
            .title("Programming")
            .credit(6)
            .level(1)
            .teachers(List.of(Teacher.builder().id(teacherId).build()))
            .specialities(List.of(Speciality.builder().id(specialityId).build()))
            .build();

    JCourse existingCourse =
        JCourse.builder()
            .id(UUID.randomUUID())
            .reference("PROG2")
            .title("Existing Course")
            .credit(56)
            .level(1)
            .build();

    when(teacherRepository.existsById(teacherId)).thenReturn(true);
    when(specialityRepository.existsById(specialityId)).thenReturn(true);
    when(courseRepository.findBySpecialityIdAndLevel(specialityId, 1))
        .thenReturn(List.of(existingCourse));

    assertThrows(ResponseStatusException.class, () -> courseService.save(course));

    verify(courseRepository, never()).save(any());
  }

  @Test
  void should_allow_course_when_total_credits_are_exactly_sixty() {
    UUID teacherId = UUID.randomUUID();
    UUID specialityId = UUID.randomUUID();
    UUID courseId = UUID.randomUUID();

    Course course =
        Course.builder()
            .reference("PROG2")
            .title("Advanced Programming")
            .credit(6)
            .level(1)
            .teachers(List.of(Teacher.builder().id(teacherId).build()))
            .specialities(List.of(Speciality.builder().id(specialityId).build()))
            .build();

    JCourse existingCourse =
        JCourse.builder()
            .id(UUID.randomUUID())
            .reference("PROG1")
            .title("Existing Course")
            .credit(54)
            .level(1)
            .build();

    JCourse savedCourse =
        JCourse.builder()
            .id(courseId)
            .reference("PROG2")
            .title("Advanced Programming")
            .credit(6)
            .level(1)
            .build();

    when(teacherRepository.existsById(teacherId)).thenReturn(true);
    when(specialityRepository.existsById(specialityId)).thenReturn(true);
    when(courseRepository.findBySpecialityIdAndLevel(specialityId, 1))
        .thenReturn(List.of(existingCourse));
    when(courseRepository.save(any(JCourse.class))).thenReturn(savedCourse);

    when(teacherRepository.getReferenceById(teacherId))
        .thenReturn(JTeacher.builder().id(teacherId).build());

    when(specialityRepository.getReferenceById(specialityId))
        .thenReturn(JSpeciality.builder().id(specialityId).name("EL").build());

    when(teacherCourseRepository.findByCourse_Id(courseId)).thenReturn(List.of());
    when(specialityCourseRepository.findByCourse_Id(courseId)).thenReturn(List.of());

    Course result = courseService.save(course);

    assertThat(result.getId()).isEqualTo(courseId);

    verify(courseRepository).save(any(JCourse.class));
  }

  @Test
  void should_update_course() {
    UUID courseId = UUID.randomUUID();
    UUID teacherId = UUID.randomUUID();
    UUID specialityId = UUID.randomUUID();

    JCourse existing =
        JCourse.builder()
            .id(courseId)
            .reference("PROG1")
            .title("Old Programming")
            .credit(6)
            .level(1)
            .build();

    Course course =
        Course.builder()
            .reference("PROG1")
            .title("Updated Programming")
            .credit(6)
            .level(1)
            .teachers(List.of(Teacher.builder().id(teacherId).build()))
            .specialities(List.of(Speciality.builder().id(specialityId).build()))
            .build();

    JCourse updated =
        JCourse.builder()
            .id(courseId)
            .reference("PROG1")
            .title("Updated Programming")
            .credit(6)
            .level(1)
            .build();

    when(courseRepository.findById(courseId)).thenReturn(Optional.of(existing));
    when(teacherCourseRepository.findByCourse_Id(courseId)).thenReturn(List.of());
    when(specialityCourseRepository.findByCourse_Id(courseId)).thenReturn(List.of());

    when(teacherRepository.existsById(teacherId)).thenReturn(true);
    when(specialityRepository.existsById(specialityId)).thenReturn(true);
    when(courseRepository.findBySpecialityIdAndLevel(specialityId, 1)).thenReturn(List.of());

    when(courseRepository.save(any(JCourse.class))).thenReturn(updated);

    when(teacherRepository.getReferenceById(teacherId))
        .thenReturn(JTeacher.builder().id(teacherId).build());

    when(specialityRepository.getReferenceById(specialityId))
        .thenReturn(JSpeciality.builder().id(specialityId).name("EL").build());

    Course result = courseService.update(courseId, course);

    assertThat(result.getId()).isEqualTo(courseId);
    assertThat(result.getTitle()).isEqualTo("Updated Programming");
    assertThat(course.getId()).isEqualTo(courseId);

    verify(teacherCourseRepository).deleteByCourse_Id(courseId);
    verify(specialityCourseRepository).deleteByCourse_Id(courseId);
    verify(teacherCourseRepository).save(any(JTeacherCourse.class));
    verify(specialityCourseRepository).save(any(JSpecialityCourse.class));
  }

  @Test
  void should_delete_course() {
    UUID courseId = UUID.randomUUID();

    when(courseRepository.existsById(courseId)).thenReturn(true);

    courseService.delete(courseId);

    verify(teacherCourseRepository).deleteByCourse_Id(courseId);
    verify(specialityCourseRepository).deleteByCourse_Id(courseId);
    verify(courseRepository).deleteById(courseId);
  }

  @Test
  void should_throw_when_deleting_unknown_course() {
    UUID courseId = UUID.randomUUID();

    when(courseRepository.existsById(courseId)).thenReturn(false);

    assertThrows(ResponseStatusException.class, () -> courseService.delete(courseId));

    verify(courseRepository, never()).deleteById(courseId);
  }


  @Test
  void should_find_courses_by_teacher() {
    UUID teacherId = UUID.randomUUID();
    UUID courseId = UUID.randomUUID();

    JCourse course =
        JCourse.builder()
            .id(courseId)
            .reference("PROG1")
            .title("Programming")
            .credit(6)
            .level(1)
            .build();

    when(teacherRepository.existsById(teacherId)).thenReturn(true);
    when(courseRepository.findByTeacherId(teacherId)).thenReturn(List.of(course));

    List<Course> result = courseService.findByTeacher(teacherId);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getId()).isEqualTo(courseId);
  }

  @Test
  void should_throw_when_teacher_not_found_in_filter() {
    UUID teacherId = UUID.randomUUID();

    when(teacherRepository.existsById(teacherId)).thenReturn(false);

    assertThrows(ResponseStatusException.class, () -> courseService.findByTeacher(teacherId));
  }

  @Test
  void should_find_courses_by_speciality() {
    UUID specialityId = UUID.randomUUID();
    UUID courseId = UUID.randomUUID();

    JCourse course =
        JCourse.builder()
            .id(courseId)
            .reference("PROG1")
            .title("Programming")
            .credit(6)
            .level(1)
            .build();

    when(specialityRepository.existsById(specialityId)).thenReturn(true);
    when(courseRepository.findBySpecialityId(specialityId)).thenReturn(List.of(course));

    List<Course> result = courseService.findBySpeciality(specialityId);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getId()).isEqualTo(courseId);
  }

  @Test
  void should_find_courses_by_credit() {
    JCourse course =
        JCourse.builder()
            .id(UUID.randomUUID())
            .reference("PROG1")
            .title("Programming")
            .credit(6)
            .level(1)
            .build();

    when(courseRepository.findByCredit(6)).thenReturn(List.of(course));

    List<Course> result = courseService.findByCredit(6);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getCredit()).isEqualTo(6);
  }

  @Test
  void should_throw_when_credit_is_invalid() {
    assertThrows(ResponseStatusException.class, () -> courseService.findByCredit(0));

    assertThrows(ResponseStatusException.class, () -> courseService.findByCredit(null));
  }

  @Test
  void should_find_courses_by_group() {
    UUID groupId = UUID.randomUUID();
    UUID specialityId = UUID.randomUUID();

    JGroup group =
            JGroup.builder()
                    .id(groupId)
                    .name("Group 1")
                    .speciality(JSpeciality.builder().id(specialityId).name("EL").build())
                    .build();

    JCourse course =
            JCourse.builder()
                    .id(UUID.randomUUID())
                    .reference("PROG1")
                    .title("Programming")
                    .credit(6)
                    .level(1)
                    .build();

    when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
    when(courseRepository.findBySpecialityId(specialityId)).thenReturn(List.of(course));
    when(teacherCourseRepository.findByCourse_Id(course.getId())).thenReturn(List.of());
    when(specialityCourseRepository.findByCourse_Id(course.getId())).thenReturn(List.of());

    List<Course> result = courseService.findByGroup(groupId);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getId()).isEqualTo(course.getId());
  }

  @Test
  void should_throw_when_group_not_found() {
    UUID groupId = UUID.randomUUID();

    when(groupRepository.findById(groupId)).thenReturn(Optional.empty());

    assertThrows(ResponseStatusException.class, () -> courseService.findByGroup(groupId));
  }

  @Test
  void should_find_courses_by_title() {
    JCourse course =
        JCourse.builder()
            .id(UUID.randomUUID())
            .reference("PROG1")
            .title("Programming")
            .credit(6)
            .level(1)
            .build();

    when(courseRepository.findByTitleContainingIgnoreCase("Program")).thenReturn(List.of(course));

    List<Course> result = courseService.findByTitle("Program");

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getTitle()).isEqualTo("Programming");
  }

  @Test
  void should_throw_when_title_is_blank() {
    assertThrows(ResponseStatusException.class, () -> courseService.findByTitle(" "));
  }

  @Test
  void should_find_courses_by_level() {
    UUID courseId = UUID.randomUUID();

    JCourse course =
        JCourse.builder()
            .id(courseId)
            .reference("PROG1")
            .title("Programming")
            .credit(6)
            .level(1)
            .build();

    when(courseRepository.findByLevel(1)).thenReturn(List.of(course));
    when(teacherCourseRepository.findByCourse_Id(courseId)).thenReturn(List.of());
    when(specialityCourseRepository.findByCourse_Id(courseId)).thenReturn(List.of());

    List<Course> result = courseService.findByLevel(1);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getLevel()).isEqualTo(1);
  }

  @Test
  void should_throw_when_level_is_invalid() {
    assertThrows(ResponseStatusException.class, () -> courseService.findByLevel(0));

    assertThrows(ResponseStatusException.class, () -> courseService.findByLevel(4));

    assertThrows(ResponseStatusException.class, () -> courseService.findByLevel(null));
  }
}
