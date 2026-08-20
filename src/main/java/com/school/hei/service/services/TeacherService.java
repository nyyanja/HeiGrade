package com.school.hei.service.services;

import com.school.hei.entity.JTeacher;
import com.school.hei.mapper.TeacherMapper;
import com.school.hei.model.Teacher;
import com.school.hei.repository.CourseRepository;
import com.school.hei.repository.TeacherRepository;
import com.school.hei.validator.TeacherValidator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class TeacherService {

  private final TeacherRepository teacherRepository;
  private final TeacherValidator teacherValidator;
  private final CourseRepository courseRepository;
  private final PasswordEncoder passwordEncoder;

  public List<Teacher> findBySpeciality(String speciality) {
    if (speciality == null || speciality.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "speciality is required");
    }
    return teacherRepository.findBySpecialityContainingIgnoreCase(speciality).stream()
            .map(TeacherMapper::toModel)
            .toList();
  }

  public List<Teacher> findByName(String name) {
    if (name == null || name.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name is required");
    }
    return teacherRepository
            .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(name, name)
            .stream()
            .map(TeacherMapper::toModel)
            .toList();
  }

  public List<Teacher> findByCourse(UUID courseId) {
    if (!courseRepository.existsById(courseId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "course not found");
    }
    return teacherRepository.findByCourseId(courseId).stream().map(TeacherMapper::toModel).toList();
  }

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

  @Transactional
  public Teacher save(Teacher teacher) {
    teacherValidator.accept(teacher);

    if (teacher.getPassword() == null || teacher.getPassword().isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "password is required");
    }

    JTeacher entity = TeacherMapper.toEntity(teacher);
    entity.setPassword(passwordEncoder.encode(teacher.getPassword()));

    return TeacherMapper.toModel(teacherRepository.save(entity));
  }

  @Transactional
  public Teacher update(UUID id, Teacher teacher) {
    JTeacher existing =
            teacherRepository
                    .findById(id)
                    .orElseThrow(
                            () ->
                                    new ResponseStatusException(
                                            HttpStatus.NOT_FOUND, "teacher not found with id " + id));

    teacher.setId(id);
    teacherValidator.accept(teacher);

    existing.setFirstName(teacher.getFirstName());
    existing.setLastName(teacher.getLastName());
    existing.setBirthday(teacher.getBirthday());
    existing.setSex(teacher.getSex());
    existing.setAddress(teacher.getAddress());
    existing.setEmail(teacher.getEmail());
    existing.setRole(teacher.getRole());
    existing.setSpeciality(teacher.getSpeciality());

    if (teacher.getPassword() != null && !teacher.getPassword().isBlank()) {
      existing.setPassword(passwordEncoder.encode(teacher.getPassword()));
    }

    return TeacherMapper.toModel(teacherRepository.save(existing));
  }

  public void delete(UUID id) {
    if (!teacherRepository.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "teacher not found with id " + id);
    }
    teacherRepository.deleteById(id);
  }
}
