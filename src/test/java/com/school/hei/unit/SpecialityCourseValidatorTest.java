package com.school.hei.unit;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.school.hei.entity.JSpecialityCourse;
import com.school.hei.model.Course;
import com.school.hei.model.Speciality;
import com.school.hei.model.SpecialityCourse;
import com.school.hei.repository.CourseRepository;
import com.school.hei.repository.SpecialityCourseRepository;
import com.school.hei.repository.SpecialityRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import com.school.hei.validator.SpecialityCourseValidator;

@ExtendWith(MockitoExtension.class)
class SpecialityCourseValidatorTest {

    @Mock private SpecialityRepository specialityRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private SpecialityCourseRepository specialityCourseRepository;

    private SpecialityCourseValidator validator;

    private UUID specialityId;
    private UUID courseId;

    @BeforeEach
    void setUp() {
        validator =
                new SpecialityCourseValidator(specialityRepository, courseRepository, specialityCourseRepository);
        specialityId = UUID.randomUUID();
        courseId = UUID.randomUUID();
    }

    private SpecialityCourse validSpecialityCourse() {
        return SpecialityCourse.builder()
                .speciality(Speciality.builder().id(specialityId).build())
                .course(Course.builder().id(courseId).build())
                .build();
    }

    @Test
    void should_accept_valid_association() {
        SpecialityCourse sc = validSpecialityCourse();
        when(specialityRepository.existsById(specialityId)).thenReturn(true);
        when(courseRepository.existsById(courseId)).thenReturn(true);
        when(specialityCourseRepository.findBySpeciality_IdAndCourse_Id(specialityId, courseId))
                .thenReturn(Optional.empty());

        assertThatCode(() -> validator.accept(sc)).doesNotThrowAnyException();
    }

    @Test
    void should_reject_null_association() {
        assertThatThrownBy(() -> validator.accept(null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("cannot be null");
    }

    @Test
    void should_reject_null_speciality() {
        SpecialityCourse sc = validSpecialityCourse();
        sc.setSpeciality(null);

        assertThatThrownBy(() -> validator.accept(sc))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("the specialty  cannot be null");
    }

    @Test
    void should_reject_null_course() {
        SpecialityCourse sc = validSpecialityCourse();
        sc.setCourse(null);

        assertThatThrownBy(() -> validator.accept(sc))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("the course cannot be null");
    }

    @Test
    void should_reject_when_speciality_not_found() {
        SpecialityCourse sc = validSpecialityCourse();
        when(specialityRepository.existsById(specialityId)).thenReturn(false);

        assertThatThrownBy(() -> validator.accept(sc))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("speciality not found");
    }

    @Test
    void should_reject_when_course_not_found() {
        SpecialityCourse sc = validSpecialityCourse();
        when(specialityRepository.existsById(specialityId)).thenReturn(true);
        when(courseRepository.existsById(courseId)).thenReturn(false);

        assertThatThrownBy(() -> validator.accept(sc))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("course not found");
    }

    @Test
    void should_reject_when_association_already_exists() {
        SpecialityCourse sc = validSpecialityCourse();
        JSpecialityCourse existing = new JSpecialityCourse();
        existing.setId(UUID.randomUUID());
        when(specialityRepository.existsById(specialityId)).thenReturn(true);
        when(courseRepository.existsById(courseId)).thenReturn(true);
        when(specialityCourseRepository.findBySpeciality_IdAndCourse_Id(specialityId, courseId))
                .thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> validator.accept(sc))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("this course has already been saved for this speciality");
    }
}
