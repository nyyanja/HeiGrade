package com.school.hei.service.services;

import com.school.hei.entity.JGroup;
import com.school.hei.entity.JStudent;
import com.school.hei.entity.JStudentGroupHistory;
import com.school.hei.mapper.GroupMapper;
import com.school.hei.mapper.StudentMapper;
import com.school.hei.model.Group;
import com.school.hei.model.Student;
import com.school.hei.repository.CourseRepository;
import com.school.hei.repository.ExamRepository;
import com.school.hei.repository.GroupRepository;
import com.school.hei.repository.SpecialityRepository;
import com.school.hei.repository.StudentGroupHistoryRepository;
import com.school.hei.repository.StudentRepository;
import com.school.hei.validator.GroupValidator;
import com.school.hei.validator.SpecialityChangeValidator;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class GroupService {

  private final GroupRepository groupRepository;
  private final GroupValidator groupValidator;
  private final StudentRepository studentRepository;
  private final SpecialityRepository specialityRepository;
  private final ExamRepository examRepository;
  private final CourseRepository courseRepository;
  private final StudentGroupHistoryRepository studentGroupHistoryRepository;
  private final SpecialityChangeValidator specialityChangeValidator;

  public List<Group> findAll() {
    return groupRepository.findAll().stream().map(this::toModelWithStudents).toList();
  }

  public Group findById(UUID id) {
    JGroup entity =
        groupRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "group not found with id " + id));
    return toModelWithStudents(entity);
  }

  @Transactional
  public Group save(Group group) {
    groupValidator.accept(group);
    JGroup saved = groupRepository.save(GroupMapper.toEntity(group));
    assignStudents(group.getStudents(), saved);
    return toModelWithStudents(saved);
  }

  @Transactional
  public Group update(UUID id, Group group) {
    findById(id);
    group.setId(id);
    groupValidator.accept(group);
    JGroup saved = groupRepository.save(GroupMapper.toEntity(group));
    assignStudents(group.getStudents(), saved);
    return toModelWithStudents(saved);
  }

  public void delete(UUID id) {
    if (!groupRepository.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "group not found with id " + id);
    }
    List<JStudent> students = studentRepository.findByGroup_Id(id);
    for (JStudent student : students) {
      student.setGroup(null);
      studentRepository.save(student);
    }
    groupRepository.deleteById(id);
  }

  public List<Group> findBySpeciality(UUID specialityId) {
    if (!specialityRepository.existsById(specialityId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "speciality not found");
    }
    return groupRepository.findBySpeciality_Id(specialityId).stream()
        .map(this::toModelWithStudents)
        .toList();
  }

  public List<Group> findByExam(UUID examId) {
    if (!examRepository.existsById(examId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "exam not found");
    }
    return groupRepository.findByExamId(examId).stream().map(this::toModelWithStudents).toList();
  }

  public List<Group> findByCourse(UUID courseId) {
    if (!courseRepository.existsById(courseId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "course not found");
    }
    return groupRepository.findByCourseId(courseId).stream()
        .map(this::toModelWithStudents)
        .toList();
  }

  public List<Group> findByName(String name) {
    if (name == null || name.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name is required");
    }
    return groupRepository.findByNameContainingIgnoreCase(name).stream()
        .map(this::toModelWithStudents)
        .toList();
  }

  private void assignStudents(List<Student> students, JGroup group) {
    if (students == null || students.isEmpty()) {
      return;
    }
    LocalDate today = LocalDate.now();

    for (Student student : students) {
      JStudent entity =
          studentRepository
              .findById(student.getId())
              .orElseThrow(
                  () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "student not found"));

      specialityChangeValidator.accept(entity, group);

      List<JStudentGroupHistory> history =
          studentGroupHistoryRepository.findByStudent_Id(entity.getId());
      for (JStudentGroupHistory h : history) {
        if (h.getEndDate() == null) {
          if (h.getGroup() != null && h.getGroup().getId().equals(group.getId())) {
            entity.setGroup(group);
            studentRepository.save(entity);
            return;
          }
          h.setEndDate(today.minusDays(1));
          studentGroupHistoryRepository.save(h);
        }
      }

      JStudentGroupHistory newEntry =
          JStudentGroupHistory.builder()
              .student(entity)
              .group(group)
              .startDate(today)
              .endDate(null)
              .build();
      studentGroupHistoryRepository.save(newEntry);

      entity.setGroup(group);
      studentRepository.save(entity);
    }
  }

  private Group toModelWithStudents(JGroup entity) {
    Group model = GroupMapper.toModel(entity);
    List<Student> students =
        studentRepository.findByGroup_Id(entity.getId()).stream()
            .map(StudentMapper::toModel)
            .toList();
    model.setStudents(students);
    return model;
  }
}
