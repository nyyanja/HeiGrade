package com.school.hei.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.school.hei.conf.FacadeIT;
import com.school.hei.entity.*;
import com.school.hei.enums.GroupSpeciality;
import com.school.hei.enums.Role;
import com.school.hei.model.Grade;
import com.school.hei.model.LoginRequest;
import com.school.hei.model.LoginResponse;
import com.school.hei.repository.*;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

class GradeIT extends FacadeIT {

  @LocalServerPort private int port;

  @Autowired private TestRestTemplate restTemplate;

  @Autowired private PasswordEncoder passwordEncoder;

  @Autowired private UserRepository userRepository;

  @Autowired private StudentRepository studentRepository;

  @Autowired private TeacherRepository teacherRepository;

  @Autowired private CourseRepository courseRepository;

  @Autowired private ExamRepository examRepository;

  @Autowired private GradeRepository gradeRepository;

  @Autowired private GradeHistoryRepository gradeHistoryRepository;

  @Autowired private GroupRepository groupRepository;

  @Autowired private GroupExamRepository groupExamRepository;

  @Autowired private SpecialityRepository specialityRepository;

  @Autowired private TeacherCourseRepository teacherCourseRepository;

  @Autowired private PromotionRepository promotionRepository;

  private final String studentEmail = "grade.student@heigrade.com";
  private final String otherStudentEmail = "grade.other.student@heigrade.com";
  private final String teacherEmail = "grade.teacher@heigrade.com";
  private final String otherTeacherEmail = "grade.other.teacher@heigrade.com";

  private final String password = "Password123!";

  private JStudent student;
  private JStudent otherStudent;
  private JTeacher teacher;
  private JTeacher otherTeacher;
  private JCourse course;
  private JCourse otherCourse;
  private JExam exam;
  private JGroup group;

  @BeforeEach
  void setUp() {
    gradeHistoryRepository.deleteAll();
    gradeRepository.deleteAll();
    groupExamRepository.deleteAll();
    examRepository.deleteAll();
    teacherCourseRepository.deleteAll();
    studentRepository.deleteAll();
    groupRepository.deleteAll();
    courseRepository.deleteAll();
    teacherRepository.deleteAll();
    specialityRepository.deleteAll();
    userRepository.deleteAll();

    JSpeciality speciality =
        specialityRepository.save(
            JSpeciality.builder().id(UUID.randomUUID()).name(GroupSpeciality.EL.name()).build());

    JPromotion promotion =
        promotionRepository.save(
            JPromotion.builder().id(UUID.randomUUID()).name("Promotion 2026").year(2026).build());

    group =
        groupRepository.save(
            JGroup.builder()
                .id(UUID.randomUUID())
                .name("G1")
                .promotion(promotion)
                .speciality(speciality)
                .build());

    student =
        studentRepository.save(
            JStudent.builder()
                .id(UUID.randomUUID())
                .firstName("Grade")
                .lastName("Student")
                .reference("STU-GRADE-001")
                .email(studentEmail)
                .password(passwordEncoder.encode(password))
                .role(Role.STUDENT)
                .group(group)
                .build());

    otherStudent =
        studentRepository.save(
            JStudent.builder()
                .id(UUID.randomUUID())
                .firstName("Other")
                .lastName("Student")
                .reference("STU-GRADE-002")
                .email(otherStudentEmail)
                .password(passwordEncoder.encode(password))
                .role(Role.STUDENT)
                .group(group)
                .build());

    teacher =
        teacherRepository.save(
            JTeacher.builder()
                .id(UUID.randomUUID())
                .firstName("Grade")
                .lastName("Teacher")
                .speciality("Computer Science")
                .email(teacherEmail)
                .password(passwordEncoder.encode(password))
                .role(Role.TEACHER)
                .build());

    otherTeacher =
        teacherRepository.save(
            JTeacher.builder()
                .id(UUID.randomUUID())
                .firstName("Other")
                .lastName("Teacher")
                .speciality("Mathematics")
                .email(otherTeacherEmail)
                .password(passwordEncoder.encode(password))
                .role(Role.TEACHER)
                .build());

    course =
        courseRepository.save(
            JCourse.builder()
                .id(UUID.randomUUID())
                .reference("JAVA-001")
                .title("Java Programming")
                .credit(6)
                .level(1)
                .build());

    otherCourse =
        courseRepository.save(
            JCourse.builder()
                .id(UUID.randomUUID())
                .reference("MATH-001")
                .title("Mathematics")
                .credit(6)
                .level(1)
                .build());

    exam =
        examRepository.save(
            JExam.builder()
                .id(UUID.randomUUID())
                .date(LocalDate.now())
                .coeff(1.0)
                .title("Java Final Exam")
                .course(course)
                .build());

    teacherCourseRepository.save(JTeacherCourse.builder().teacher(teacher).course(course).build());

    groupExamRepository.save(JGroupExam.builder().group(group).exam(exam).build());
  }

