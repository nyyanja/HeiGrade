package com.school.hei.endpoint.rest.controller.controllers;

import com.school.hei.model.TeacherCourse;
import com.school.hei.service.services.TeacherCourseService;
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
@RequestMapping("/teacher-courses")
@RequiredArgsConstructor
public class TeacherCourseController {

  private final TeacherCourseService teacherCourseService;

  @GetMapping
  public List<TeacherCourse> findAll() {
    return teacherCourseService.findAll();
  }

  @GetMapping("/{id}")
  public TeacherCourse findById(@PathVariable UUID id) {
    return teacherCourseService.findById(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public TeacherCourse save(@RequestBody TeacherCourse teacherCourse) {
    return teacherCourseService.save(teacherCourse);
  }

  @PutMapping("/{id}")
  public TeacherCourse update(@PathVariable UUID id, @RequestBody TeacherCourse teacherCourse) {
    return teacherCourseService.update(id, teacherCourse);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    teacherCourseService.delete(id);
  }
}


