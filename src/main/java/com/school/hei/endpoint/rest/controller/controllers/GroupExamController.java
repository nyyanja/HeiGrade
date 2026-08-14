package com.school.hei.endpoint.rest.controller.controllers;

import com.school.hei.model.GroupExam;

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
@RequestMapping("/group-exams")
@RequiredArgsConstructor
public class GroupExamController {

  private final GroupExamService groupExamService;

  @GetMapping
  public List<GroupExam> findAll() {
    return groupExamService.findAll();
  }

  @GetMapping("/{id}")
  public GroupExam findById(@PathVariable UUID id) {
    return groupExamService.findById(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public GroupExam save(@RequestBody GroupExam groupExam) {
    return groupExamService.save(groupExam);
  }

  @PutMapping("/{id}")
  public GroupExam update(@PathVariable UUID id, @RequestBody GroupExam groupExam) {
    return groupExamService.update(id, groupExam);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    groupExamService.delete(id);
  }
}
