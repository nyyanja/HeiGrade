package com.school.hei.validator;

import com.school.hei.model.Group;
import com.school.hei.repository.PromotionRepository;
import com.school.hei.repository.SpecialityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class GroupValidator implements SaveValidator<Group> {

  private final PromotionRepository promotionRepository;
  private final SpecialityRepository specialityRepository;

  @Override
  public void accept(Group group) {
    if (group == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "group cannot be null");
    }
    if (group.getName() == null || group.getName().isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "group name is required");
    }
    if (group.getSpeciality() == null || group.getSpeciality().getId() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "group speciality is required");
    }
    if (!specialityRepository.existsById(group.getSpeciality().getId())) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "speciality not found");
    }
    if (group.getPromotion() != null && group.getPromotion().getId() != null) {
      if (!promotionRepository.existsById(group.getPromotion().getId())) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "promotion not found");
      }
    }
  }
}