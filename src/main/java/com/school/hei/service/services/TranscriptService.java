package com.school.hei.service.services;

import com.school.hei.entity.JCourse;
import com.school.hei.entity.JGroup;
import com.school.hei.entity.JStudent;
import com.school.hei.model.Transcript;
import com.school.hei.model.TranscriptCourseLine;
import com.school.hei.repository.CourseRepository;
import com.school.hei.repository.GroupRepository;
import com.school.hei.repository.StudentRepository;
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
public class TranscriptService {

  private static final double MINIMUM_VALIDATION_AVERAGE = 10.0;

  private final StudentRepository studentRepository;
  private final GroupRepository groupRepository;
  private final CourseRepository courseRepository;
  private final GradeService gradeService;

  @Transactional(readOnly = true)
  public Transcript getStudentTranscript(UUID studentId) {

    JStudent student = requireStudent(studentId);

    if (student.getGroup() == null || student.getGroup().getId() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "student has no group");
    }

    JGroup group = student.getGroup();
    if (!gradeService.isGroupYearComplete(group.getId())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "group year is not complete");
    }

    if (group.getSpeciality() == null || group.getSpeciality().getId() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "group has no speciality");
    }

    List<JCourse> courses = courseRepository.findBySpecialityId(group.getSpeciality().getId());

    if (courses.isEmpty()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "no course found for the student's speciality");
    }

    List<TranscriptCourseLine> transcriptCourses = new ArrayList<>();

    double weightedSum = 0.0;
    int totalCredits = 0;

    for (JCourse course : courses) {

      Double average = gradeService.computeStudentCourseAverage(student.getId(), course.getId());
      if (average == null) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "student does not have grades for all courses");
      }
      int obtainedCredit = average >= MINIMUM_VALIDATION_AVERAGE ? course.getCredit() : 0;

      transcriptCourses.add(
          TranscriptCourseLine.builder()
              .courseId(course.getId())
              .reference(course.getReference())
              .title(course.getTitle())
              .credit(course.getCredit())
              .average(average)
              .obtainedCredit(obtainedCredit)
              .build());

      weightedSum += average * course.getCredit();
      totalCredits += course.getCredit();
    }

    Double generalAverage = totalCredits == 0 ? null : weightedSum / totalCredits;

    int totalObtainedCredit =
        transcriptCourses.stream().mapToInt(TranscriptCourseLine::getObtainedCredit).sum();

    return Transcript.builder()
        .studentId(student.getId())
        .reference(student.getReference())
        .firstName(student.getFirstName())
        .lastName(student.getLastName())
        .groupId(group.getId())
        .groupName(group.getName())
        .courses(transcriptCourses)
        .generalAverage(generalAverage)
        .totalCredit(totalObtainedCredit)
        .build();
  }

  @Transactional(readOnly = true)
  public List<Transcript> getGroupTranscripts(UUID groupId) {

    requireGroup(groupId);

    if (!gradeService.isGroupYearComplete(groupId)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "group year is not complete");
    }

    List<JStudent> students = studentRepository.findByGroup_Id(groupId);

    List<Transcript> transcripts = new ArrayList<>();

    for (JStudent student : students) {

      if (hasGradesForAllCourses(student)) {
        transcripts.add(getStudentTranscript(student.getId()));
      }
    }

    return transcripts;
  }

  @Transactional(readOnly = true)
  public List<Transcript> getAllCompletedTranscripts() {

    List<JStudent> students = studentRepository.findAll();

    List<Transcript> transcripts = new ArrayList<>();

    for (JStudent student : students) {

      if (student.getGroup() == null || student.getGroup().getId() == null) {
        continue;
      }

      UUID groupId = student.getGroup().getId();

      if (!gradeService.isGroupYearComplete(groupId)) {
        continue;
      }

      if (hasGradesForAllCourses(student)) {
        transcripts.add(getStudentTranscript(student.getId()));
      }
    }

    return transcripts;
  }

  private boolean hasGradesForAllCourses(JStudent student) {

    if (student.getGroup() == null
        || student.getGroup().getSpeciality() == null
        || student.getGroup().getSpeciality().getId() == null) {
      return false;
    }

    UUID specialityId = student.getGroup().getSpeciality().getId();

    List<JCourse> courses = courseRepository.findBySpecialityId(specialityId);

    if (courses.isEmpty()) {
      return false;
    }

    for (JCourse course : courses) {

      Double average = gradeService.computeStudentCourseAverage(student.getId(), course.getId());

      if (average == null) {
        return false;
      }
    }

    return true;
  }

  private JStudent requireStudent(UUID studentId) {

    if (studentId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "student id is required");
    }

    return studentRepository
        .findById(studentId)
        .orElseThrow(
            () ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "student not found with id " + studentId));
  }

  private JGroup requireGroup(UUID groupId) {

    if (groupId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "group id is required");
    }

    return groupRepository
        .findById(groupId)
        .orElseThrow(
            () ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "group not found with id " + groupId));
  }
}
