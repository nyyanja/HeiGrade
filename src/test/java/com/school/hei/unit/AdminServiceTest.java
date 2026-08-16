package com.school.hei.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.school.hei.entity.JAdmin;
import com.school.hei.enums.Role;
import com.school.hei.enums.Sex;
import com.school.hei.model.Admin;
import com.school.hei.repository.AdminRepository;
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
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private AdminValidator adminValidator;

    @InjectMocks
    private com.school.hei.service.services.AdminService adminService;

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
                        .build();
    }

    // ============================================================
    // findAll()
    // ============================================================

    @Test
    void should_find_all_admins() {
        when(adminRepository.findAll()).thenReturn(List.of(jAdmin));

        List<Admin> result = adminService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(adminId);
        assertThat(result.get(0).getFirstName()).isEqualTo("John");
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

    // ============================================================
    // findById()
    // ============================================================

    @Test
    void should_find_admin_by_id() {
        when(adminRepository.findById(adminId)).thenReturn(Optional.of(jAdmin));

        Admin result = adminService.findById(adminId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(adminId);
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");
        assertThat(result.getEmail()).isEqualTo("john.doe@hei.school");
        assertThat(result.getRole()).isEqualTo(Role.ADMIN);
        assertThat(result.getAdminReference()).isEqualTo("ADM-001");

        verify(adminRepository).findById(adminId);
    }

    @Test
    void should_throw_not_found_when_admin_does_not_exist() {
        when(adminRepository.findById(adminId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.findById(adminId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("admin not found with id " + adminId);

        verify(adminRepository).findById(adminId);
    }

    // ============================================================
    // save()
    // ============================================================

    @Test
    void should_save_admin() {
        when(adminRepository.save(any(JAdmin.class))).thenReturn(jAdmin);

        Admin result = adminService.save(admin);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(adminId);
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getAdminReference()).isEqualTo("ADM-001");

        verify(adminValidator).accept(admin);
        verify(adminRepository).save(any(JAdmin.class));
    }

    @Test
    void should_validate_admin_before_saving() {
        when(adminRepository.save(any(JAdmin.class))).thenReturn(jAdmin);

        adminService.save(admin);

        verify(adminValidator).accept(admin);
        verify(adminRepository).save(any(JAdmin.class));
    }

    @Test
    void should_not_save_admin_when_validation_fails() {
        doThrow(
                new ResponseStatusException(
                        org.springframework.http.HttpStatus.BAD_REQUEST,
                        "admin reference is required"))
                .when(adminValidator)
                .accept(admin);

        assertThatThrownBy(() -> adminService.save(admin))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("admin reference is required");

        verify(adminValidator).accept(admin);
        verify(adminRepository, never()).save(any(JAdmin.class));
    }

    // ============================================================
    // update()
    // ============================================================

    @Test
    void should_update_admin() {
        when(adminRepository.findById(adminId)).thenReturn(Optional.of(jAdmin));

        admin.setFirstName("Jane");
        admin.setSex(Sex.FEMALE);
        admin.setEmail("jane.doe@hei.school");
        admin.setAdminReference("ADM-002");

        JAdmin updatedEntity =
                JAdmin.builder()
                        .id(adminId)
                        .firstName("Jane")
                        .lastName("Doe")
                        .birthday(LocalDate.of(1995, 5, 10))
                        .sex(Sex.FEMALE)
                        .address("Antananarivo")
                        .email("jane.doe@hei.school")
                        .role(Role.ADMIN)
                        .adminReference("ADM-002")
                        .build();

        when(adminRepository.save(any(JAdmin.class))).thenReturn(updatedEntity);

        Admin result = adminService.update(adminId, admin);

        assertThat(result.getId()).isEqualTo(adminId);
        assertThat(result.getFirstName()).isEqualTo("Jane");
        assertThat(result.getAdminReference()).isEqualTo("ADM-002");
        assertThat(admin.getId()).isEqualTo(adminId);

        verify(adminRepository).findById(adminId);
        verify(adminValidator).accept(admin);
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
    void should_validate_admin_before_update() {
        when(adminRepository.findById(adminId)).thenReturn(Optional.of(jAdmin));
        when(adminRepository.save(any(JAdmin.class))).thenReturn(jAdmin);

        adminService.update(adminId, admin);

        assertThat(admin.getId()).isEqualTo(adminId);

        verify(adminRepository).findById(adminId);
        verify(adminValidator).accept(admin);
        verify(adminRepository).save(any(JAdmin.class));
    }

    // ============================================================
    // delete()
    // ============================================================

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