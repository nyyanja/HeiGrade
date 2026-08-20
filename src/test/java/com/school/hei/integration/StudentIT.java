package com.school.hei.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.school.hei.conf.FacadeIT;
import com.school.hei.entity.JUser;
import com.school.hei.enums.Role;
import com.school.hei.model.LoginRequest;
import com.school.hei.model.LoginResponse;
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

class StudentIT extends FacadeIT {

  @LocalServerPort private int port;

  @Autowired private TestRestTemplate restTemplate;

  @Autowired private UserRepository userRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  private String adminEmail;
  private String studentEmail;

  private final String password = "Password123!";

  @BeforeEach
  void setUp() {
    adminEmail = "admin-" + UUID.randomUUID() + "@heigrade.com";

    studentEmail = "student-" + UUID.randomUUID() + "@heigrade.com";

    createUser(adminEmail, Role.ADMIN);
    createUser(studentEmail, Role.STUDENT);
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
        restTemplate.getForEntity("http://localhost:" + port + "/students", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void shouldAllowAdminToGetStudents() {
    String token = login(adminEmail);

    HttpEntity<Void> request = new HttpEntity<>(authorizationHeaders(token));

    ResponseEntity<String> response =
        restTemplate.exchange(
            "http://localhost:" + port + "/students", HttpMethod.GET, request, String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void shouldRejectStudentFromListingAllStudents() {
    String token = login(studentEmail);

    HttpEntity<Void> request = new HttpEntity<>(authorizationHeaders(token));

    ResponseEntity<String> response =
        restTemplate.exchange(
            "http://localhost:" + port + "/students", HttpMethod.GET, request, String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }
}

