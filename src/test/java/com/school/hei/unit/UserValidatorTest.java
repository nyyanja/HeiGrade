package com.school.hei.unit;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.school.hei.entity.JUser;
import com.school.hei.enums.Sex;
import com.school.hei.model.User;
import com.school.hei.repository.UserRepository;
import com.school.hei.validator.UserValidator;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class UserValidatorTest {

  @Mock private UserRepository userRepository;

  private UserValidator validator;

  @BeforeEach
  void setUp() {
    validator = new UserValidator(userRepository);
  }

  private User validUser() {
    return User.builder()
        .firstName("John")
        .lastName("Doe")
        .email("john.doe@example.com")
        .birthday(LocalDate.now().minusYears(20))
        .sex(Sex.MALE)
        .password("secret")
        .build();
  }

  @Test
  void should_accept_valid_user() {
    User user = validUser();
    when(userRepository.findByEmailIgnoreCase("john.doe@example.com")).thenReturn(Optional.empty());

    assertThatCode(() -> validator.validateCommonFields(user)).doesNotThrowAnyException();
  }

  @Test
  void should_reject_null_user() {
    assertThatThrownBy(() -> validator.validateCommonFields(null))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("user cannot be null");
  }

  @Test
  void should_reject_blank_first_name() {
    User user = validUser();
    user.setFirstName("  ");

    assertThatThrownBy(() -> validator.validateCommonFields(user))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("first name is required");
  }

  @Test
  void should_reject_blank_email() {
    User user = validUser();
    user.setEmail("  ");

    assertThatThrownBy(() -> validator.validateCommonFields(user))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("email is required");
  }

  @Test
  void should_reject_invalid_email_format() {
    User user = validUser();
    user.setEmail("not-an-email");

    assertThatThrownBy(() -> validator.validateCommonFields(user))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("email format is invalid");
  }

  @Test
  void should_reject_future_birthday() {
    User user = validUser();
    user.setBirthday(LocalDate.now().plusDays(1));

    assertThatThrownBy(() -> validator.validateCommonFields(user))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("birthday cannot be in the future");
  }

  @Test
  void should_reject_null_sex() {
    User user = validUser();
    user.setSex(null);

    assertThatThrownBy(() -> validator.validateCommonFields(user))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("sex is required");
  }

  @Test
  void should_reject_when_email_already_used() {
    User user = validUser();
    JUser existing = JUser.builder().id(UUID.randomUUID()).build();
    when(userRepository.findByEmailIgnoreCase("john.doe@example.com"))
        .thenReturn(Optional.of(existing));

    assertThatThrownBy(() -> validator.validateCommonFields(user))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("email already used");
  }

  @Test
  void should_accept_when_existing_email_is_same_user() {
    User user = validUser();
    user.setId(UUID.randomUUID());
    JUser existing = JUser.builder().id(user.getId()).build();
    when(userRepository.findByEmailIgnoreCase("john.doe@example.com"))
        .thenReturn(Optional.of(existing));

    assertThatCode(() -> validator.validateCommonFields(user)).doesNotThrowAnyException();
  }
}
