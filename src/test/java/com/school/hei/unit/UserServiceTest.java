package com.school.hei.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.school.hei.entity.JStudent;
import com.school.hei.entity.JUser;
import com.school.hei.enums.Role;
import com.school.hei.enums.Sex;
import com.school.hei.model.CreateUserRequest;
import com.school.hei.model.User;
import com.school.hei.repository.GroupRepository;
import com.school.hei.repository.StudentRepository;
import com.school.hei.repository.UserRepository;
import com.school.hei.service.services.UserService;
import com.school.hei.validator.UserValidator;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock private UserRepository userRepository;

  @Mock private UserValidator userValidator;

  @Mock private StudentRepository studentRepository;

  @Mock private GroupRepository groupRepository;

  @Mock private PasswordEncoder passwordEncoder;

  @InjectMocks private UserService userService;

  private UUID userId;
  private UUID groupId;
  private JUser userEntity;
  private User user;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    groupId = UUID.randomUUID();

    userEntity =
        JUser.builder()
            .id(userId)
            .firstName("John")
            .lastName("Doe")
            .birthday(LocalDate.of(2000, 1, 15))
            .sex(Sex.MALE)
            .address("Antananarivo")
            .email("john.doe@test.com")
            .role(Role.STUDENT)
            .build();

    user =
        User.builder()
            .id(userId)
            .firstName("John")
            .lastName("Doe")
            .birthday(LocalDate.of(2000, 1, 15))
            .sex(Sex.MALE)
            .address("Antananarivo")
            .email("john.doe@test.com")
            .role(Role.STUDENT)
            .build();
  }

  @Test
  void should_find_all_users() {
    when(userRepository.findAll()).thenReturn(List.of(userEntity));

    List<User> result = userService.findAll();

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getId()).isEqualTo(userId);
    assertThat(result.get(0).getFirstName()).isEqualTo("John");
    assertThat(result.get(0).getLastName()).isEqualTo("Doe");
    assertThat(result.get(0).getEmail()).isEqualTo("john.doe@test.com");
    assertThat(result.get(0).getRole()).isEqualTo(Role.STUDENT);

    verify(userRepository).findAll();
  }

  @Test
  void should_return_empty_list_when_no_users_exist() {
    when(userRepository.findAll()).thenReturn(List.of());

    List<User> result = userService.findAll();

    assertThat(result).isEmpty();

    verify(userRepository).findAll();
  }

  @Test
  void should_find_user_by_id() {
    when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));

    User result = userService.findById(userId);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(userId);
    assertThat(result.getFirstName()).isEqualTo("John");
    assertThat(result.getLastName()).isEqualTo("Doe");
    assertThat(result.getEmail()).isEqualTo("john.doe@test.com");

    verify(userRepository).findById(userId);
  }

  @Test
  void should_throw_when_user_not_found_by_id() {
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.findById(userId))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("user not found with id");

    verify(userRepository).findById(userId);
  }

  @Test
  void should_save_user() {
    CreateUserRequest request = new CreateUserRequest();
    request.setFirstName("John");
    request.setLastName("Doe");
    request.setEmail("john.doe@test.com");
    request.setPassword("plain-password");
    request.setRole(Role.STUDENT);

    when(passwordEncoder.encode("plain-password")).thenReturn("encoded-password");
    when(userRepository.save(any(JUser.class))).thenReturn(userEntity);

    User result = userService.save(request);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(userId);
    assertThat(result.getFirstName()).isEqualTo("John");
    assertThat(result.getEmail()).isEqualTo("john.doe@test.com");
    assertThat(result.getRole()).isEqualTo(Role.STUDENT);

    verify(userValidator).validateCommonFields(any(User.class));
    verify(passwordEncoder).encode("plain-password");
    verify(userRepository).save(any(JUser.class));
  }

  @Test
  void should_not_save_user_when_validation_fails() {
    CreateUserRequest request = new CreateUserRequest();
    request.setFirstName("");
    request.setLastName("Doe");
    request.setEmail("john.doe@test.com");
    request.setPassword("plain-password");
    request.setRole(Role.STUDENT);

    doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "first name is required"))
        .when(userValidator)
        .validateCommonFields(any(User.class));

    assertThatThrownBy(() -> userService.save(request))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("first name is required");

    verify(userValidator).validateCommonFields(any(User.class));
    verify(userRepository, never()).save(any(JUser.class));
    verifyNoInteractions(passwordEncoder);
  }

  @Test
  void should_update_user() {
    when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
    when(userRepository.save(any(JUser.class))).thenReturn(userEntity);

    User result = userService.update(userId, user);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(userId);
    assertThat(user.getId()).isEqualTo(userId);
    assertThat(result.getFirstName()).isEqualTo("John");

    verify(userRepository).findById(userId);
    verify(userValidator).validateCommonFields(user);
    verify(userRepository).save(any(JUser.class));
  }

  @Test
  void should_not_update_unknown_user() {
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.update(userId, user))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("user not found with id");

    verify(userRepository).findById(userId);
    verify(userValidator, never()).validateCommonFields(any(User.class));
    verify(userRepository, never()).save(any(JUser.class));
  }

  @Test
  void should_not_update_user_when_validation_fails() {
    when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));

    doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "email format is invalid"))
        .when(userValidator)
        .validateCommonFields(user);

    assertThatThrownBy(() -> userService.update(userId, user))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("email format is invalid");

    verify(userRepository).findById(userId);
    verify(userValidator).validateCommonFields(user);
    verify(userRepository, never()).save(any(JUser.class));
  }

  @Test
  void should_delete_user() {
    when(userRepository.existsById(userId)).thenReturn(true);

    userService.delete(userId);

    verify(userRepository).existsById(userId);
    verify(userRepository).deleteById(userId);
  }

  @Test
  void should_not_delete_unknown_user() {
    when(userRepository.existsById(userId)).thenReturn(false);

    assertThatThrownBy(() -> userService.delete(userId))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("user not found with id");

    verify(userRepository).existsById(userId);
    verify(userRepository, never()).deleteById(any(UUID.class));
  }

  @Test
  void should_find_users_by_role() {
    when(userRepository.findByRole(Role.STUDENT)).thenReturn(List.of(userEntity));

    List<User> result = userService.findByRole(Role.STUDENT);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getId()).isEqualTo(userId);
    assertThat(result.get(0).getRole()).isEqualTo(Role.STUDENT);

    verify(userRepository).findByRole(Role.STUDENT);
  }

  @Test
  void should_return_empty_list_when_no_users_match_role() {
    when(userRepository.findByRole(Role.TEACHER)).thenReturn(List.of());

    List<User> result = userService.findByRole(Role.TEACHER);

    assertThat(result).isEmpty();

    verify(userRepository).findByRole(Role.TEACHER);
  }

  @Test
  void should_reject_null_role() {
    assertThatThrownBy(() -> userService.findByRole(null))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("role is required");

    verifyNoInteractions(userRepository);
  }

  @Test
  void should_find_users_by_name() {
    when(userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            "john", "john"))
        .thenReturn(List.of(userEntity));

    List<User> result = userService.findByName("john");

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getId()).isEqualTo(userId);
    assertThat(result.get(0).getFirstName()).isEqualTo("John");

    verify(userRepository)
        .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("john", "john");
  }

  @Test
  void should_find_users_by_last_name() {
    when(userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            "doe", "doe"))
        .thenReturn(List.of(userEntity));

    List<User> result = userService.findByName("doe");

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getLastName()).isEqualTo("Doe");

    verify(userRepository)
        .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("doe", "doe");
  }

  @Test
  void should_reject_null_name() {
    assertThatThrownBy(() -> userService.findByName(null))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("name is required");

    verifyNoInteractions(userRepository);
  }

  @Test
  void should_reject_blank_name() {
    assertThatThrownBy(() -> userService.findByName(" "))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("name is required");

    verifyNoInteractions(userRepository);
  }

  @Test
  void should_find_students_by_group() {
    UUID studentId = UUID.randomUUID();

    JStudent studentEntity =
        JStudent.builder()
            .id(studentId)
            .firstName("Alice")
            .lastName("Smith")
            .birthday(LocalDate.of(2001, 5, 10))
            .sex(Sex.FEMALE)
            .address("Antananarivo")
            .email("alice.smith@test.com")
            .role(Role.STUDENT)
            .build();

    when(groupRepository.existsById(groupId)).thenReturn(true);
    when(studentRepository.findByGroup_Id(groupId)).thenReturn(List.of(studentEntity));

    List<User> result = userService.findByGroup(groupId);

    assertThat(result).hasSize(1);
    assertThat(result.get(0)).isNotNull();
    assertThat(result.get(0).getId()).isEqualTo(studentId);
    assertThat(result.get(0).getFirstName()).isEqualTo("Alice");
    assertThat(result.get(0).getLastName()).isEqualTo("Smith");
    assertThat(result.get(0).getEmail()).isEqualTo("alice.smith@test.com");
    assertThat(result.get(0).getRole()).isEqualTo(Role.STUDENT);

    verify(groupRepository).existsById(groupId);
    verify(studentRepository).findByGroup_Id(groupId);
  }

  @Test
  void should_return_empty_list_when_group_has_no_students() {
    when(groupRepository.existsById(groupId)).thenReturn(true);
    when(studentRepository.findByGroup_Id(groupId)).thenReturn(List.of());

    List<User> result = userService.findByGroup(groupId);

    assertThat(result).isEmpty();

    verify(groupRepository).existsById(groupId);
    verify(studentRepository).findByGroup_Id(groupId);
  }

  @Test
  void should_throw_when_group_not_found() {
    when(groupRepository.existsById(groupId)).thenReturn(false);

    assertThatThrownBy(() -> userService.findByGroup(groupId))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("group not found");

    verify(groupRepository).existsById(groupId);
    verify(studentRepository, never()).findByGroup_Id(any(UUID.class));
  }
}
