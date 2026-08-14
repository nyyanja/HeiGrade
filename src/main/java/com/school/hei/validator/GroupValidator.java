package com.school.hei.validator;

import com.school.hei.enums.GroupSpeciality;
import com.school.hei.model.Group;
import com.school.hei.repository.PromotionRepository;
import com.school.hei.repository.SpecialityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class GroupValidator implements SaveValidator<Group> {

    private final PromotionRepository promotionRepository;
    private final SpecialityRepository specialityRepository;

    @Override
    public void accept(Group group) {
        if (group == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "the parameter is null or empty it  cannot be saved");
        }
        if (group.getName() == null || group.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "group name cannot be null or empty it is not allowed");
        }
        if (group.getPromotion() != null && group.getPromotion().getId() != null) {
            if (!promotionRepository.existsById(group.getPromotion().getId())) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Promotion not found");
            }
        }
        if (group.getSpeciality() == null || group.getSpeciality().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "the speciality name cannot be null or empty it is not allowed");
        }

        if (!specialityRepository.existsById(group.getSpeciality().getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "speciality not found");
        }
        validateSpecialityIsAllowedValue(group.getSpeciality().getName());
    }
        private void validateSpecialityIsAllowedValue(String specialityName) {
            boolean isAllowed =
                    Arrays.stream(GroupSpeciality.values())
                            .anyMatch(allowed -> allowed.name().equalsIgnoreCase(specialityName));
            if (!isAllowed) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "the special name have to be one of : "
                                + Arrays.toString(GroupSpeciality.values()));
            }
        }
    }
