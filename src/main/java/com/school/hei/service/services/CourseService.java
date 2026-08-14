package com.school.hei.service.services;

import com.school.hei.entity.JCourse;
import com.school.hei.mapper.CourseMapper;
import com.school.hei.model.Course;
import com.school.hei.repository.CourseRepository;
import com.school.hei.validator.CourseValidator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CourseService {

  private final CourseRepository courseRepository;
  private final CourseValidator courseValidator;

  public List<Course> findAll() {
    return courseRepository.findAll().stream().map(CourseMapper::toModel).toList();
  }

  public Course findById(UUID id) {
    return courseRepository
        .findById(id)
        .map(CourseMapper::toModel)
        .orElseThrow(
            () ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "course not found with id " + id));
  }

  public Course save(Course course) {
    courseValidator.accept(course);

    JCourse entity = CourseMapper.toEntity(course);
    return CourseMapper.toModel(courseRepository.save(entity));
  }

  public Course update(UUID id, Course course) {
    findById(id);

    course.setId(id);
    courseValidator.accept(course);

    JCourse entity = CourseMapper.toEntity(course);
    return CourseMapper.toModel(courseRepository.save(entity));
  }

  public void delete(UUID id) {
    if (!courseRepository.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "course not found with id " + id);
    }

    courseRepository.deleteById(id);
  }
}
