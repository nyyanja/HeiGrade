package com.school.hei.service.services;

import com.school.hei.mapper.TeacherCourseMapper;
import com.school.hei.model.TeacherCourse;
import com.school.hei.repository.TeacherCourseRepository;
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
}