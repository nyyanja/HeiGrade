package com.school.hei.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.school.hei.entity.JSpeciality;
import com.school.hei.enums.GroupSpeciality;
import com.school.hei.model.Speciality;
import com.school.hei.repository.SpecialityRepository;
import com.school.hei.service.services.SpecialityService;
import com.school.hei.validator.SpecialityValidator;
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
class SpecialityServiceTest {

  @Mock private SpecialityRepository specialityRepository;

  @Mock private SpecialityValidator specialityValidator;

  @InjectMocks private SpecialityService specialityService;

  @Test
  void should_find_all_specialities() {
    JSpeciality speciality1 = JSpeciality.builder().id(UUID.randomUUID()).name("EL").build();

    JSpeciality speciality2 = JSpeciality.builder().id(UUID.randomUUID()).name("TN").build();

    when(specialityRepository.findAll()).thenReturn(List.of(speciality1, speciality2));

    List<Speciality> result = specialityService.findAll();

    assertThat(result).hasSize(2);
    assertThat(result)
        .extracting(Speciality::getName)
        .containsExactly(GroupSpeciality.EL, GroupSpeciality.TN);

    verify(specialityRepository).findAll();
  }

  @Test
  void should_find_speciality_by_id() {
    UUID id = UUID.randomUUID();

    JSpeciality entity = JSpeciality.builder().id(id).name("EL").build();

    when(specialityRepository.findById(id)).thenReturn(Optional.of(entity));

    Speciality result = specialityService.findById(id);

    assertThat(result.getId()).isEqualTo(id);
    assertThat(result.getName()).isEqualTo(GroupSpeciality.EL);

    verify(specialityRepository).findById(id);
  }

  @Test
  void should_throw_when_speciality_is_not_found() {
    UUID id = UUID.randomUUID();

    when(specialityRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(ResponseStatusException.class, () -> specialityService.findById(id));

    verify(specialityRepository).findById(id);
  }

  @Test
  void should_save_speciality() {
    Speciality speciality = Speciality.builder().name(GroupSpeciality.EL).build();

    UUID id = UUID.randomUUID();

    JSpeciality savedEntity = JSpeciality.builder().id(id).name("EL").build();

    when(specialityRepository.save(any(JSpeciality.class))).thenReturn(savedEntity);

    Speciality result = specialityService.save(speciality);

    assertThat(result.getId()).isEqualTo(id);
    assertThat(result.getName()).isEqualTo(GroupSpeciality.EL);

    verify(specialityValidator).accept(speciality);
    verify(specialityRepository).save(any(JSpeciality.class));
  }

  @Test
  void should_update_speciality() {
    UUID id = UUID.randomUUID();

    Speciality speciality = Speciality.builder().name(GroupSpeciality.TN).build();

    JSpeciality existingEntity = JSpeciality.builder().id(id).name("EL").build();

    JSpeciality updatedEntity = JSpeciality.builder().id(id).name("TN").build();

    when(specialityRepository.findById(id)).thenReturn(Optional.of(existingEntity));

    when(specialityRepository.save(any(JSpeciality.class))).thenReturn(updatedEntity);

    Speciality result = specialityService.update(id, speciality);

    assertThat(result.getId()).isEqualTo(id);
    assertThat(result.getName()).isEqualTo(GroupSpeciality.TN);
    assertThat(speciality.getId()).isEqualTo(id);

    verify(specialityRepository).findById(id);
    verify(specialityValidator).accept(speciality);
    verify(specialityRepository).save(any(JSpeciality.class));
  }

  @Test
  void should_throw_when_updating_unknown_speciality() {
    UUID id = UUID.randomUUID();

    Speciality speciality = Speciality.builder().name(GroupSpeciality.TN).build();

    when(specialityRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(ResponseStatusException.class, () -> specialityService.update(id, speciality));

    verify(specialityRepository).findById(id);
    verify(specialityRepository, never()).save(any(JSpeciality.class));
    verify(specialityValidator, never()).accept(any(Speciality.class));
  }

  @Test
  void should_delete_speciality() {
    UUID id = UUID.randomUUID();

    when(specialityRepository.existsById(id)).thenReturn(true);

    specialityService.delete(id);

    verify(specialityRepository).existsById(id);
    verify(specialityRepository).deleteById(id);
  }

  @Test
  void should_throw_when_deleting_unknown_speciality() {
    UUID id = UUID.randomUUID();

    when(specialityRepository.existsById(id)).thenReturn(false);

    assertThrows(ResponseStatusException.class, () -> specialityService.delete(id));

    verify(specialityRepository).existsById(id);
    verify(specialityRepository, never()).deleteById(id);
  }
}
