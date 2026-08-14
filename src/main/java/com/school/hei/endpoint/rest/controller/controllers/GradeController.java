package com.school.hei.endpoint.rest.controller.controllers;

import com.school.hei.model.Grade;
import com.school.hei.service.services.GradeService;
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
@RequestMapping("/grades")
@RequiredArgsConstructor
public class GradeController {

  private final GradeService gradeService;

  @GetMapping
  public List<Grade> findAll() {
    return gradeService.findAll();
  }

  @GetMapping("/{id}")
  public Grade findById(@PathVariable UUID id) {
    return gradeService.findById(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Grade save(@RequestBody Grade grade) {
    return gradeService.save(grade);
  }

  @PutMapping("/{id}")
  public Grade update(@PathVariable UUID id, @RequestBody Grade grade) {
    return gradeService.update(id, grade);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    gradeService.delete(id);
  }
}
