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
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AdminService {

  private final AdminRepository adminRepository;
  private final AdminValidator adminValidator;

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

  public Admin save(Admin admin) {
    adminValidator.accept(admin);

    JAdmin entity = AdminMapper.toEntity(admin);
    return AdminMapper.toModel(adminRepository.save(entity));
  }

  public Admin update(UUID id, Admin admin) {
    findById(id);

    admin.setId(id);
    adminValidator.accept(admin);

    JAdmin entity = AdminMapper.toEntity(admin);
    return AdminMapper.toModel(adminRepository.save(entity));
  }

  public void delete(UUID id) {
    if (!adminRepository.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "admin not found with id " + id);
    }

    adminRepository.deleteById(id);
  }
}
