package com.school.hei.service.services;

import com.school.hei.entity.JSpeciality;
import com.school.hei.mapper.SpecialityMapper;
import com.school.hei.model.Speciality;
import com.school.hei.repository.SpecialityRepository;
import com.school.hei.validator.SpecialityValidator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class SpecialityService {

  private final SpecialityRepository specialityRepository;
  private final SpecialityValidator specialityValidator;

  public List<Speciality> findAll() {
    return specialityRepository.findAll().stream().map(SpecialityMapper::toModel).toList();
  }

  public Speciality findById(UUID id) {
    return specialityRepository
        .findById(id)
        .map(SpecialityMapper::toModel)
        .orElseThrow(
            () ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "speciality not found with id " + id));
  }

  public Speciality save(Speciality speciality) {
    specialityValidator.accept(speciality);

    JSpeciality entity = SpecialityMapper.toEntity(speciality);
    return SpecialityMapper.toModel(specialityRepository.save(entity));
  }

  public Speciality update(UUID id, Speciality speciality) {
    findById(id);

    speciality.setId(id);
    specialityValidator.accept(speciality);

    JSpeciality entity = SpecialityMapper.toEntity(speciality);
    return SpecialityMapper.toModel(specialityRepository.save(entity));
  }

  public void delete(UUID id) {
    if (!specialityRepository.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "speciality not found with id " + id);
    }

    specialityRepository.deleteById(id);
  }
}


