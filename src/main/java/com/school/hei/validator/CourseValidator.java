package com.school.hei.validator;

import com.school.hei.model.Course;
import com.school.hei.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class CourseValidator implements SaveValidator<Course> {

    private final CourseRepository courseRepository;

    @Override
    public void accept(Course course) {
        if (course == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "the course is null it is not allowed");
        }
        if (course.getReference() == null || course.getReference().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "course's is blank or null it is not allowed");
        }
        if (course.getTitle() == null || course.getTitle().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "course's title cannot be blank or null");
        }
        if (course.getCredit() == null || course.getCredit() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "the course credit cannot be less than 0");
        }

        courseRepository
                .findByReferenceIgnoreCase(course.getReference())
                .ifPresent(
                        existing -> {
                            boolean isSameCourse = course.getId() != null && existing.getId().equals(course.getId());
                            if (!isSameCourse) {
                                throw new ResponseStatusException(HttpStatus.CONFLICT, "this course has already been saved");
                            }
                        });
    }
}