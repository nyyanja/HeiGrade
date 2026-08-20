package com.school.hei.unit;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

import com.school.hei.enums.Role;
import com.school.hei.model.Admin;
import com.school.hei.validator.AdminValidator;
import com.school.hei.validator.UserValidator;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class AdminValidatorTest {

  @Mock private UserValidator userValidator;

  private AdminValidator validator;

  @BeforeEach
  void setUp() {
    validator = new AdminValidator(userValidator);
  }

  private Admin validAdmin() {
    return Admin.builder().id(UUID.randomUUID()).adminReference("ADM-1").role(Role.ADMIN).build();
  }

  @Test
  void should_accept_valid_admin() {
    Admin admin = validAdmin();
    doNothing().when(userValidator).validateCommonFields(admin);

    assertThatCode(() -> validator.accept(admin)).doesNotThrowAnyException();

    verify(userValidator).validateCommonFields(admin);
  }

  @Test
  void should_reject_null_reference() {
    Admin admin = validAdmin();
    admin.setAdminReference(null);
    doNothing().when(userValidator).validateCommonFields(admin);

    assertThatThrownBy(() -> validator.accept(admin))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("admin reference is required");
  }

  @Test
  void should_reject_blank_reference() {
    Admin admin = validAdmin();
    admin.setAdminReference("   ");
    doNothing().when(userValidator).validateCommonFields(admin);

    assertThatThrownBy(() -> validator.accept(admin))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("admin reference is required");
  }

  @Test
  void should_reject_wrong_role() {
    Admin admin = validAdmin();
    admin.setRole(Role.TEACHER);
    doNothing().when(userValidator).validateCommonFields(admin);

    assertThatThrownBy(() -> validator.accept(admin))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("admin role must be ADMIN");
  }
}
