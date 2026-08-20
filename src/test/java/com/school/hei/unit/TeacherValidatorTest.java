package com.school.hei.unit;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

import com.school.hei.enums.Role;
import com.school.hei.model.Teacher;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import com.school.hei.validator.TeacherValidator;
import com.school.hei.validator.UserValidator;

@ExtendWith(MockitoExtension.class)
class TeacherValidatorTest {

    @Mock private UserValidator userValidator;

    private TeacherValidator validator;

    @BeforeEach
    void setUp() {
        validator = new TeacherValidator(userValidator);
    }

    private Teacher validTeacher() {
        return Teacher.builder()
                .id(UUID.randomUUID())
                .speciality("Math")
                .role(Role.TEACHER)
                .build();
    }

    @Test
    void should_accept_valid_teacher() {
        Teacher teacher = validTeacher();
        doNothing().when(userValidator).validateCommonFields(teacher);

        assertThatCode(() -> validator.accept(teacher)).doesNotThrowAnyException();

        verify(userValidator).validateCommonFields(teacher);
    }

    @Test
    void should_reject_null_speciality() {
        Teacher teacher = validTeacher();
        teacher.setSpeciality(null);
        doNothing().when(userValidator).validateCommonFields(teacher);

        assertThatThrownBy(() -> validator.accept(teacher))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("teacher speciality is required");
    }

    @Test
    void should_reject_blank_speciality() {
        Teacher teacher = validTeacher();
        teacher.setSpeciality("   ");
        doNothing().when(userValidator).validateCommonFields(teacher);

        assertThatThrownBy(() -> validator.accept(teacher))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("teacher speciality is required");
    }

    @Test
    void should_reject_wrong_role() {
        Teacher teacher = validTeacher();
        teacher.setRole(Role.ADMIN);
        doNothing().when(userValidator).validateCommonFields(teacher);

        assertThatThrownBy(() -> validator.accept(teacher))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("teacher role must be TEACHER");
    }
}
