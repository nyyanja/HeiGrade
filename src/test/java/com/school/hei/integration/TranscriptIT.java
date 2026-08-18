package com.school.hei.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.school.hei.conf.FacadeIT;
import com.school.hei.entity.JCourse;
import com.school.hei.entity.JExam;
import com.school.hei.entity.JGrade;
import com.school.hei.entity.JGroup;
import com.school.hei.entity.JGroupExam;
import com.school.hei.entity.JPromotion;
import com.school.hei.entity.JSpeciality;
import com.school.hei.entity.JSpecialityCourse;
import com.school.hei.entity.JStudent;
import com.school.hei.entity.JStudentGroupHistory;
import com.school.hei.entity.JUser;
import com.school.hei.enums.GroupSpeciality;
import com.school.hei.enums.Role;
import com.school.hei.model.LoginRequest;
import com.school.hei.model.LoginResponse;
import com.school.hei.model.Transcript;
import com.school.hei.repository.CourseRepository;
import com.school.hei.repository.ExamRepository;
import com.school.hei.repository.GradeRepository;
import com.school.hei.repository.GroupExamRepository;
import com.school.hei.repository.GroupRepository;
import com.school.hei.repository.PromotionRepository;
import com.school.hei.repository.SpecialityCourseRepository;
import com.school.hei.repository.SpecialityRepository;
import com.school.hei.repository.StudentGroupHistoryRepository;
import com.school.hei.repository.StudentRepository;
import com.school.hei.repository.TeacherCourseRepository;
import com.school.hei.repository.UserRepository;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class TranscriptIT extends FacadeIT {

  @LocalServerPort private int port;

  @Autowired private TestRestTemplate restTemplate;

  @Autowired private UserRepository userRepository;

  @Autowired private StudentRepository studentRepository;

  @Autowired private SpecialityRepository specialityRepository;

  @Autowired private PromotionRepository promotionRepository;

  @Autowired private GroupRepository groupRepository;

  @Autowired private CourseRepository courseRepository;

  @Autowired private ExamRepository examRepository;

  @Autowired private GradeRepository gradeRepository;

  @Autowired private GroupExamRepository groupExamRepository;

  @Autowired private StudentGroupHistoryRepository studentGroupHistoryRepository;

  @Autowired private SpecialityCourseRepository specialityCourseRepository;

  @Autowired private TeacherCourseRepository teacherCourseRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  private JStudent student;
  private JStudent otherStudent;
  private JGroup group;

  private String adminToken;
  private String studentToken;

  @BeforeEach
  void setUp() {
    gradeRepository.deleteAll();
    groupExamRepository.deleteAll();
    examRepository.deleteAll();
    teacherCourseRepository.deleteAll();
    specialityCourseRepository.deleteAll();
    studentGroupHistoryRepository.deleteAll();
    studentRepository.deleteAll();
    courseRepository.deleteAll();
    groupRepository.deleteAll();
    promotionRepository.deleteAll();
    specialityRepository.deleteAll();
    userRepository.deleteAll();

    JUser admin =
        userRepository.save(
            JUser.builder()
                .firstName("Admin")
                .lastName("Test")
                .email("admin@heigrade.com")
                .password(passwordEncoder.encode("Password123!"))
                .role(Role.ADMIN)
                .build());

    student =
        studentRepository.save(
            JStudent.builder()
                .firstName("John")
                .lastName("Student")
                .email("student@heigrade.com")
                .password(passwordEncoder.encode("Password123!"))
                .role(Role.STUDENT)
                .reference("STD001")
                .build());

    otherStudent =
        studentRepository.save(
            JStudent.builder()
                .firstName("Other")
                .lastName("Student")
                .email("other@heigrade.com")
                .password(passwordEncoder.encode("Password123!"))
                .role(Role.STUDENT)
                .reference("STD002")
                .build());

    JSpeciality speciality =
        specialityRepository.save(JSpeciality.builder().name(GroupSpeciality.EL.name()).build());

    JPromotion promotion =
        promotionRepository.save(JPromotion.builder().name("Promotion 2026").year(2026).build());

    group =
        groupRepository.save(
            JGroup.builder().name("EL-2026-A").speciality(speciality).promotion(promotion).build());

    student.setGroup(group);
    studentRepository.save(student);

    studentGroupHistoryRepository.save(
        JStudentGroupHistory.builder()
            .student(student)
            .group(group)
            .startDate(LocalDate.of(2025, 9, 1))
            .build());

    adminToken = login(admin.getEmail());
    studentToken = login(student.getEmail());
  }

  private String login(String email) {
    LoginRequest request = new LoginRequest(email, "Password123!");

    ResponseEntity<LoginResponse> response =
        restTemplate.postForEntity(
            "http://localhost:" + port + "/auth/login", request, LoginResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getToken()).isNotBlank();

    return response.getBody().getToken();
  }

  private JCourse createCourse(String reference, String title, int credit) {

    JCourse course =
        courseRepository.save(
            JCourse.builder().reference(reference).title(title).credit(credit).level(1).build());

    specialityCourseRepository.save(
        JSpecialityCourse.builder().course(course).speciality(group.getSpeciality()).build());

    return course;
  }

  private JExam createExam(JCourse course, String title, double coeff, LocalDate date) {

    JExam exam =
        examRepository.save(
            JExam.builder().course(course).title(title).coeff(coeff).date(date).build());

    groupExamRepository.save(JGroupExam.builder().group(group).exam(exam).build());

    return exam;
  }

  private void createGrade(JExam exam, double value) {
    gradeRepository.save(
        JGrade.builder().value(value).date(exam.getDate()).student(student).exam(exam).build());
  }

  private HttpHeaders headers(String token) {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);
    headers.setContentType(MediaType.APPLICATION_JSON);
    return headers;
  }

  private ResponseEntity<Transcript> getTranscript(String token, UUID studentId, int level) {

    HttpEntity<Void> request = new HttpEntity<>(headers(token));

    return restTemplate.exchange(
        "http://localhost:" + port + "/transcripts/student/" + studentId + "/L" + level,
        HttpMethod.GET,
        request,
        Transcript.class);
  }

  @Test
  void adminShouldGetStudentTranscript() {
    for (int i = 1; i <= 10; i++) {
      JCourse course = createCourse("L1-" + i, "Course " + i, 6);
      JExam exam = createExam(course, "Exam " + i, 1.0, LocalDate.of(2026, 6, 10));

      createGrade(exam, 12.0);
    }

    ResponseEntity<Transcript> response = getTranscript(adminToken, student.getId(), 1);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();

    Transcript transcript = response.getBody();

    assertThat(transcript.getStudentId()).isEqualTo(student.getId());
    assertThat(transcript.getReference()).isEqualTo("STD001");
    assertThat(transcript.getLevel()).isEqualTo(1);
    assertThat(transcript.getCourses()).hasSize(10);
    assertThat(transcript.getGeneralAverage()).isEqualTo(12.0);
    assertThat(transcript.getTotalCredit()).isEqualTo(60);
  }

  @Test
  void transcriptShouldCalculateWeightedAverage() {
    JCourse firstCourse = createCourse("WEIGHT-1", "Weighted Course 1", 6);

    JExam firstExam = createExam(firstCourse, "Exam 1", 0.4, LocalDate.of(2026, 6, 10));

    JExam secondExam = createExam(firstCourse, "Exam 2", 0.6, LocalDate.of(2026, 6, 15));

    createGrade(firstExam, 10);
    createGrade(secondExam, 15);

    JCourse secondCourse = createCourse("WEIGHT-2", "Weighted Course 2", 54);

    JExam thirdExam = createExam(secondCourse, "Exam", 1.0, LocalDate.of(2026, 6, 20));

    createGrade(thirdExam, 17);

    ResponseEntity<Transcript> response = getTranscript(adminToken, student.getId(), 1);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();

    Transcript transcript = response.getBody();

    assertThat(transcript.getCourses()).hasSize(2);
    assertThat(transcript.getCourses())
        .anySatisfy(
            course -> {
              assertThat(course.getReference()).isEqualTo("WEIGHT-1");
              assertThat(course.getAverage()).isEqualTo(13.0);
            });

    assertThat(transcript.getCourses())
        .anySatisfy(
            course -> {
              assertThat(course.getReference()).isEqualTo("WEIGHT-2");
              assertThat(course.getAverage()).isEqualTo(17.0);
            });

    double expectedAverage = (13.0 * 6 + 17.0 * 54) / 60;

    assertThat(transcript.getGeneralAverage()).isEqualTo(expectedAverage);

    assertThat(transcript.getTotalCredit()).isEqualTo(60);
  }

  @Test
  void studentShouldGetOwnTranscript() {
    for (int i = 1; i <= 10; i++) {
      JCourse course = createCourse("STUDENT-" + i, "Student Course " + i, 6);

      JExam exam = createExam(course, "Exam " + i, 1.0, LocalDate.of(2026, 6, 10));

      createGrade(exam, 14);
    }

    ResponseEntity<Transcript> response = getTranscript(studentToken, student.getId(), 1);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getStudentId()).isEqualTo(student.getId());
  }

  @Test
  void studentShouldNotGetAnotherStudentTranscript() {
    ResponseEntity<Transcript> response = getTranscript(studentToken, otherStudent.getId(), 1);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void studentWithoutGroupHistoryShouldReturnBadRequest() {
    studentGroupHistoryRepository.deleteAll();

    ResponseEntity<Transcript> response = getTranscript(adminToken, student.getId(), 1);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void groupWithLessThan60CreditsShouldNotCompleteYear() {
    for (int i = 1; i <= 9; i++) {
      JCourse course = createCourse("INCOMPLETE-" + i, "Incomplete Course " + i, 6);
      JExam exam = createExam(course, "Exam " + i, 1.0, LocalDate.of(2026, 6, 10));

      createGrade(exam, 15);
    }

    ResponseEntity<Transcript> response = getTranscript(adminToken, student.getId(), 1);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void courseWithoutStudentGradeShouldReturnBadRequest() {
    for (int i = 1; i <= 10; i++) {
      JCourse course = createCourse("NOGRADE-" + i, "No Grade Course " + i, 6);

      JExam exam = createExam(course, "Exam " + i, 1.0, LocalDate.of(2026, 6, 10));

      if (i < 10) {
        createGrade(exam, 14);
      }
    }

    ResponseEntity<Transcript> response = getTranscript(adminToken, student.getId(), 1);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void examCoefficientsNotEqualToOneShouldNotCompleteYear() {
    for (int i = 1; i <= 10; i++) {
      JCourse course = createCourse("COEFF-" + i, "Coefficient Course " + i, 6);

      double coefficient = i == 1 ? 0.8 : 1.0;

      JExam exam = createExam(course, "Exam " + i, coefficient, LocalDate.of(2026, 6, 10));

      createGrade(exam, 14);
    }

    ResponseEntity<Transcript> response = getTranscript(adminToken, student.getId(), 1);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void failedCourseShouldNotGiveCredits() {
    for (int i = 1; i <= 10; i++) {
      JCourse course = createCourse("FAIL-" + i, "Fail Course " + i, 6);
      JExam exam = createExam(course, "Exam " + i, 1.0, LocalDate.of(2026, 6, 10));

      createGrade(exam, i == 1 ? 9.0 : 14.0);
    }

    ResponseEntity<Transcript> response = getTranscript(adminToken, student.getId(), 1);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getTotalCredit()).isEqualTo(54);
  }
}