  private String login(String email) {
    LoginRequest request = new LoginRequest(email, password);

    ResponseEntity<LoginResponse> response =
        restTemplate.postForEntity(
            "http://localhost:" + port + "/auth/login", request, LoginResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getToken()).isNotBlank();

    return response.getBody().getToken();
  }

  private HttpHeaders authorizationHeaders(String token) {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);
    return headers;
  }

  private HttpEntity<Void> authorizedRequest(String token) {
    return new HttpEntity<>(authorizationHeaders(token));
  }

  private HttpEntity<Grade> authorizedGradeRequest(String token, Grade grade) {
    return new HttpEntity<>(grade, authorizationHeaders(token));
  }

  @Test
  void studentShouldSeeOwnGrade() {
    JGrade savedGrade =
        gradeRepository.save(
            JGrade.builder().value(15.0).date(LocalDate.now()).student(student).exam(exam).build());

    String token = login(studentEmail);

    ResponseEntity<String> response =
        restTemplate.exchange(
            "http://localhost:" + port + "/grades/" + savedGrade.getId(),
            HttpMethod.GET,
            authorizedRequest(token),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void studentShouldNotSeeAnotherStudentGrade() {
    JGrade savedGrade =
        gradeRepository.save(
            JGrade.builder()
                .value(15.0)
                .date(LocalDate.now())
                .student(otherStudent)
                .exam(exam)
                .build());

    String token = login(studentEmail);

    ResponseEntity<String> response =
        restTemplate.exchange(
            "http://localhost:" + port + "/grades/" + savedGrade.getId(),
            HttpMethod.GET,
            authorizedRequest(token),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void teacherShouldCreateGradeForTaughtCourse() {
    String token = login(teacherEmail);

    Grade grade =
        Grade.builder()
            .value(16.0)
            .date(LocalDate.now())
            .student(com.school.hei.model.Student.builder().id(student.getId()).build())
            .exam(com.school.hei.model.Exam.builder().id(exam.getId()).build())
            .build();

    ResponseEntity<String> response =
        restTemplate.postForEntity(
            "http://localhost:" + port + "/grades",
            authorizedGradeRequest(token, grade),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(gradeRepository.findByStudent_IdAndExam_Id(student.getId(), exam.getId()))
        .isPresent();
  }

  @Test
  void otherTeacherShouldNotCreateGradeForCourseHeDoesNotTeach() {
    String token = login(otherTeacherEmail);

    Grade grade =
        Grade.builder()
            .value(16.0)
            .date(LocalDate.now())
            .student(com.school.hei.model.Student.builder().id(student.getId()).build())
            .exam(com.school.hei.model.Exam.builder().id(exam.getId()).build())
            .build();

    ResponseEntity<String> response =
        restTemplate.postForEntity(
            "http://localhost:" + port + "/grades",
            authorizedGradeRequest(token, grade),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void updatingGradeShouldCreateHistory() {
    JGrade savedGrade =
        gradeRepository.save(
            JGrade.builder().value(12.0).date(LocalDate.now()).student(student).exam(exam).build());

    String token = login(teacherEmail);

    Grade updatedGrade =
        Grade.builder()
            .id(savedGrade.getId())
            .value(17.0)
            .date(LocalDate.now())
            .student(com.school.hei.model.Student.builder().id(student.getId()).build())
            .exam(com.school.hei.model.Exam.builder().id(exam.getId()).build())
            .build();

    HttpHeaders headers = authorizationHeaders(token);
    HttpEntity<Grade> request = new HttpEntity<>(updatedGrade, headers);

    ResponseEntity<String> response =
        restTemplate.exchange(
            "http://localhost:"
                + port
                + "/grades/"
                + savedGrade.getId()
                + "?reason=Correction of the exam&modifiedById="
                + teacher.getId(),
            HttpMethod.PUT,
            request,
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    assertThat(gradeRepository.findById(savedGrade.getId()))
        .isPresent()
        .get()
        .extracting(JGrade::getValue)
        .isEqualTo(17.0);

    assertThat(gradeHistoryRepository.findAll())
        .hasSize(1)
        .first()
        .satisfies(
            history -> {
              assertThat(history.getOldValue()).isEqualTo(12.0);
              assertThat(history.getNewValue()).isEqualTo(17.0);
              assertThat(history.getReason()).isEqualTo("Correction of the exam");
            });
  }
}


