package com.school.hei.validator;

import com.school.hei.model.Admin;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class AdminValidator implements SaveValidator<Admin> {

  private final UserValidator userValidator;

  @Override
  public void accept(Admin admin) {
    userValidator.validateCommonFields(admin);
    if (admin.getAdminReference() == null || admin.getAdminReference().isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "admin reference is required");
    }
  }
}