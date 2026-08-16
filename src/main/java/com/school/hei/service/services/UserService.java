package com.school.hei.service.services;

import com.school.hei.entity.JUser;
import com.school.hei.enums.Role;
import com.school.hei.mapper.StudentMapper;
import com.school.hei.mapper.UserMapper;
import com.school.hei.model.CreateUserRequest;
import com.school.hei.model.User;
import com.school.hei.repository.GroupRepository;
import com.school.hei.repository.StudentRepository;
import com.school.hei.repository.UserRepository;
import com.school.hei.validator.UserValidator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final UserValidator userValidator;
  private final StudentRepository studentRepository;
  private final GroupRepository groupRepository;
  private final PasswordEncoder passwordEncoder;

  public List<User> findAll() {
    return userRepository.findAll().stream().map(UserMapper::toModel).toList();
  }

  public User findById(UUID id) {
    return userRepository
        .findById(id)
        .map(UserMapper::toModel)
        .orElseThrow(
            () ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found with id " + id));
  }

  public User save(CreateUserRequest request) {

    if (request == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "user is required");
    }

    User user =
        User.builder()
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .email(request.getEmail())
            .role(request.getRole())
            .build();

    userValidator.validateCommonFields(user);

    String encodedPassword = passwordEncoder.encode(request.getPassword());

    JUser entity =
        JUser.builder()
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .email(request.getEmail())
            .role(request.getRole())
            .password(encodedPassword)
            .build();

    return UserMapper.toModel(userRepository.save(entity));
  }

  public User update(UUID id, User user) {

    JUser existing =
        userRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "user not found with id " + id));

    user.setId(id);

    userValidator.validateCommonFields(user);

    existing.setFirstName(user.getFirstName());
    existing.setLastName(user.getLastName());
    existing.setBirthday(user.getBirthday());
    existing.setSex(user.getSex());
    existing.setAddress(user.getAddress());
    existing.setEmail(user.getEmail());
    existing.setRole(user.getRole());

    return UserMapper.toModel(userRepository.save(existing));
  }

  public void delete(UUID id) {
    if (!userRepository.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found with id " + id);
    }

    userRepository.deleteById(id);
  }

  public List<User> findByRole(Role role) {
    if (role == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "role is required");
    }

    return userRepository.findByRole(role).stream().map(UserMapper::toModel).toList();
  }

  public List<User> findByName(String name) {
    if (name == null || name.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name is required");
    }

    return userRepository
        .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(name, name)
        .stream()
        .map(UserMapper::toModel)
        .toList();
  }

  public List<User> findByGroup(UUID groupId) {
    if (!groupRepository.existsById(groupId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "group not found");
    }

    return studentRepository.findByGroup_Id(groupId).stream()
        .map(StudentMapper::toModel)
        .map(student -> (User) student)
        .toList();
  }
}
