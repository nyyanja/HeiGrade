package com.school.hei.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.school.hei.entity.JAdmin;
import com.school.hei.enums.Role;
import com.school.hei.enums.Sex;
import com.school.hei.model.Admin;
import com.school.hei.repository.AdminRepository;
import com.school.hei.service.services.AdminService;
import com.school.hei.validator.AdminValidator;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

  @Mock private AdminRepository adminRepository;
  @Mock private AdminValidator adminValidator;
  @Mock private PasswordEncoder passwordEncoder;

  @InjectMocks private AdminService adminService;

  private UUID adminId;
  private Admin admin;
  private JAdmin jAdmin;

  @BeforeEach
  void setUp() {
    adminId = UUID.randomUUID();

    admin =
            Admin.builder()
                    .id(adminId)
                    .firstName("John")
                    .lastName("Doe")
                    .birthday(LocalDate.of(1995, 5, 10))
                    .sex(Sex.MALE)
                    .address("Antananarivo")
                    .email("john.doe@hei.school")
                    .role(Role.ADMIN)
                    .adminReference("ADM-001")
                    .build();

    jAdmin =
            JAdmin.builder()
                    .id(adminId)
                    .firstName("John")
                    .lastName("Doe")
                    .birthday(LocalDate.of(1995, 5, 10))
                    .sex(Sex.MALE)
                    .address("Antananarivo")
                    .email("john.doe@hei.school")
                    .role(Role.ADMIN)
                    .adminReference("ADM-001")
                    .password("oldEncoded")
                    .build();
  }

  @Test
  void should_find_all_admins() {
    when(adminRepository.findAll()).thenReturn(List.of(jAdmin));

    List<Admin> result = adminService.findAll();

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getId()).isEqualTo(adminId);
    assertThat(result.get(0).getAdminReference()).isEqualTo("ADM-001");
    verify(adminRepository).findAll();
  }

  @Test
  void should_return_empty_list_when_no_admin_exists() {
    when(adminRepository.findAll()).thenReturn(List.of());

    List<Admin> result = adminService.findAll();

    assertThat(result).isEmpty();
    verify(adminRepository).findAll();
  }

  @Test
  void should_find_admin_by_id() {
    when(adminRepository.findById(adminId)).thenReturn(Optional.of(jAdmin));

    Admin result = adminService.findById(adminId);

    assertThat(result.getId()).isEqualTo(adminId);
    assertThat(result.getAdminReference()).isEqualTo("ADM-001");
    verify(adminRepository).findById(adminId);
  }

  @Test
  void should_throw_when_admin_not_found() {
    when(adminRepository.findById(adminId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> adminService.findById(adminId))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("admin not found with id " + adminId);
  }


  @Test
  void should_save_admin() {
    Admin toSave =
            Admin.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .birthday(LocalDate.of(1995, 5, 10))
                    .sex(Sex.MALE)
                    .address("Antananarivo")
                    .email("john.doe@hei.school")
                    .role(Role.ADMIN)
                    .adminReference("ADM-001")
                    .password("plainPassword123")
                    .build();

    JAdmin saved =
            JAdmin.builder()
                    .id(adminId)
                    .firstName("John")
                    .lastName("Doe")
                    .email("john.doe@hei.school")
                    .role(Role.ADMIN)
                    .adminReference("ADM-001")
                    .password("encodedPassword")
                    .build();

    when(passwordEncoder.encode("plainPassword123")).thenReturn("encodedPassword");
    when(adminRepository.save(any(JAdmin.class))).thenReturn(saved);

    Admin result = adminService.save(toSave);

    assertThat(result.getId()).isEqualTo(adminId);
    verify(adminValidator).accept(toSave);
    verify(passwordEncoder).encode("plainPassword123");
    verify(adminRepository).save(any(JAdmin.class));
  }

  @Test
  void should_throw_when_password_missing_on_save() {

    assertThatThrownBy(() -> adminService.save(admin))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("password is required");

    verify(passwordEncoder, never()).encode(anyString());
    verify(adminRepository, never()).save(any());
  }


  @Test
  void should_update_admin_without_password() {
    Admin updated =
            Admin.builder()
                    .firstName("Jane")
                    .lastName("Doe")
                    .birthday(LocalDate.of(1995, 5, 10))
                    .sex(Sex.FEMALE)
                    .address("Antananarivo")
                    .email("jane.doe@hei.school")
                    .role(Role.ADMIN)
                    .adminReference("ADM-002")
                    .build(); // no password

    JAdmin updatedEntity =
            JAdmin.builder()
                    .id(adminId)
                    .firstName("Jane")
                    .lastName("Doe")
                    .sex(Sex.FEMALE)
                    .email("jane.doe@hei.school")
                    .role(Role.ADMIN)
                    .adminReference("ADM-002")
                    .password("oldEncoded")
                    .build();

    when(adminRepository.findById(adminId)).thenReturn(Optional.of(jAdmin));
    when(adminRepository.save(any(JAdmin.class))).thenReturn(updatedEntity);

    Admin result = adminService.update(adminId, updated);

    assertThat(result.getFirstName()).isEqualTo("Jane");
    assertThat(result.getAdminReference()).isEqualTo("ADM-002");
    assertThat(updated.getId()).isEqualTo(adminId);

    verify(adminRepository).findById(adminId);
    verify(adminValidator).accept(updated);
    verify(passwordEncoder, never()).encode(anyString());
    verify(adminRepository).save(any(JAdmin.class));
  }

  @Test
  void should_update_admin_and_change_password_when_provided() {
    Admin updated =
            Admin.builder()
                    .firstName("Jane")
                    .lastName("Doe")
                    .birthday(LocalDate.of(1995, 5, 10))
                    .sex(Sex.FEMALE)
                    .address("Antananarivo")
                    .email("jane.doe@hei.school")
                    .role(Role.ADMIN)
                    .adminReference("ADM-002")
                    .password("newPlainPassword")
                    .build();

    when(adminRepository.findById(adminId)).thenReturn(Optional.of(jAdmin));
    when(passwordEncoder.encode("newPlainPassword")).thenReturn("newEncoded");
    when(adminRepository.save(any(JAdmin.class))).thenAnswer(inv -> inv.getArgument(0));

    Admin result = adminService.update(adminId, updated);

    assertThat(result.getFirstName()).isEqualTo("Jane");
    verify(passwordEncoder).encode("newPlainPassword");
    verify(adminRepository).save(any(JAdmin.class));
  }

  @Test
  void should_not_update_admin_when_admin_does_not_exist() {
    when(adminRepository.findById(adminId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> adminService.update(adminId, admin))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("admin not found with id " + adminId);

    verify(adminRepository).findById(adminId);
    verify(adminValidator, never()).accept(any(Admin.class));
    verify(adminRepository, never()).save(any(JAdmin.class));
  }


  @Test
  void should_delete_admin() {
    when(adminRepository.existsById(adminId)).thenReturn(true);

    adminService.delete(adminId);

    verify(adminRepository).existsById(adminId);
    verify(adminRepository).deleteById(adminId);
  }

  @Test
  void should_throw_not_found_when_deleting_non_existing_admin() {
    when(adminRepository.existsById(adminId)).thenReturn(false);

    assertThatThrownBy(() -> adminService.delete(adminId))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("admin not found with id " + adminId);

    verify(adminRepository).existsById(adminId);
    verify(adminRepository, never()).deleteById(any(UUID.class));
  }
}
