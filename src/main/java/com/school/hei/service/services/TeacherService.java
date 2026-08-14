package com.school.hei.service.services;

import com.school.hei.entity.JTeacher;
import com.school.hei.mapper.TeacherMapper;
import com.school.hei.model.Teacher;
import com.school.hei.repository.TeacherRepository;
import com.school.hei.validator.TeacherValidator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class TeacherService {

  private final TeacherRepository teacherRepository;
  private final TeacherValidator teacherValidator;

  public List<Teacher> findAll() {
    return teacherRepository.findAll().stream().map(TeacherMapper::toModel).toList();
  }

  public Teacher findById(UUID id) {
    return teacherRepository
        .findById(id)
        .map(TeacherMapper::toModel)
        .orElseThrow(
            () ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "teacher not found with id " + id));
  }

  public Teacher save(Teacher teacher) {
    teacherValidator.accept(teacher);

    JTeacher entity = TeacherMapper.toEntity(teacher);
    return TeacherMapper.toModel(teacherRepository.save(entity));
  }

  public Teacher update(UUID id, Teacher teacher) {
    findById(id);

    teacher.setId(id);
    teacherValidator.accept(teacher);

    JTeacher entity = TeacherMapper.toEntity(teacher);
    return TeacherMapper.toModel(teacherRepository.save(entity));
  }

  public void delete(UUID id) {
    if (!teacherRepository.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "teacher not found with id " + id);
    }

    teacherRepository.deleteById(id);
  }
}
