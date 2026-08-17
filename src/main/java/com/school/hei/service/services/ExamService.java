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
import com.school.hei.security.CourseAccessService;
import com.school.hei.validator.ExamValidator;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
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
  private final CourseAccessService courseAccessService;

  public List<Exam> findAll() {
    List<JExam> exams = examRepository.findAll();
    return filterExamsForCurrentUser(exams).stream().map(this::toModelWithGroups).toList();
  }

  public Exam findById(UUID id) {
    JExam entity =
        examRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "exam not found with id " + id));
    assertCanReadExam(entity);
    return toModelWithGroups(entity);
  }

  public List<Exam> findByCourse(UUID courseId) {
    if (!courseRepository.existsById(courseId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "course not found");
    }
    courseAccessService.assertCanAccessCourse(courseId);
    return examRepository.findByCourse_Id(courseId).stream().map(this::toModelWithGroups).toList();
  }

  public List<Exam> findByTitle(String title) {
    if (title == null || title.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "title is required");
    }
    List<JExam> exams = examRepository.findByTitleContainingIgnoreCase(title);
    return filterExamsForCurrentUser(exams).stream().map(this::toModelWithGroups).toList();
  }

  public List<Exam> findByDate(LocalDate date) {
    if (date == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "date is required");
    }
    List<JExam> exams = examRepository.findByDate(date);
    return filterExamsForCurrentUser(exams).stream().map(this::toModelWithGroups).toList();
  }

  public List<Exam> findByCoeff(Double coeff) {
    if (coeff == null || coeff <= 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "coeff must be greater than 0");
    }
    List<JExam> exams = examRepository.findByCoeff(coeff);
    return filterExamsForCurrentUser(exams).stream().map(this::toModelWithGroups).toList();
  }

  public List<Exam> findByGroup(UUID groupId) {
    if (!groupRepository.existsById(groupId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "group not found");
    }
    List<JExam> exams = examRepository.findByGroupId(groupId);
    return filterExamsForCurrentUser(exams).stream().map(this::toModelWithGroups).toList();
  }

  public List<Exam> findBySpeciality(UUID specialityId) {
    if (!specialityRepository.existsById(specialityId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "speciality not found");
    }
    List<JExam> exams = examRepository.findBySpecialityId(specialityId);
    return filterExamsForCurrentUser(exams).stream().map(this::toModelWithGroups).toList();
  }

  public Double remainingCoeff(UUID courseId) {
    if (!courseRepository.existsById(courseId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "course not found");
    }
    courseAccessService.assertCanAccessCourse(courseId);
    double used =
        examRepository.findByCourse_Id(courseId).stream().mapToDouble(JExam::getCoeff).sum();
    return COEFF_TOTAL - used;
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

  private List<JExam> filterExamsForCurrentUser(List<JExam> exams) {
    if (courseAccessService.isAdmin()) {
      return exams;
    }
    if (courseAccessService.isTeacher()) {
      Set<UUID> courseIds = courseAccessService.taughtCourseIds();
      return exams.stream()
          .filter(e -> e.getCourse() != null && courseIds.contains(e.getCourse().getId()))
          .toList();
    }
    if (courseAccessService.isStudent()) {
      return exams;
    }
    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "access denied");
  }

  private void assertCanReadExam(JExam entity) {
    if (courseAccessService.isAdmin()) {
      return;
    }
    if (courseAccessService.isTeacher()) {
      if (entity.getCourse() == null || entity.getCourse().getId() == null) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "exam has no course");
      }
      courseAccessService.assertCanAccessCourse(entity.getCourse().getId());
      return;
    }
    if (courseAccessService.isStudent()) {
      return;
    }
    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "access denied");
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
}


