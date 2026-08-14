package com.school.hei.validator;

import com.school.hei.model.User;
import com.school.hei.repository.UserRepository;
import java.time.LocalDate;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class UserValidator {

  private static final Pattern EMAIL_PATTERN =
          Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");

  private final UserRepository userRepository;

  public void validateCommonFields(User user) {
    if (user == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "user cannot be null");
    }
    if (isBlank(user.getFirstName())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "first name is required");
    }
    if (isBlank(user.getEmail())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email is required");
    }
    if (!EMAIL_PATTERN.matcher(user.getEmail()).matches()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email format is invalid");
    }
    if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
      throw new ResponseStatusException(
              HttpStatus.BAD_REQUEST, "birthday cannot be in the future");
    }
    if (user.getSex() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "sex is required");
    }

    userRepository
            .findByEmailIgnoreCase(user.getEmail())
            .ifPresent(
                    existing -> {
                      boolean isSameUser = user.getId() != null && existing.getId().equals(user.getId());
                      if (!isSameUser) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT, "email already used");
                      }
                    });
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}