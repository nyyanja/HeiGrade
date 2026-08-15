package com.school.hei.service.services;

import com.school.hei.entity.JGrade;
import com.school.hei.entity.JGradeHistory;
import com.school.hei.entity.JUser;
import com.school.hei.mapper.GradeHistoryMapper;
import com.school.hei.mapper.GradeMapper;
import com.school.hei.model.Grade;
import com.school.hei.repository.CourseRepository;
import com.school.hei.repository.ExamRepository;
import com.school.hei.repository.GradeHistoryRepository;
import com.school.hei.repository.GradeRepository;
import com.school.hei.repository.GroupRepository;
import com.school.hei.repository.SpecialityRepository;
import com.school.hei.repository.StudentRepository;
import com.school.hei.repository.UserRepository;
import com.school.hei.validator.GradeHistoryValidator;
import com.school.hei.validator.GradeValidator;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class GradeService {

  private final GradeRepository gradeRepository;
  private final GradeHistoryRepository gradeHistoryRepository;

  private final GradeValidator gradeValidator;
  private final GradeHistoryValidator gradeHistoryValidator;

  private final ExamRepository examRepository;
  private final StudentRepository studentRepository;
  private final CourseRepository courseRepository;
  private final GroupRepository groupRepository;
  private final SpecialityRepository specialityRepository;
  private final UserRepository userRepository;

  public List<Grade> findAll() {
    return gradeRepository.findAll().stream().map(GradeMapper::toModel).toList();
  }

  public Grade findById(UUID id) {

    if (id == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "grade id is required");
    }

    return gradeRepository
        .findById(id)
        .map(GradeMapper::toModel)
        .orElseThrow(
            () ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "grade not found with id " + id));
  }

  @Transactional
  public Grade save(Grade grade) {

    if (grade == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "grade cannot be null");
    }

    gradeValidator.accept(grade);

    if (grade.getDate() == null) {
      grade.setDate(LocalDate.now());
    }

    JGrade entity = GradeMapper.toEntity(grade);

    JGrade savedEntity = gradeRepository.save(entity);

    return GradeMapper.toModel(savedEntity);
  }

  @Transactional
  public List<Grade> saveAll(List<Grade> grades) {

    if (grades == null || grades.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "at least one grade is required");
    }

    for (Grade grade : grades) {
      gradeValidator.accept(grade);
    }
    List<Grade> savedGrades = new ArrayList<>();

    for (Grade grade : grades) {

      if (grade.getDate() == null) {
        grade.setDate(LocalDate.now());
      }

      JGrade entity = GradeMapper.toEntity(grade);

      JGrade savedEntity = gradeRepository.save(entity);

      savedGrades.add(GradeMapper.toModel(savedEntity));
    }

    return savedGrades;
  }

  @Transactional
  public Grade update(UUID id, Grade grade, String reason, UUID modifiedById) {

    if (id == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "grade id is required");
    }

    if (reason == null || reason.isBlank()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "a reason is required to modify a grade");
    }
    if (modifiedById == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "modifier is required");
    }
    JGrade existingEntity =
        gradeRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "grade not found with id " + id));

    if (!userRepository.existsById(modifiedById)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "modifier not found");
    }
    if (grade == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "grade cannot be null");
    }
    grade.setId(id);

    gradeValidator.accept(grade);

    Double oldValue = existingEntity.getValue();
    Double newValue = grade.getValue();

    boolean valueChanged = !oldValue.equals(newValue);

    if (valueChanged) {

      JGradeHistory history =
          JGradeHistory.builder()
              .date(LocalDateTime.now())
              .oldValue(oldValue)
              .newValue(newValue)
              .reason(reason)
              .grade(existingEntity)
              .modifiedBy(JUser.builder().id(modifiedById).build())
              .build();
      gradeHistoryValidator.accept(GradeHistoryMapper.toModel(history));

      gradeHistoryRepository.save(history);
    }

    JGrade updatedEntity = gradeRepository.save(GradeMapper.toEntity(grade));

    return GradeMapper.toModel(updatedEntity);
  }

  public void delete(UUID id) {

    if (id == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "grade id is required");
    }

    if (!gradeRepository.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "grade not found with id " + id);
    }

    gradeRepository.deleteById(id);
  }

  public List<Grade> findByExam(UUID examId) {

    requireExistingExam(examId);

    return gradeRepository.findByExam_Id(examId).stream().map(GradeMapper::toModel).toList();
  }

  public List<Grade> findByStudent(UUID studentId) {

    requireExistingStudent(studentId);

    return gradeRepository.findByStudent_Id(studentId).stream().map(GradeMapper::toModel).toList();
  }

  public List<Grade> findByCourse(UUID courseId) {

    requireExistingCourse(courseId);

    return gradeRepository.findByCourseId(courseId).stream().map(GradeMapper::toModel).toList();
  }

  public List<Grade> findByStudentAndCourse(UUID studentId, UUID courseId) {

    requireExistingStudent(studentId);
    requireExistingCourse(courseId);

    return gradeRepository.findByStudentIdAndCourseId(studentId, courseId).stream()
        .map(GradeMapper::toModel)
        .toList();
  }

  public List<Grade> findByGroup(UUID groupId) {

    requireExistingGroup(groupId);

    return gradeRepository.findByGroupId(groupId).stream().map(GradeMapper::toModel).toList();
  }

  public List<Grade> findByGroupAndExam(UUID groupId, UUID examId) {

    requireExistingGroup(groupId);
    requireExistingExam(examId);

    return gradeRepository.findByGroupIdAndExamId(groupId, examId).stream()
        .map(GradeMapper::toModel)
        .toList();
  }

  public List<Grade> findByGroupAndCourse(UUID groupId, UUID courseId) {

    requireExistingGroup(groupId);
    requireExistingCourse(courseId);

    return gradeRepository.findByGroupIdAndCourseId(groupId, courseId).stream()
        .map(GradeMapper::toModel)
        .toList();
  }

  public List<Grade> findBySpeciality(UUID specialityId) {

    requireExistingSpeciality(specialityId);

    return gradeRepository.findBySpecialityId(specialityId).stream()
        .map(GradeMapper::toModel)
        .toList();
  }

  public List<Grade> findBySpecialityAndExam(UUID specialityId, UUID examId) {

    requireExistingSpeciality(specialityId);
    requireExistingExam(examId);

    return gradeRepository.findBySpecialityIdAndExamId(specialityId, examId).stream()
        .map(GradeMapper::toModel)
        .toList();
  }

  public List<Grade> findBySpecialityAndCourse(UUID specialityId, UUID courseId) {

    requireExistingSpeciality(specialityId);
    requireExistingCourse(courseId);

    return gradeRepository.findBySpecialityIdAndCourseId(specialityId, courseId).stream()
        .map(GradeMapper::toModel)
        .toList();
  }

  public Grade findByStudentAndExam(UUID studentId, UUID examId) {

    requireExistingStudent(studentId);
    requireExistingExam(examId);

    return gradeRepository
        .findByStudent_IdAndExam_Id(studentId, examId)
        .map(GradeMapper::toModel)
        .orElseThrow(
            () ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "grade not found for this student and exam"));
  }

  public List<Grade> findByDate(LocalDate date) {

    if (date == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "date is required");
    }

    return gradeRepository.findByDate(date).stream().map(GradeMapper::toModel).toList();
  }

  public List<Grade> findByMinValue(Double minValue) {

    if (minValue == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "minValue is required");
    }

    if (minValue < 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "minValue cannot be negative");
    }

    return gradeRepository.findByValueGreaterThanEqual(minValue).stream()
        .map(GradeMapper::toModel)
        .toList();
  }

  private void requireExistingExam(UUID examId) {

    if (examId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "exam id is required");
    }

    if (!examRepository.existsById(examId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "exam not found");
    }
  }

  private void requireExistingStudent(UUID studentId) {

    if (studentId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "student id is required");
    }

    if (!studentRepository.existsById(studentId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "student not found");
    }
  }

  private void requireExistingCourse(UUID courseId) {

    if (courseId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "course id is required");
    }

    if (!courseRepository.existsById(courseId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "course not found");
    }
  }

  private void requireExistingGroup(UUID groupId) {

    if (groupId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "group id is required");
    }

    if (!groupRepository.existsById(groupId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "group not found");
    }
  }

  private void requireExistingSpeciality(UUID specialityId) {

    if (specialityId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "speciality id is required");
    }

    if (!specialityRepository.existsById(specialityId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "speciality not found");
    }
  }
}
