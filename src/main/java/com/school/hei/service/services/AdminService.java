package com.school.hei.service.services;

import com.school.hei.entity.JAdmin;
import com.school.hei.mapper.AdminMapper;
import com.school.hei.model.Admin;
import com.school.hei.repository.AdminRepository;
import com.school.hei.validator.AdminValidator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AdminService {

  private final AdminRepository adminRepository;
  private final AdminValidator adminValidator;
  private final PasswordEncoder passwordEncoder;

  public List<Admin> findAll() {
    return adminRepository.findAll().stream().map(AdminMapper::toModel).toList();
  }

  public Admin findById(UUID id) {
    return adminRepository
            .findById(id)
            .map(AdminMapper::toModel)
            .orElseThrow(
                    () ->
                            new ResponseStatusException(HttpStatus.NOT_FOUND, "admin not found with id " + id));
  }

  @Transactional
  public Admin save(Admin admin) {
    adminValidator.accept(admin);

    if (admin.getPassword() == null || admin.getPassword().isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "password is required");
    }

    JAdmin entity = AdminMapper.toEntity(admin);
    entity.setPassword(passwordEncoder.encode(admin.getPassword()));

    return AdminMapper.toModel(adminRepository.save(entity));
  }

  @Transactional
  public Admin update(UUID id, Admin admin) {
    JAdmin existing =
            adminRepository
                    .findById(id)
                    .orElseThrow(
                            () ->
                                    new ResponseStatusException(
                                            HttpStatus.NOT_FOUND, "admin not found with id " + id));

    admin.setId(id);
    adminValidator.accept(admin);

    existing.setFirstName(admin.getFirstName());
    existing.setLastName(admin.getLastName());
    existing.setBirthday(admin.getBirthday());
    existing.setSex(admin.getSex());
    existing.setAddress(admin.getAddress());
    existing.setEmail(admin.getEmail());
    existing.setRole(admin.getRole());
    existing.setAdminReference(admin.getAdminReference());

    if (admin.getPassword() != null && !admin.getPassword().isBlank()) {
      existing.setPassword(passwordEncoder.encode(admin.getPassword()));
    }

    return AdminMapper.toModel(adminRepository.save(existing));
  }

  public void delete(UUID id) {
    if (!adminRepository.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "admin not found with id " + id);
    }
    adminRepository.deleteById(id);
  }
}
