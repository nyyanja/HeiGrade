package com.school.hei.security;

import com.school.hei.entity.JUser;
import com.school.hei.repository.TeacherCourseRepository;
import com.school.hei.repository.UserRepository;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CourseAccessService {

  private final UserRepository userRepository;
  private final TeacherCourseRepository teacherCourseRepository;

  public boolean isAdmin() {
    return hasRole("ROLE_ADMIN");
  }

  public boolean isTeacher() {
    return hasRole("ROLE_TEACHER");
  }

  public boolean isStudent() {
    return hasRole("ROLE_STUDENT");
  }

  public UUID currentUserId() {
    Authentication auth = requireAuth();
    String email = auth.getName();
    return userRepository
        .findByEmailIgnoreCase(email)
        .map(JUser::getId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "user not found"));
  }

  public boolean teaches(UUID courseId) {
    if (courseId == null) {
      return false;
    }
    return teacherCourseRepository
        .findByTeacher_IdAndCourse_Id(currentUserId(), courseId)
        .isPresent();
  }

  public Set<UUID> taughtCourseIds() {
    return teacherCourseRepository.findByTeacher_Id(currentUserId()).stream()
        .filter(tc -> tc.getCourse() != null)
        .map(tc -> tc.getCourse().getId())
        .collect(Collectors.toSet());
  }

  public void assertCanAccessCourse(UUID courseId) {
    if (isAdmin()) {
      return;
    }
    if (isTeacher()) {
      if (!teaches(courseId)) {
        throw new ResponseStatusException(
            HttpStatus.FORBIDDEN, "teacher does not teach this course");
      }
      return;
    }
    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "access denied");
  }

  private boolean hasRole(String role) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null) {
      return false;
    }
    return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(role));
  }

  private Authentication requireAuth() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated()) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "not authenticated");
    }
    return auth;
  }
}

