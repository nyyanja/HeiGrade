package com.school.hei.validator;

import com.school.hei.model.Speciality;
import com.school.hei.repository.SpecialityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class SpecialityValidator implements SaveValidator<Speciality> {

  private final SpecialityRepository specialityRepository;

  @Override
  public void accept(Speciality speciality) {
    if (speciality == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "speciality cannot be null");
    }
    if (speciality.getName() == null) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "speciality name is required (EL, TN or COMMON_PART)");
    }

    specialityRepository
        .findByNameIgnoreCase(speciality.getName().name())
        .ifPresent(
            existing -> {
              boolean isSame =
                  speciality.getId() != null && existing.getId().equals(speciality.getId());
              if (!isSame) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "speciality already exists");
              }
            });
  }
}

