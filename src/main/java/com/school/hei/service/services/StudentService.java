package com.school.hei.service.services;

import com.school.hei.entity.JStudent;
import com.school.hei.mapper.StudentMapper;
import com.school.hei.model.Student;
import com.school.hei.repository.StudentRepository;
import com.school.hei.validator.StudentValidator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class StudentService {

  private final StudentRepository studentRepository;
  private final StudentValidator studentValidator;

  public List<Student> findAll() {
    return studentRepository.findAll().stream().map(StudentMapper::toModel).toList();
  }

  public Student findById(UUID id) {
    return studentRepository
        .findById(id)
        .map(StudentMapper::toModel)
        .orElseThrow(
            () ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "student not found with id " + id));
  }

  public Student save(Student student) {
    studentValidator.accept(student);

    JStudent entity = StudentMapper.toEntity(student);
    return StudentMapper.toModel(studentRepository.save(entity));
  }

  public Student update(UUID id, Student student) {
    findById(id);

    student.setId(id);
    studentValidator.accept(student);

    JStudent entity = StudentMapper.toEntity(student);
    return StudentMapper.toModel(studentRepository.save(entity));
  }

  public void delete(UUID id) {
    if (!studentRepository.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "student not found with id " + id);
    }

    studentRepository.deleteById(id);
  }
}
