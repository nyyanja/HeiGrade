package com.school.hei.endpoint.rest.controller.controllers;

import com.school.hei.model.Admin;
import com.school.hei.service.services.AdminService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admins")
@RequiredArgsConstructor
public class AdminController {

  private final AdminService adminService;

  @GetMapping
  public List<Admin> findAll() {
    return adminService.findAll();
  }

  @GetMapping("/{id}")
  public Admin findById(@PathVariable UUID id) {
    return adminService.findById(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Admin save(@RequestBody Admin admin) {
    return adminService.save(admin);
  }

  @PutMapping("/{id}")
  public Admin update(@PathVariable UUID id, @RequestBody Admin admin) {
    return adminService.update(id, admin);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    adminService.delete(id);
  }
}


