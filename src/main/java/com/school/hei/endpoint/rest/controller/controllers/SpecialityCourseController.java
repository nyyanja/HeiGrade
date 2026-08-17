package com.school.hei.endpoint.rest.controller.controllers;

import com.school.hei.model.SpecialityCourse;
import com.school.hei.service.services.SpecialityCourseService;
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
@RequestMapping("/speciality-courses")
@RequiredArgsConstructor
public class SpecialityCourseController {

  private final SpecialityCourseService specialityCourseService;

  @GetMapping
  public List<SpecialityCourse> findAll() {
    return specialityCourseService.findAll();
  }

  @GetMapping("/{id}")
  public SpecialityCourse findById(@PathVariable UUID id) {
    return specialityCourseService.findById(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public SpecialityCourse save(@RequestBody SpecialityCourse specialityCourse) {
    return specialityCourseService.save(specialityCourse);
  }

  @PutMapping("/{id}")
  public SpecialityCourse update(
      @PathVariable UUID id, @RequestBody SpecialityCourse specialityCourse) {
    return specialityCourseService.update(id, specialityCourse);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    specialityCourseService.delete(id);
  }
}
