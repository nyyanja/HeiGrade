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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

class AuthIT extends FacadeIT {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final String email = "test.auth@heigrade.com";
    private final String password = "Password123!";

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        JUser user =
                JUser.builder()
                        .id(UUID.randomUUID())
                        .firstName("Integration")
                        .lastName("Test")
                        .email(email)
                        .password(passwordEncoder.encode(password))
                        .role(Role.STUDENT)
                        .build();

        userRepository.save(user);
    }

    @Test
    void shouldLoginAndReturnJwt() {
        LoginRequest request = new LoginRequest(email, password);

        ResponseEntity<LoginResponse> response =
                restTemplate.postForEntity(
                        "http://localhost:" + port + "/auth/login",
                        request,
                        LoginResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getToken()).isNotBlank();
    }
}