package com.school.hei.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.school.hei.file.bucket.BucketComponent;
import com.school.hei.model.LoginRequest;
import com.school.hei.model.LoginResponse;
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
import com.school.hei.repository.UserRepository;
import java.io.File;
import java.io.FileInputStream;
import java.time.Duration;
import java.time.LocalDate;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

class GraduateIT extends FacadeIT {

  @LocalServerPort private int port;

  @Autowired private TestRestTemplate restTemplate;

  @Autowired private UserRepository userRepository;

  @Autowired private StudentRepository studentRepository;

  @Autowired private SpecialityRepository specialityRepository;

  @Autowired private SpecialityCourseRepository specialityCourseRepository;

  @Autowired private PromotionRepository promotionRepository;

  @Autowired private GroupRepository groupRepository;

  @Autowired private CourseRepository courseRepository;

  @Autowired private ExamRepository examRepository;

  @Autowired private GradeRepository gradeRepository;

  @Autowired private GroupExamRepository groupExamRepository;

  @Autowired private StudentGroupHistoryRepository studentGroupHistoryRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  @MockBean private BucketComponent bucketComponent;

  private JStudent student;
  private JPromotion promotion;
  private JSpeciality speciality;

  private String adminToken;
  private String studentToken;

  @BeforeEach
  void setUp() {
    gradeRepository.deleteAll();
    groupExamRepository.deleteAll();
    examRepository.deleteAll();
    studentGroupHistoryRepository.deleteAll();
    specialityCourseRepository.deleteAll();
    studentRepository.deleteAll();
    courseRepository.deleteAll();
    groupRepository.deleteAll();
    promotionRepository.deleteAll();
    specialityRepository.deleteAll();
    userRepository.deleteAll();

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
                .lastName("Graduate")
                .email("student@heigrade.com")
                .password(passwordEncoder.encode("Password123!"))
                .role(Role.STUDENT)
                .reference("STD001")
                .build());

    speciality =
        specialityRepository.save(JSpeciality.builder().name(GroupSpeciality.EL.name()).build());

    promotion =
        promotionRepository.save(JPromotion.builder().name("Promotion 2026").year(2026).build());

    JGroup group =
        groupRepository.save(
            JGroup.builder().name("EL-2026-A").speciality(speciality).promotion(promotion).build());

    student.setGroup(group);
    studentRepository.save(student);

    studentGroupHistoryRepository.save(
        JStudentGroupHistory.builder()
            .student(student)
            .group(group)
            .startDate(LocalDate.of(2023, 1, 1))
            .build());

    adminToken = login("admin@heigrade.com");
    studentToken = login("student@heigrade.com");
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

