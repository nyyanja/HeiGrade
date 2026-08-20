package com.school.hei.endpoint.rest.controller.controllers;

import com.school.hei.model.GradeHistory;
import com.school.hei.service.services.GradeHistoryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/grade-histories")
@RequiredArgsConstructor
public class GradeHistoryController {

  private final GradeHistoryService gradeHistoryService;

  @GetMapping
  public List<GradeHistory> findAll() {
    return gradeHistoryService.findAll();
  }

  @GetMapping("/{id}")
  public GradeHistory findById(@PathVariable UUID id) {
    return gradeHistoryService.findById(id);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    gradeHistoryService.delete(id);
  }
}
