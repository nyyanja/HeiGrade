package com.school.hei.service.services;

import com.school.hei.entity.JCourse;
import com.school.hei.entity.JSpecialityCourse;
import com.school.hei.entity.JTeacherCourse;
import com.school.hei.mapper.CourseMapper;
import com.school.hei.mapper.SpecialityMapper;
import com.school.hei.mapper.TeacherMapper;
import com.school.hei.model.Course;
import com.school.hei.model.Speciality;
import com.school.hei.model.Teacher;
import com.school.hei.repository.*;
import com.school.hei.security.CourseAccessService;
import com.school.hei.validator.CourseValidator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CourseService {

  private static final int MAX_CREDITS_PER_LEVEL = 60;

  private final CourseRepository courseRepository;
  private final CourseValidator courseValidator;
  private final TeacherRepository teacherRepository;
  private final SpecialityRepository specialityRepository;
  private final TeacherCourseRepository teacherCourseRepository;
  private final SpecialityCourseRepository specialityCourseRepository;
  private final GroupRepository groupRepository;
  private final CourseAccessService courseAccessService;

  @Transactional
  public Course save(Course course) {
    courseValidator.accept(course);
    validateTeachers(course);
    validateSpecialities(course);
    validateCreditsPerLevel(course);

    JCourse savedCourse = courseRepository.save(CourseMapper.toEntity(course));
    course.setId(savedCourse.getId());

    saveTeacherCourses(course, savedCourse);
    saveSpecialityCourses(course, savedCourse);

    return toModelWithRelations(savedCourse);
  }

  @Transactional
  public Course update(UUID id, Course course) {
    findById(id);
    course.setId(id);
    courseValidator.accept(course);
    validateTeachers(course);
    validateSpecialities(course);
    validateCreditsPerLevel(course);

    JCourse savedCourse = courseRepository.save(CourseMapper.toEntity(course));

    teacherCourseRepository.deleteByCourse_Id(id);
    specialityCourseRepository.deleteByCourse_Id(id);
    saveTeacherCourses(course, savedCourse);
    saveSpecialityCourses(course, savedCourse);

    return toModelWithRelations(savedCourse);
  }

  public void delete(UUID id) {
    if (!courseRepository.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "course not found with id " + id);
    }
    teacherCourseRepository.deleteByCourse_Id(id);
    specialityCourseRepository.deleteByCourse_Id(id);
    courseRepository.deleteById(id);
  }

  private void validateTeachers(Course course) {
    if (course.getTeachers() == null || course.getTeachers().isEmpty()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "at least one teacher is required to create a course");
    }
    for (Teacher teacher : course.getTeachers()) {
      if (teacher == null || teacher.getId() == null) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "teacher id is required");
      }
      if (!teacherRepository.existsById(teacher.getId())) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "teacher not found");
      }
    }
  }

  private void validateSpecialities(Course course) {
    if (course.getSpecialities() == null || course.getSpecialities().isEmpty()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "at least one speciality is required to create a course");
    }
    for (Speciality speciality : course.getSpecialities()) {
      if (speciality == null || speciality.getId() == null) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "speciality id is required");
      }
      if (!specialityRepository.existsById(speciality.getId())) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "speciality not found");
      }
    }
  }

  private void validateCreditsPerLevel(Course course) {
    if (course.getSpecialities() == null || course.getSpecialities().isEmpty()) {
      return;
    }

    Integer level = course.getLevel();
    int newCredit = course.getCredit();

    for (Speciality speciality : course.getSpecialities()) {
      if (speciality == null || speciality.getId() == null) {
        continue;
      }

      UUID specialityId = speciality.getId();
      List<JCourse> existingCourses =
          courseRepository.findBySpecialityIdAndLevel(specialityId, level);

      int existingSum =
          existingCourses.stream()
              .filter(c -> course.getId() == null || !c.getId().equals(course.getId()))
              .mapToInt(JCourse::getCredit)
              .sum();

      int total = existingSum + newCredit;

      if (total > MAX_CREDITS_PER_LEVEL) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            String.format(
                "cannot create/update course: total credits for speciality at level %d "
                    + "would reach %d (existing %d + new %d), maximum is %d",
                level, total, existingSum, newCredit, MAX_CREDITS_PER_LEVEL));
      }
    }
  }

  private void saveTeacherCourses(Course course, JCourse savedCourse) {
    for (Teacher teacher : course.getTeachers()) {
      JTeacherCourse entity =
          JTeacherCourse.builder()
              .teacher(teacherRepository.getReferenceById(teacher.getId()))
              .course(savedCourse)
              .build();
      teacherCourseRepository.save(entity);
    }
  }

  private void saveSpecialityCourses(Course course, JCourse savedCourse) {
    for (Speciality speciality : course.getSpecialities()) {
      JSpecialityCourse entity =
          JSpecialityCourse.builder()
              .speciality(specialityRepository.getReferenceById(speciality.getId()))
              .course(savedCourse)
              .build();
      specialityCourseRepository.save(entity);
    }
  }

  private Course toModelWithRelations(JCourse entity) {
    Course model = CourseMapper.toModel(entity);
    model.setTeachers(
        teacherCourseRepository.findByCourse_Id(entity.getId()).stream()
            .map(tc -> TeacherMapper.toModel(tc.getTeacher()))
            .toList());
    model.setSpecialities(
        specialityCourseRepository.findByCourse_Id(entity.getId()).stream()
            .map(sc -> SpecialityMapper.toModel(sc.getSpeciality()))
            .toList());
    return model;
  }

  public List<Course> findAll() {
    List<JCourse> courses = courseRepository.findAll();
    return filterCoursesForCurrentUser(courses).stream().map(this::toModelWithRelations).toList();
  }

  public Course findById(UUID id) {
    JCourse entity =
        courseRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "course not found with id " + id));
    assertCanReadCourse(entity.getId());
    return toModelWithRelations(entity);
  }

  public List<Course> findByTeacher(UUID teacherId) {
    if (!teacherRepository.existsById(teacherId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "teacher not found");
    }
    if (courseAccessService.isTeacher() && !courseAccessService.isAdmin()) {
      if (!courseAccessService.currentUserId().equals(teacherId)) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "not your courses");
      }
    }
    return courseRepository.findByTeacherId(teacherId).stream()
        .map(this::toModelWithRelations)
        .toList();
  }

  public List<Course> findBySpeciality(UUID specialityId) {
    if (!specialityRepository.existsById(specialityId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "speciality not found");
    }
    List<JCourse> courses = courseRepository.findBySpecialityId(specialityId);
    return filterCoursesForCurrentUser(courses).stream().map(this::toModelWithRelations).toList();
  }

  public List<Course> findByCredit(Integer credit) {
    if (credit == null || credit <= 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "credit must be greater than 0");
    }
    List<JCourse> courses = courseRepository.findByCredit(credit);
    return filterCoursesForCurrentUser(courses).stream().map(this::toModelWithRelations).toList();
  }

  public List<Course> findByGroup(UUID groupId) {
    var group =
        groupRepository
            .findById(groupId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "group not found"));
    List<JCourse> courses = courseRepository.findBySpecialityId(group.getSpeciality().getId());
    return filterCoursesForCurrentUser(courses).stream().map(this::toModelWithRelations).toList();
  }

  public List<Course> findByTitle(String title) {
    if (title == null || title.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "title is required");
    }
    List<JCourse> courses = courseRepository.findByTitleContainingIgnoreCase(title);
    return filterCoursesForCurrentUser(courses).stream().map(this::toModelWithRelations).toList();
  }

  public List<Course> findByLevel(Integer level) {
    if (level == null || level < 1 || level > 3) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "level must be 1, 2 or 3");
    }
    List<JCourse> courses = courseRepository.findByLevel(level);
    return filterCoursesForCurrentUser(courses).stream().map(this::toModelWithRelations).toList();
  }

  private List<JCourse> filterCoursesForCurrentUser(List<JCourse> courses) {
    if (courseAccessService.isAdmin()) {
      return courses;
    }
    if (courseAccessService.isTeacher()) {
      Set<UUID> taught = courseAccessService.taughtCourseIds();
      return courses.stream().filter(c -> taught.contains(c.getId())).toList();
    }
    if (courseAccessService.isStudent()) {
      return courses;
    }
    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "access denied");
  }

  private void assertCanReadCourse(UUID courseId) {
    if (courseAccessService.isAdmin() || courseAccessService.isStudent()) {
      return;
    }
    if (courseAccessService.isTeacher()) {
      courseAccessService.assertCanAccessCourse(courseId);
      return;
    }
    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "access denied");
  }
}
