package com.school.hei.endpoint.rest.controller.controllers;

import com.school.hei.model.LoginRequest;
import com.school.hei.model.LoginResponse;
import com.school.hei.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping("/login")
  public LoginResponse login(@RequestBody LoginRequest request) {

    return authService.login(request);
  }
}


