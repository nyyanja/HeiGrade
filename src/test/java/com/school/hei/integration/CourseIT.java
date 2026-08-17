package com.school.hei.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.school.hei.conf.FacadeIT;
import com.school.hei.entity.JSpeciality;
import com.school.hei.entity.JTeacher;
import com.school.hei.entity.JUser;
import com.school.hei.enums.GroupSpeciality;
import com.school.hei.enums.Role;
import com.school.hei.model.Course;
import com.school.hei.model.LoginRequest;
import com.school.hei.model.LoginResponse;
import com.school.hei.model.Speciality;
import com.school.hei.model.Teacher;
import com.school.hei.repository.SpecialityRepository;
import com.school.hei.repository.TeacherRepository;
import com.school.hei.repository.UserRepository;
import com.school.hei.repository.GroupRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;

class CourseIT extends FacadeIT {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private SpecialityRepository specialityRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;

    private JTeacher teacher;
    private JSpeciality speciality;

    @BeforeEach
    void setUp() {

        groupRepository.deleteAll();
        specialityRepository.deleteAll();
        teacherRepository.deleteAll();
        userRepository.deleteAll();

        JUser admin =
                JUser.builder()
                        .id(UUID.randomUUID())
                        .firstName("Admin")
                        .lastName("Test")
                        .email("admin@heigrade.com")
                        .password(passwordEncoder.encode("Password123!"))
                        .role(Role.ADMIN)
                        .build();

        userRepository.save(admin);

        teacher =
                teacherRepository.save(
                        JTeacher.builder()
                                .firstName("Teacher")
                                .lastName("One")
                                .email("teacher@heigrade.com")
                                .password(passwordEncoder.encode("Password123!"))
                                .role(Role.TEACHER)
                                .speciality("JAVA")
                                .build());

        speciality =
                specialityRepository.save(
                        JSpeciality.builder()
                                .name(GroupSpeciality.EL.name())
                                .build());
        adminToken = login("admin@heigrade.com");
    }

    private String login(String email) {

        LoginRequest request =
                new LoginRequest(email, "Password123!");

        ResponseEntity<LoginResponse> response =
                restTemplate.postForEntity(
                        "http://localhost:" + port + "/auth/login",
                        request,
                        LoginResponse.class);

        return response.getBody().getToken();
    }

    @Test
    void adminShouldCreateCourse() {

        Course course =
                Course.builder()
                        .reference("PROG4")
                        .title("Programmation Avancée")
                        .credit(6)
                        .level(2)
                        .teachers(
                                List.of(
                                        Teacher.builder()
                                                .id(teacher.getId())
                                                .build()))
                        .specialities(
                                List.of(
                                        Speciality.builder()
                                                .id(speciality.getId())
                                                .build()))
                        .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);

        HttpEntity<Course> request =
                new HttpEntity<>(course, headers);

        ResponseEntity<Course> response =
                restTemplate.exchange(
                        "http://localhost:" + port + "/courses",
                        HttpMethod.POST,
                        request,
                        Course.class);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.CREATED);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getReference())
                .isEqualTo("PROG4");
    }
}