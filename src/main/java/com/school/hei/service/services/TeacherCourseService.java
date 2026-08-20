package com.school.hei.service.services;

import com.school.hei.entity.JTeacherCourse;
import com.school.hei.mapper.TeacherCourseMapper;
import com.school.hei.model.TeacherCourse;
import com.school.hei.repository.TeacherCourseRepository;
import com.school.hei.validator.TeacherCourseValidator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class TeacherCourseService {

  private final TeacherCourseRepository teacherCourseRepository;
  private final TeacherCourseValidator teacherCourseValidator;

  public List<TeacherCourse> findAll() {
    return teacherCourseRepository.findAll().stream().map(TeacherCourseMapper::toModel).toList();
  }

  public TeacherCourse findById(UUID id) {
    return teacherCourseRepository
        .findById(id)
        .map(TeacherCourseMapper::toModel)
        .orElseThrow(
            () ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "teacher course not found with id " + id));
  }

  public TeacherCourse save(TeacherCourse teacherCourse) {
    teacherCourseValidator.accept(teacherCourse);

    JTeacherCourse entity = TeacherCourseMapper.toEntity(teacherCourse);

    return TeacherCourseMapper.toModel(teacherCourseRepository.save(entity));
  }

  public TeacherCourse update(UUID id, TeacherCourse teacherCourse) {

    findById(id);

    teacherCourse.setId(id);
    teacherCourseValidator.accept(teacherCourse);

    JTeacherCourse entity = TeacherCourseMapper.toEntity(teacherCourse);

    return TeacherCourseMapper.toModel(teacherCourseRepository.save(entity));
  }

  public void delete(UUID id) {
    if (!teacherCourseRepository.existsById(id)) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND, "teacher course not found with id " + id);
    }

    teacherCourseRepository.deleteById(id);
  }
}

