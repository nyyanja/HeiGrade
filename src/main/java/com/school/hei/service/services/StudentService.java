package com.school.hei.service.services;

import com.school.hei.entity.JStudent;
import com.school.hei.mapper.StudentMapper;
import com.school.hei.model.Student;
import com.school.hei.repository.GroupRepository;
import com.school.hei.repository.SpecialityRepository;
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
  private final GroupRepository groupRepository;
  private final SpecialityRepository specialityRepository;

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

  public Student findByReference(String reference) {
    return studentRepository
        .findByReference(reference)
        .map(StudentMapper::toModel)
        .orElseThrow(
            () ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "student not found with reference " + reference));
  }

  public List<Student> findByName(String name) {
    if (name == null || name.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name is required");
    }
    return studentRepository
        .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(name, name)
        .stream()
        .map(StudentMapper::toModel)
        .toList();
  }

  public List<Student> findByGroup(UUID groupId) {
    if (!groupRepository.existsById(groupId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "group not found");
    }
    return studentRepository.findByGroup_Id(groupId).stream().map(StudentMapper::toModel).toList();
  }

  public List<Student> findBySpeciality(UUID specialityId) {
    if (!specialityRepository.existsById(specialityId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "speciality not found");
    }
    return studentRepository.findByGroup_Speciality_Id(specialityId).stream()
        .map(StudentMapper::toModel)
        .toList();
  }

  public Student save(Student student) {
    studentValidator.accept(student);

    JStudent entity = StudentMapper.toEntity(student);
    return StudentMapper.toModel(studentRepository.save(entity));
  }

  public List<Student> saveAll(List<Student> students) {
    if (students == null || students.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "at least one student is required");
    }

    students.forEach(studentValidator::accept);

    return studentRepository
        .saveAll(students.stream().map(StudentMapper::toEntity).toList())
        .stream()
        .map(StudentMapper::toModel)
        .toList();
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
