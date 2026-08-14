package com.school.hei.validator;

import com.school.hei.model.SpecialityCourse;
import com.school.hei.repository.CourseRepository;
import com.school.hei.repository.SpecialityCourseRepository;
import com.school.hei.repository.SpecialityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class SpecialityCourseValidator implements SaveValidator<SpecialityCourse> {

    private final SpecialityRepository specialityRepository;
    private final CourseRepository courseRepository;
    private final SpecialityCourseRepository specialityCourseRepository;

    @Override
    public void accept(SpecialityCourse specialityCourse) {
        if (specialityCourse == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "the specialty and course association cannot be null");
        }
        if (specialityCourse.getSpeciality() == null || specialityCourse.getSpeciality().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "the specialty  cannot be null");
        }
        if (specialityCourse.getCourse() == null || specialityCourse.getCourse().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "the course cannot be null");
        }
        if (!specialityRepository.existsById(specialityCourse.getSpeciality().getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "speciality not found");
        }
        if (!courseRepository.existsById(specialityCourse.getCourse().getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "course not found");
        }

        specialityCourseRepository
                .findBySpeciality_IdAndCourse_Id(
                        specialityCourse.getSpeciality().getId(), specialityCourse.getCourse().getId())
                .ifPresent(
                        existing -> {
                            boolean isSame = specialityCourse.getId() != null && existing.getId().equals(specialityCourse.getId());
                            if (!isSame) {
                                throw new ResponseStatusException(HttpStatus.CONFLICT, "this course has already been saved for this speciality");
                            }
                        });
    }
}