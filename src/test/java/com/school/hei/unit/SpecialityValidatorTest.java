package com.school.hei.unit;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.school.hei.entity.JSpeciality;
import com.school.hei.enums.GroupSpeciality;
import com.school.hei.model.Speciality;
import com.school.hei.repository.SpecialityRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import com.school.hei.validator.SpecialityValidator;

@ExtendWith(MockitoExtension.class)
class SpecialityValidatorTest {

    @Mock private SpecialityRepository specialityRepository;

    private SpecialityValidator validator;

    @BeforeEach
    void setUp() {
        validator = new SpecialityValidator(specialityRepository);
    }

    private Speciality validSpeciality() {
        return Speciality.builder().id(UUID.randomUUID()).name(GroupSpeciality.EL).build();
    }

    @Test
    void should_accept_new_speciality() {
        Speciality speciality = validSpeciality();
        when(specialityRepository.findByNameIgnoreCase("EL")).thenReturn(Optional.empty());

        assertThatCode(() -> validator.accept(speciality)).doesNotThrowAnyException();
    }

    @Test
    void should_reject_null_speciality() {
        assertThatThrownBy(() -> validator.accept(null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("speciality cannot be null");
    }

    @Test
    void should_reject_null_name() {
        Speciality speciality = validSpeciality();
        speciality.setName(null);

        assertThatThrownBy(() -> validator.accept(speciality))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("speciality name is required");
    }

    @Test
    void should_accept_when_existing_is_same_speciality() {
        Speciality speciality = validSpeciality();
        JSpeciality existing = new JSpeciality();
        existing.setId(speciality.getId());
        when(specialityRepository.findByNameIgnoreCase("EL")).thenReturn(Optional.of(existing));

        assertThatCode(() -> validator.accept(speciality)).doesNotThrowAnyException();
    }

    @Test
    void should_reject_when_name_already_used_by_another_speciality() {
        Speciality speciality = validSpeciality();
        JSpeciality existing = new JSpeciality();
        existing.setId(UUID.randomUUID());
        when(specialityRepository.findByNameIgnoreCase("EL")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> validator.accept(speciality))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("speciality already exists");
    }
}
