package com.school.hei.service.services;

import com.school.hei.entity.JCourse;
import com.school.hei.entity.JExam;
import com.school.hei.entity.JGrade;
import com.school.hei.entity.JGroup;
import com.school.hei.entity.JStudent;
import com.school.hei.entity.JStudentGroupHistory;
import com.school.hei.model.Transcript;
import com.school.hei.model.TranscriptCourseLine;
import com.school.hei.repository.CourseRepository;
import com.school.hei.repository.ExamRepository;
import com.school.hei.repository.GradeRepository;
import com.school.hei.repository.GroupExamRepository;
import com.school.hei.repository.GroupRepository;
import com.school.hei.repository.StudentGroupHistoryRepository;
import com.school.hei.repository.StudentRepository;
import com.school.hei.security.CourseAccessService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class TranscriptService {

  private static final double PASS_MARK = 10.0;
  private static final int YEAR_TOTAL_CREDITS = 60;
  private static final double COEFF_TOTAL = 1.0;
  private static final double COEFF_EPSILON = 0.0001;

  private final StudentRepository studentRepository;
  private final CourseRepository courseRepository;
  private final GradeRepository gradeRepository;
  private final StudentGroupHistoryRepository studentGroupHistoryRepository;
  private final GroupExamRepository groupExamRepository;
  private final ExamRepository examRepository;
  private final GroupRepository groupRepository;
  private final CourseAccessService courseAccessService;

  /**
   * Relevé pour un appel HTTP (avec contrôle d'accès).
   * STUDENT : uniquement son relevé.
   * ADMIN / TEACHER : OK.
   */
  @Transactional(readOnly = true)
  public Transcript getStudentTranscript(UUID studentId, Integer level) {
    validateLevel(level);
    assertCanAccessTranscript(studentId);
    return buildTranscript(studentId, level);
  }

  /**
   * Relevé pour le worker async (email / PDF).
   * Aucun contrôle JWT — usage interne uniquement.
   */
  @Transactional(readOnly = true)
  public Transcript getStudentTranscriptForSystem(UUID studentId, Integer level) {
    validateLevel(level);
    return buildTranscript(studentId, level);
  }

  /**
   * Tous les relevés d'un level.
   * Interdit aux STUDENT.
   */
  @Transactional(readOnly = true)
  public List<Transcript> getAllTranscripts(Integer level) {
    validateLevel(level);

    if (courseAccessService.isStudent()) {
      throw new ResponseStatusException(
              HttpStatus.FORBIDDEN, "students cannot list all transcripts");
    }

    List<Transcript> result = new ArrayList<>();
    for (JStudent student : studentRepository.findAll()) {
      try {
        // sans re-check sécu par étudiant (ADMIN/TEACHER déjà OK)
        result.add(buildTranscript(student.getId(), level));
      } catch (ResponseStatusException e) {
        if (e.getStatusCode().equals(HttpStatus.BAD_REQUEST)
                || e.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
          continue;
        }
        throw e;
      }
    }
    return result;
  }

  /** Cœur métier du relevé (sans sécurité). */
  private Transcript buildTranscript(UUID studentId, Integer level) {
    JStudent student =
            studentRepository
                    .findById(studentId)
                    .orElseThrow(
                            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "student not found"));

    List<JStudentGroupHistory> history =
            studentGroupHistoryRepository.findByStudent_Id(studentId);
    if (history.isEmpty()) {
      throw new ResponseStatusException(
              HttpStatus.BAD_REQUEST, "student has no group history");
    }

    Set<UUID> studentGroupIds = new HashSet<>();
    Set<UUID> specialityIds = new HashSet<>();
    for (JStudentGroupHistory h : history) {
      if (h.getGroup() != null) {
        studentGroupIds.add(h.getGroup().getId());
        if (h.getGroup().getSpeciality() != null) {
          specialityIds.add(h.getGroup().getSpeciality().getId());
        }
      }
    }

    boolean anyGroupYearComplete = false;
    for (UUID groupId : studentGroupIds) {
      if (isGroupYearCompleteForLevel(groupId, level)) {
        anyGroupYearComplete = true;
        break;
      }
    }
    if (!anyGroupYearComplete) {
      throw new ResponseStatusException(
              HttpStatus.BAD_REQUEST,
              "no group of this student has completed the year for level " + level);
    }

    List<JCourse> courses = resolveStudentCoursesForLevel(specialityIds, level);
    if (courses.isEmpty()) {
      throw new ResponseStatusException(
              HttpStatus.NOT_FOUND, "no course found for student path at level " + level);
    }

    List<JGrade> allGrades = gradeRepository.findByStudent_Id(studentId);
    Map<UUID, List<JGrade>> gradesByCourse = groupGradesByCourse(allGrades);
    Map<UUID, List<JExam>> allExamsByCourse =
            resolveAllExamsByCourseForStudentGroups(studentGroupIds, courses);

    List<TranscriptCourseLine> lines = new ArrayList<>();
    double weightedSum = 0.0;
    int totalCourseCredits = 0;
    int totalObtainedCredits = 0;

    for (JCourse course : courses) {
      List<JGrade> courseGrades = gradesByCourse.getOrDefault(course.getId(), List.of());
      List<JGrade> validGrades = filterGradesForStudentPath(student, courseGrades);

      if (validGrades.isEmpty()) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "student does not have a grade for course " + course.getReference());
      }

      List<JExam> courseExams = allExamsByCourse.getOrDefault(course.getId(), List.of());
      double average = calculateCourseAverage(validGrades, courseExams);
      int obtainedCredit = average >= PASS_MARK ? course.getCredit() : 0;

      lines.add(
              TranscriptCourseLine.builder()
                      .courseId(course.getId())
                      .reference(course.getReference())
                      .title(course.getTitle())
                      .credit(course.getCredit())
                      .average(average)
                      .obtainedCredit(obtainedCredit)
                      .build());

      weightedSum += average * course.getCredit();
      totalCourseCredits += course.getCredit();
      totalObtainedCredits += obtainedCredit;
    }

    double generalAverage =
            totalCourseCredits == 0 ? 0.0 : weightedSum / totalCourseCredits;

    return Transcript.builder()
            .studentId(student.getId())
            .reference(student.getReference())
            .firstName(student.getFirstName())
            .lastName(student.getLastName())
            .level(level)
            .courses(lines)
            .generalAverage(generalAverage)
            .totalCredit(totalObtainedCredits)
            .build();
  }

  @Transactional(readOnly = true)
  public boolean isGroupYearCompleteForLevel(UUID groupId, Integer level) {
    JGroup group =
            groupRepository
                    .findById(groupId)
                    .orElseThrow(
                            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "group not found"));

    if (group.getSpeciality() == null || group.getSpeciality().getId() == null) {
      return false;
    }

    List<JCourse> courses =
            courseRepository.findBySpecialityIdAndLevel(group.getSpeciality().getId(), level);
    if (courses.isEmpty()) {
      return false;
    }

    int totalCredits = courses.stream().mapToInt(JCourse::getCredit).sum();
    if (totalCredits != YEAR_TOTAL_CREDITS) {
      return false;
    }

    for (JCourse course : courses) {
      double coeffSum =
              findExamsForGroupAndCourse(groupId, course.getId()).stream()
                      .mapToDouble(JExam::getCoeff)
                      .sum();
      if (Math.abs(coeffSum - COEFF_TOTAL) > COEFF_EPSILON) {
        return false;
      }
    }
    return true;
  }

  // -------------------- sécurité --------------------

  private void assertCanAccessTranscript(UUID studentId) {
    if (courseAccessService.isAdmin() || courseAccessService.isTeacher()) {
      return;
    }
    if (courseAccessService.isStudent()) {
      if (!courseAccessService.currentUserId().equals(studentId)) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "not your transcript");
      }
      return;
    }
    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "access denied");
  }

  // -------------------- helpers --------------------

  private List<JExam> findExamsForGroupAndCourse(UUID groupId, UUID courseId) {
    return examRepository.findByGroupId(groupId).stream()
            .filter(exam -> exam.getCourse() != null && courseId.equals(exam.getCourse().getId()))
            .toList();
  }

  private Map<UUID, List<JExam>> resolveAllExamsByCourseForStudentGroups(
          Set<UUID> studentGroupIds, List<JCourse> courses) {

    Map<UUID, Map<UUID, JExam>> byCourseThenExamId = new HashMap<>();

    for (UUID groupId : studentGroupIds) {
      for (JCourse course : courses) {
        for (JExam exam : findExamsForGroupAndCourse(groupId, course.getId())) {
          byCourseThenExamId
                  .computeIfAbsent(course.getId(), id -> new HashMap<>())
                  .putIfAbsent(exam.getId(), exam);
        }
      }
    }

    Map<UUID, List<JExam>> result = new HashMap<>();
    for (Map.Entry<UUID, Map<UUID, JExam>> entry : byCourseThenExamId.entrySet()) {
      result.put(entry.getKey(), new ArrayList<>(entry.getValue().values()));
    }
    return result;
  }

  private List<JCourse> resolveStudentCoursesForLevel(Set<UUID> specialityIds, Integer level) {
    Map<UUID, JCourse> byId = new HashMap<>();
    for (UUID specialityId : specialityIds) {
      for (JCourse course : courseRepository.findBySpecialityIdAndLevel(specialityId, level)) {
        byId.putIfAbsent(course.getId(), course);
      }
    }
    return new ArrayList<>(byId.values());
  }

  private Map<UUID, List<JGrade>> groupGradesByCourse(List<JGrade> grades) {
    Map<UUID, List<JGrade>> result = new HashMap<>();
    for (JGrade grade : grades) {
      if (grade.getExam() == null || grade.getExam().getCourse() == null) {
        continue;
      }
      result
              .computeIfAbsent(grade.getExam().getCourse().getId(), id -> new ArrayList<>())
              .add(grade);
    }
    return result;
  }

  private List<JGrade> filterGradesForStudentPath(JStudent student, List<JGrade> grades) {
    List<JGrade> valid = new ArrayList<>();
    for (JGrade grade : grades) {
      JExam exam = grade.getExam();
      if (exam == null || exam.getDate() == null) {
        continue;
      }
      JStudentGroupHistory atDate =
              studentGroupHistoryRepository
                      .findStudentGroupAtDate(student.getId(), exam.getDate())
                      .orElse(null);
      if (atDate == null || atDate.getGroup() == null) {
        continue;
      }
      if (examBelongsToGroup(exam, atDate.getGroup())) {
        valid.add(grade);
      }
    }
    return valid;
  }

  private boolean examBelongsToGroup(JExam exam, JGroup group) {
    return groupExamRepository
            .findByGroup_IdAndExam_Id(group.getId(), exam.getId())
            .isPresent();
  }

  /**
   * Moyenne matière :
   * numérateur = Σ (note × coeff) des examens faits
   * dénominateur = Σ coeffs de TOUS les examens de la matière
   */
  private double calculateCourseAverage(List<JGrade> studentGrades, List<JExam> allCourseExams) {
    double numerator = 0.0;
    for (JGrade grade : studentGrades) {
      if (grade.getExam() == null || grade.getExam().getCoeff() == null) {
        continue;
      }
      numerator += grade.getValue() * grade.getExam().getCoeff();
    }
    double denominator = 0.0;
    for (JExam exam : allCourseExams) {
      if (exam.getCoeff() != null) {
        denominator += exam.getCoeff();
      }
    }
    if (denominator == 0) {
      throw new ResponseStatusException(
              HttpStatus.BAD_REQUEST, "cannot calculate course average: no exam coefficients");
    }
    return numerator / denominator;
  }

  private void validateLevel(Integer level) {
    if (level == null || level < 1 || level > 3) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "level must be 1, 2 or 3");
    }
  }
}