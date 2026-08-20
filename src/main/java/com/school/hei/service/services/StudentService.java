package com.school.hei.service.services;

import com.school.hei.entity.JGroup;
import com.school.hei.entity.JStudent;
import com.school.hei.entity.JStudentGroupHistory;
import com.school.hei.mapper.StudentMapper;
import com.school.hei.model.Student;
import com.school.hei.repository.GroupRepository;
import com.school.hei.repository.SpecialityRepository;
import com.school.hei.repository.StudentGroupHistoryRepository;
import com.school.hei.repository.StudentRepository;
import com.school.hei.security.CourseAccessService;
import com.school.hei.validator.SpecialityChangeValidator;
import com.school.hei.validator.StudentValidator;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class StudentService {

  private final StudentRepository studentRepository;
  private final StudentValidator studentValidator;
  private final GroupRepository groupRepository;
  private final SpecialityRepository specialityRepository;
  private final StudentGroupHistoryRepository studentGroupHistoryRepository;
  private final SpecialityChangeValidator specialityChangeValidator;
  private final CourseAccessService courseAccessService;
  private final PasswordEncoder passwordEncoder;

  @Transactional
  public Student save(Student student) {
    studentValidator.accept(student);

    if (student.getPassword() == null || student.getPassword().isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "password is required");
    }

    JStudent entity = StudentMapper.toEntity(student);
    entity.setPassword(passwordEncoder.encode(student.getPassword()));
    setGroup(entity, student);

    if (entity.getGroup() != null) {
      specialityChangeValidator.accept(entity, entity.getGroup());
    }

    JStudent savedStudent = studentRepository.save(entity);
    createInitialGroupHistory(savedStudent);

    return StudentMapper.toModel(savedStudent);
  }

  @Transactional
  public List<Student> saveAll(List<Student> students) {
    if (students == null || students.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "at least one student is required");
    }

    students.forEach(studentValidator::accept);

    List<JStudent> entities =
        students.stream()
            .map(
                student -> {
                  if (student.getPassword() == null || student.getPassword().isBlank()) {
                    throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "password is required for all students");
                  }
                  JStudent entity = StudentMapper.toEntity(student);
                  entity.setPassword(passwordEncoder.encode(student.getPassword()));
                  setGroup(entity, student);
                  if (entity.getGroup() != null) {
                    specialityChangeValidator.accept(entity, entity.getGroup());
                  }
                  return entity;
                })
            .toList();

    List<JStudent> savedStudents = studentRepository.saveAll(entities);
    savedStudents.forEach(this::createInitialGroupHistory);

    return savedStudents.stream().map(StudentMapper::toModel).toList();
  }

  @Transactional
  public Student update(UUID id, Student student) {
    JStudent existing =
        studentRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "student not found with id " + id));

    UUID oldGroupId = existing.getGroup() == null ? null : existing.getGroup().getId();

    student.setId(id);
    studentValidator.accept(student);

    existing.setFirstName(student.getFirstName());
    existing.setLastName(student.getLastName());
    existing.setBirthday(student.getBirthday());
    existing.setSex(student.getSex());
    existing.setAddress(student.getAddress());
    existing.setEmail(student.getEmail());
    existing.setRole(student.getRole());
    existing.setReference(student.getReference());

    if (student.getPassword() != null && !student.getPassword().isBlank()) {
      existing.setPassword(passwordEncoder.encode(student.getPassword()));
    }

    setGroup(existing, student);

    UUID newGroupId = existing.getGroup() == null ? null : existing.getGroup().getId();

    if (!Objects.equals(oldGroupId, newGroupId) && existing.getGroup() != null) {
      specialityChangeValidator.accept(existing, existing.getGroup());
    }

    JStudent updatedStudent = studentRepository.save(existing);

    if (!Objects.equals(oldGroupId, newGroupId)) {
      updateGroupHistory(updatedStudent, oldGroupId, newGroupId);
    }

    return StudentMapper.toModel(updatedStudent);
  }

  public void delete(UUID id) {
    if (!studentRepository.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "student not found with id " + id);
    }
    studentRepository.deleteById(id);
  }

  private void setGroup(JStudent entity, Student student) {
    if (student.getGroup() == null || student.getGroup().getId() == null) {
      entity.setGroup(null);
      return;
    }

    UUID groupId = student.getGroup().getId();
    JGroup group =
        groupRepository
            .findById(groupId)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "group not found with id " + groupId));

    entity.setGroup(group);
  }

  private void createInitialGroupHistory(JStudent student) {
    if (student.getGroup() == null) {
      return;
    }
    if (studentGroupHistoryRepository
        .findByStudent_IdAndEndDateIsNull(student.getId())
        .isPresent()) {
      return;
    }

    studentGroupHistoryRepository.save(
        JStudentGroupHistory.builder()
            .student(student)
            .group(student.getGroup())
            .startDate(LocalDate.now())
            .endDate(null)
            .build());
  }

  private void updateGroupHistory(JStudent student, UUID oldGroupId, UUID newGroupId) {
    studentGroupHistoryRepository
        .findByStudent_IdAndEndDateIsNull(student.getId())
        .ifPresent(
            history -> {
              history.setEndDate(LocalDate.now().minusDays(1));
              studentGroupHistoryRepository.save(history);
            });
    if (newGroupId != null && student.getGroup() != null) {
      studentGroupHistoryRepository.save(
          JStudentGroupHistory.builder()
              .student(student)
              .group(student.getGroup())
              .startDate(LocalDate.now())
              .endDate(null)
              .build());
    }
  }

  public List<Student> findAll() {
    if (courseAccessService.isStudent()) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "students cannot list all students");
    }
    return studentRepository.findAll().stream().map(StudentMapper::toModel).toList();
  }

  public Student findById(UUID id) {
    assertCanAccessStudent(id);
    return studentRepository
        .findById(id)
        .map(StudentMapper::toModel)
        .orElseThrow(
            () ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "student not found with id " + id));
  }

  public Student findByReference(String reference) {
    Student student =
        studentRepository
            .findByReference(reference)
            .map(StudentMapper::toModel)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "student not found with reference " + reference));
    assertCanAccessStudent(student.getId());
    return student;
  }

  public List<Student> findByName(String name) {
    if (courseAccessService.isStudent()) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "access denied");
    }
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
    if (courseAccessService.isStudent()) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "access denied");
    }
    if (!groupRepository.existsById(groupId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "group not found");
    }
    return studentRepository.findByGroup_Id(groupId).stream().map(StudentMapper::toModel).toList();
  }

  public List<Student> findBySpeciality(UUID specialityId) {
    if (courseAccessService.isStudent()) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "access denied");
    }
    if (!specialityRepository.existsById(specialityId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "speciality not found");
    }
    return studentRepository.findByGroup_Speciality_Id(specialityId).stream()
        .map(StudentMapper::toModel)
        .toList();
  }

  private void assertCanAccessStudent(UUID studentId) {
    if (courseAccessService.isAdmin() || courseAccessService.isTeacher()) {
      return;
    }
    if (courseAccessService.isStudent()) {
      if (!courseAccessService.currentUserId().equals(studentId)) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "not your profile");
      }
      return;
    }
    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "access denied");
  }
}
