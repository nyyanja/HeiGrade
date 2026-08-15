package com.school.hei.endpoint.rest.controller.controllers;

import com.school.hei.model.Course;
import com.school.hei.service.services.CourseService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController {

  private final CourseService courseService;

  @GetMapping
  public List<Course> findAll() {
    return courseService.findAll();
  }

  @GetMapping("/teacher/{teacherId}")
  public List<Course> findByTeacher(@PathVariable UUID teacherId) {
    return courseService.findByTeacher(teacherId);
  }

  @GetMapping("/speciality/{specialityId}")
  public List<Course> findBySpeciality(@PathVariable UUID specialityId) {
    return courseService.findBySpeciality(specialityId);
  }

  @GetMapping("/credit/{credit}")
  public List<Course> findByCredit(@PathVariable Integer credit) {
    return courseService.findByCredit(credit);
  }

  @GetMapping("/group/{groupId}")
  public List<Course> findByGroup(@PathVariable UUID groupId) {
    return courseService.findByGroup(groupId);
  }

  @GetMapping("/title/{title}")
  public List<Course> findByTitle(@PathVariable String title) {
    return courseService.findByTitle(title);
  }

  @GetMapping("/{id}")
  public Course findById(@PathVariable UUID id) {
    return courseService.findById(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Course save(@RequestBody Course course) {
    return courseService.save(course);
  }

  @PutMapping("/{id}")
  public Course update(@PathVariable UUID id, @RequestBody Course course) {
    return courseService.update(id, course);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    courseService.delete(id);
  }
}
