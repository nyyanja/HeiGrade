package com.school.hei.endpoint.rest.controller.controllers;

import com.school.hei.enums.Role;
import com.school.hei.model.CreateUserRequest;
import com.school.hei.model.User;
import com.school.hei.service.services.UserService;
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
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @GetMapping
  public List<User> findAll() {
    return userService.findAll();
  }

  @GetMapping("/role/{role}")
  public List<User> findByRole(@PathVariable Role role) {
    return userService.findByRole(role);
  }

  @GetMapping("/name/{name}")
  public List<User> findByName(@PathVariable String name) {
    return userService.findByName(name);
  }

  @GetMapping("/group/{groupId}")
  public List<User> findByGroup(@PathVariable UUID groupId) {
    return userService.findByGroup(groupId);
  }

  @GetMapping("/{id}")
  public User findById(@PathVariable UUID id) {
    return userService.findById(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public User save(@RequestBody CreateUserRequest request) {
    return userService.save(request);
  }

  @PutMapping("/{id}")
  public User update(@PathVariable UUID id, @RequestBody User user) {
    return userService.update(id, user);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    userService.delete(id);
  }
}


