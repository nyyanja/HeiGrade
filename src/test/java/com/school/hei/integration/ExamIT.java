package com.school.hei.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.school.hei.conf.FacadeIT;
import com.school.hei.entity.JUser;
import com.school.hei.enums.Role;
import com.school.hei.model.LoginRequest;
import com.school.hei.model.LoginResponse;
import com.school.hei.repository.CourseRepository;
import com.school.hei.repository.ExamRepository;
import com.school.hei.repository.GradeHistoryRepository;
import com.school.hei.repository.GradeRepository;
import com.school.hei.repository.GroupExamRepository;
import com.school.hei.repository.GroupRepository;
import com.school.hei.repository.PromotionRepository;
import com.school.hei.repository.SpecialityCourseRepository;
import com.school.hei.repository.SpecialityRepository;
import com.school.hei.repository.StudentGroupHistoryRepository;
import com.school.hei.repository.StudentRepository;
import com.school.hei.repository.TeacherCourseRepository;
import com.school.hei.repository.TeacherRepository;
import com.school.hei.repository.UserRepository;
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

class ExamIT extends FacadeIT {

  @LocalServerPort private int port;

  @Autowired private TestRestTemplate restTemplate;

  @Autowired private PasswordEncoder passwordEncoder;

  @Autowired private UserRepository userRepository;

  @Autowired private StudentRepository studentRepository;

  @Autowired private TeacherRepository teacherRepository;

  @Autowired private GradeRepository gradeRepository;

  @Autowired private GradeHistoryRepository gradeHistoryRepository;

  @Autowired private StudentGroupHistoryRepository studentGroupHistoryRepository;

  @Autowired private GroupExamRepository groupExamRepository;

  @Autowired private ExamRepository examRepository;

  @Autowired private TeacherCourseRepository teacherCourseRepository;

  @Autowired private GroupRepository groupRepository;

  @Autowired private CourseRepository courseRepository;

  @Autowired private PromotionRepository promotionRepository;

  @Autowired private SpecialityRepository specialityRepository;

  @Autowired private SpecialityCourseRepository specialityCourseRepository;

  private final String adminEmail = "[exam.admin@heigrade.com](mailto:exam.admin@heigrade.com)";
  private final String studentEmail =
      "[exam.student@heigrade.com](mailto:exam.student@heigrade.com)";
  private final String teacherEmail =
      "[exam.teacher@heigrade.com](mailto:exam.teacher@heigrade.com)";

  private final String password = "Password123!";

  @BeforeEach
  void setUp() {

    gradeHistoryRepository.deleteAll();
    gradeRepository.deleteAll();
    groupExamRepository.deleteAll();
    examRepository.deleteAll();
    teacherCourseRepository.deleteAll();

    studentGroupHistoryRepository.deleteAll();
    studentRepository.deleteAll();

    groupRepository.deleteAll();
    specialityCourseRepository.deleteAll();
    courseRepository.deleteAll();
    teacherRepository.deleteAll();

    promotionRepository.deleteAll();
    specialityRepository.deleteAll();

    userRepository.deleteAll();

    createUser(adminEmail, Role.ADMIN);
    createUser(studentEmail, Role.STUDENT);
    createUser(teacherEmail, Role.TEACHER);
  }

  private void createUser(String email, Role role) {
    JUser user =
        JUser.builder()
            .id(UUID.randomUUID())
            .firstName("Integration")
            .lastName(role.name())
            .email(email)
            .password(passwordEncoder.encode(password))
            .role(role)
            .build();

    userRepository.save(user);
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

  @Test
  void shouldRejectUnauthenticatedRequest() {
    ResponseEntity<String> response =
        restTemplate.getForEntity("http://localhost:" + port + "/exams", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void shouldAllowAdminToGetExams() {
    String token = login(adminEmail);
    HttpEntity<Void> request = new HttpEntity<>(authorizationHeaders(token));

    ResponseEntity<String> response =
        restTemplate.exchange(
            "http://localhost:" + port + "/exams", HttpMethod.GET, request, String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void shouldAllowStudentToGetExams() {
    String token = login(studentEmail);

    HttpEntity<Void> request = new HttpEntity<>(authorizationHeaders(token));

    ResponseEntity<String> response =
        restTemplate.exchange(
            "http://localhost:" + port + "/exams", HttpMethod.GET, request, String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void shouldAllowTeacherToGetExams() {
    String token = login(teacherEmail);

    HttpEntity<Void> request = new HttpEntity<>(authorizationHeaders(token));

    ResponseEntity<String> response =
        restTemplate.exchange(
            "http://localhost:" + port + "/exams", HttpMethod.GET, request, String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }
}
