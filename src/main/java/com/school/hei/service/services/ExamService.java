package com.school.hei.service.services;

import com.school.hei.entity.JExam;
import com.school.hei.entity.JGroupExam;
import com.school.hei.mapper.ExamMapper;
import com.school.hei.mapper.GroupMapper;
import com.school.hei.model.Exam;
import com.school.hei.model.Group;
import com.school.hei.repository.CourseRepository;
import com.school.hei.repository.ExamRepository;
import com.school.hei.repository.GroupExamRepository;
import com.school.hei.repository.GroupRepository;
import com.school.hei.repository.SpecialityRepository;
import com.school.hei.validator.ExamValidator;
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
public class ExamService {

  private static final double COEFF_TOTAL = 1.0;
  private static final double COEFF_EPSILON = 0.0001;

  private final ExamRepository examRepository;
  private final ExamValidator examValidator;
  private final CourseRepository courseRepository;
  private final GroupRepository groupRepository;
  private final SpecialityRepository specialityRepository;
  private final GroupExamRepository groupExamRepository;

  public List<Exam> findAll() {
    return examRepository.findAll().stream().map(this::toModelWithGroups).toList();
  }

  public Exam findById(UUID id) {
    JExam entity =
        examRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "exam not found with id " + id));
    return toModelWithGroups(entity);
  }

  @Transactional
  public Exam save(Exam exam) {
    examValidator.accept(exam);
    validateGroups(exam);
    validateCoeffSum(exam, null);

    JExam savedExam = examRepository.save(ExamMapper.toEntity(exam));
    exam.setId(savedExam.getId());
    saveGroupExams(exam, savedExam);

    return toModelWithGroups(savedExam);
  }

  @Transactional
  public Exam update(UUID id, Exam exam) {
    findById(id);
    exam.setId(id);
    examValidator.accept(exam);
    validateGroups(exam);
    validateCoeffSum(exam, id);

    JExam savedExam = examRepository.save(ExamMapper.toEntity(exam));
    groupExamRepository.deleteByExam_Id(id);
    saveGroupExams(exam, savedExam);

    return toModelWithGroups(savedExam);
  }

  public void delete(UUID id) {
    if (!examRepository.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "exam not found with id " + id);
    }
    groupExamRepository.deleteByExam_Id(id);
    examRepository.deleteById(id);
  }

  public Double remainingCoeff(UUID courseId) {
    if (!courseRepository.existsById(courseId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "course not found");
    }
    double used =
        examRepository.findByCourse_Id(courseId).stream().mapToDouble(JExam::getCoeff).sum();
    return COEFF_TOTAL - used;
  }

  private void validateGroups(Exam exam) {
    if (exam.getGroups() == null || exam.getGroups().isEmpty()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "at least one group is required to create an exam");
    }
    for (Group group : exam.getGroups()) {
      if (group == null || group.getId() == null) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "group id is required");
      }
      if (!groupRepository.existsById(group.getId())) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "group not found");
      }
    }
  }

  private void validateCoeffSum(Exam exam, UUID excludedExamId) {
    UUID courseId = exam.getCourse().getId();
    double existingSum =
        examRepository.findByCourse_Id(courseId).stream()
            .filter(e -> excludedExamId == null || !e.getId().equals(excludedExamId))
            .mapToDouble(JExam::getCoeff)
            .sum();
    double total = existingSum + exam.getCoeff();
    if (total > COEFF_TOTAL + COEFF_EPSILON) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          String.format(
              "the sum of exam coefficients for this course would reach %.2f, it cannot exceed 1",
              total));
    }
  }

  private void saveGroupExams(Exam exam, JExam savedExam) {
    for (Group group : exam.getGroups()) {
      JGroupExam entity =
          JGroupExam.builder()
              .group(groupRepository.getReferenceById(group.getId()))
              .exam(savedExam)
              .build();
      groupExamRepository.save(entity);
    }
  }

  private Exam toModelWithGroups(JExam entity) {
    Exam model = ExamMapper.toModel(entity);
    model.setGroups(
        groupExamRepository.findByExam_Id(entity.getId()).stream()
            .map(ge -> GroupMapper.toModel(ge.getGroup()))
            .toList());
    return model;
  }

  public List<Exam> findByCourse(UUID courseId) {
    if (!courseRepository.existsById(courseId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "course not found");
    }
    return examRepository.findByCourse_Id(courseId).stream().map(ExamMapper::toModel).toList();
  }

  public List<Exam> findByTitle(String title) {
    if (title == null || title.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "title is required");
    }
    return examRepository.findByTitleContainingIgnoreCase(title).stream()
        .map(ExamMapper::toModel)
        .toList();
  }

  public List<Exam> findByDate(LocalDate date) {
    if (date == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "date is required");
    }
    return examRepository.findByDate(date).stream().map(ExamMapper::toModel).toList();
  }

  public List<Exam> findByCoeff(Double coeff) {
    if (coeff == null || coeff <= 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "coeff must be greater than 0");
    }
    return examRepository.findByCoeff(coeff).stream().map(ExamMapper::toModel).toList();
  }

  public List<Exam> findByGroup(UUID groupId) {
    if (!groupRepository.existsById(groupId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "group not found");
    }
    return examRepository.findByGroupId(groupId).stream().map(ExamMapper::toModel).toList();
  }

  public List<Exam> findBySpeciality(UUID specialityId) {
    if (!specialityRepository.existsById(specialityId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "speciality not found");
    }
    return examRepository.findBySpecialityId(specialityId).stream()
        .map(ExamMapper::toModel)
        .toList();
  }
}