  private HttpHeaders headers(String token) {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);
    headers.setContentType(MediaType.APPLICATION_JSON);
    return headers;
  }

  private void createYearCourses(int level) {
    for (int i = 1; i <= 10; i++) {

      JCourse course =
          courseRepository.save(
              JCourse.builder()
                  .reference("L" + level + "-" + i)
                  .title("Course " + level + "-" + i)
                  .credit(6)
                  .level(level)
                  .build());

      specialityCourseRepository.save(
          JSpecialityCourse.builder().speciality(speciality).course(course).build());

      JExam exam =
          examRepository.save(
              JExam.builder()
                  .course(course)
                  .title("Exam " + level + "-" + i)
                  .coeff(1.0)
                  .date(LocalDate.of(2023 + level, 6, 10))
                  .build());

      groupExamRepository.save(JGroupExam.builder().group(student.getGroup()).exam(exam).build());

      gradeRepository.save(
          JGrade.builder().value(14.0).date(exam.getDate()).student(student).exam(exam).build());
    }
  }

  @Test
  void adminShouldExportGraduatesToExcel() throws Exception {

    createYearCourses(1);
    createYearCourses(2);
    createYearCourses(3);

    String expectedUrl = "https://example.com/graduates/" + promotion.getId() + ".xlsx";

    when(bucketComponent.presign(anyString(), eq(Duration.ofMinutes(15))))
        .thenReturn(java.net.URI.create(expectedUrl).toURL());

    doAnswer(
            invocation -> {
              File file = invocation.getArgument(0);

              assertThat(file).exists();
              assertThat(file.length()).isGreaterThan(0);

              try (XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(file))) {

                assertThat(workbook.getNumberOfSheets()).isEqualTo(1);

                var sheet = workbook.getSheetAt(0);

                assertThat(sheet.getSheetName()).isEqualTo("Diplomes");

                var header = sheet.getRow(0);

                assertThat((Object) header).isNotNull();

                assertThat(header.getCell(0).getStringCellValue()).isEqualTo("Rang");

                assertThat(header.getCell(1).getStringCellValue()).isEqualTo("Reference");

                assertThat(header.getCell(2).getStringCellValue()).isEqualTo("Prenom");

                assertThat(header.getCell(3).getStringCellValue()).isEqualTo("Nom");

                assertThat(header.getCell(4).getStringCellValue()).isEqualTo("Groupe");

                assertThat(header.getCell(5).getStringCellValue()).isEqualTo("Moyenne generale");

                assertThat(header.getCell(6).getStringCellValue()).isEqualTo("Credits totaux");

                var row = sheet.getRow(1);

                assertThat((Object) row)
                    .as("The exported Excel must contain at least one graduate row")
                    .isNotNull();

                assertThat(row.getCell(0).getNumericCellValue()).isEqualTo(1);

                assertThat(row.getCell(1).getStringCellValue()).isEqualTo("STD001");

                assertThat(row.getCell(2).getStringCellValue()).isEqualTo("John");

                assertThat(row.getCell(3).getStringCellValue()).isEqualTo("Graduate");

                assertThat(row.getCell(4).getStringCellValue()).isEqualTo("EL-2026-A");

                assertThat(row.getCell(5).getNumericCellValue()).isEqualTo(14.0);

                assertThat(row.getCell(6).getNumericCellValue()).isEqualTo(180);
              }

              return null;
            })
        .when(bucketComponent)
        .upload(any(File.class), anyString());

    HttpEntity<Void> request = new HttpEntity<>(headers(adminToken));

    ResponseEntity<String> response =
        restTemplate.exchange(
            "http://localhost:" + port + "/graduates/promotion/" + promotion.getId() + "/export",
            HttpMethod.GET,
            request,
            String.class);

    assertThat(response.getStatusCode())
        .as("Export response: status=%s, body=%s", response.getStatusCode(), response.getBody())
        .isEqualTo(HttpStatus.OK);

    assertThat(response.getBody()).isEqualTo(expectedUrl);

    verify(bucketComponent).upload(any(File.class), anyString());

    verify(bucketComponent).presign(anyString(), eq(Duration.ofMinutes(15)));
  }

  @Test
  void studentShouldNotExportGraduatesToExcel() {

    HttpEntity<Void> request = new HttpEntity<>(headers(studentToken));

    ResponseEntity<String> response =
        restTemplate.exchange(
            "http://localhost:" + port + "/graduates/promotion/" + promotion.getId() + "/export",
            HttpMethod.GET,
            request,
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void adminShouldExportEmptyPromotion() throws Exception {

    String expectedUrl = "https://example.com/empty-" + promotion.getId() + ".xlsx";

    when(bucketComponent.presign(anyString(), eq(Duration.ofMinutes(15))))
        .thenReturn(java.net.URI.create(expectedUrl).toURL());

    doAnswer(
            invocation -> {
              File file = invocation.getArgument(0);

              assertThat(file).exists();
              assertThat(file.length()).isGreaterThan(0);

              try (XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(file))) {

                var sheet = workbook.getSheet("Diplomes");

                assertThat((Object) sheet).isNotNull();

                assertThat(sheet.getLastRowNum()).isEqualTo(0);

                var header = sheet.getRow(0);

                assertThat((Object) header).isNotNull();

                assertThat(header.getCell(0).getStringCellValue()).isEqualTo("Rang");

                assertThat(header.getCell(1).getStringCellValue()).isEqualTo("Reference");

                assertThat(header.getCell(2).getStringCellValue()).isEqualTo("Prenom");

                assertThat(header.getCell(3).getStringCellValue()).isEqualTo("Nom");

                assertThat(header.getCell(4).getStringCellValue()).isEqualTo("Groupe");

                assertThat(header.getCell(5).getStringCellValue()).isEqualTo("Moyenne generale");

                assertThat(header.getCell(6).getStringCellValue()).isEqualTo("Credits totaux");
              }

              return null;
            })
        .when(bucketComponent)
        .upload(any(File.class), anyString());

    HttpEntity<Void> request = new HttpEntity<>(headers(adminToken));

    ResponseEntity<String> response =
        restTemplate.exchange(
            "http://localhost:" + port + "/graduates/promotion/" + promotion.getId() + "/export",
            HttpMethod.GET,
            request,
            String.class);

    assertThat(response.getStatusCode())
        .as("Export response: status=%s, body=%s", response.getStatusCode(), response.getBody())
        .isEqualTo(HttpStatus.OK);

    assertThat(response.getBody()).isEqualTo(expectedUrl);

    verify(bucketComponent).upload(any(File.class), anyString());

    verify(bucketComponent).presign(anyString(), eq(Duration.ofMinutes(15)));
  }
}
