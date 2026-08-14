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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "the user can not be null");
        }
        if (isBlank(user.getFirstName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "FirstName can not be blank");
        }
        if (isBlank(user.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "L'email is required");
        }
        if (!EMAIL_PATTERN.matcher(user.getEmail()).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "the email format is invalid");
        }
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "the birthday is in the past can not be null or after the current date");
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
                                throw new ResponseStatusException(HttpStatus.CONFLICT, "this email already used");
                            }
                        });
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}