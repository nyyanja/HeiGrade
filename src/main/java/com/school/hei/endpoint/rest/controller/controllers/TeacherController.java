package com.school.hei.endpoint.rest.controller.controllers;

import com.school.hei.model.Teacher;
import com.school.hei.service.services.TeacherService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/teachers")
@RequiredArgsConstructor
public class TeacherController {

  private final TeacherService teacherService;

  @GetMapping
  public List<Teacher> findAll() {
    return teacherService.findAll();
  }

  @GetMapping("/{id}")
  public Teacher findById(@PathVariable UUID id) {
    return teacherService.findById(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Teacher save(@RequestBody Teacher teacher) {
    return teacherService.save(teacher);
  }

  @PutMapping("/{id}")
  public Teacher update(@PathVariable UUID id, @RequestBody Teacher teacher) {
    return teacherService.update(id, teacher);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    teacherService.delete(id);
  }

  @GetMapping("/speciality/{speciality}")
  public List<Teacher> findBySpeciality(@PathVariable String speciality) {
    return teacherService.findBySpeciality(speciality);
  }

  @GetMapping("/name/{name}")
  public List<Teacher> findByName(@PathVariable String name) {
    return teacherService.findByName(name);
  }

  @GetMapping("/course/{courseId}")
  public List<Teacher> findByCourse(@PathVariable UUID courseId) {
    return teacherService.findByCourse(courseId);
  }
}
