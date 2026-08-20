package com.school.hei.endpoint.rest.controller.controllers;

import com.school.hei.model.Group;
import com.school.hei.service.services.GroupService;
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
@RequestMapping("/groups")
@RequiredArgsConstructor
public class GroupController {

  private final GroupService groupService;

  @GetMapping
  public List<Group> findAll() {
    return groupService.findAll();
  }

  @GetMapping("/speciality/{specialityId}")
  public List<Group> findBySpeciality(@PathVariable UUID specialityId) {
    return groupService.findBySpeciality(specialityId);
  }

  @GetMapping("/exam/{examId}")
  public List<Group> findByExam(@PathVariable UUID examId) {
    return groupService.findByExam(examId);
  }

  @GetMapping("/course/{courseId}")
  public List<Group> findByCourse(@PathVariable UUID courseId) {
    return groupService.findByCourse(courseId);
  }

  @GetMapping("/name/{name}")
  public List<Group> findByName(@PathVariable String name) {
    return groupService.findByName(name);
  }

  @GetMapping("/{id}")
  public Group findById(@PathVariable UUID id) {
    return groupService.findById(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Group save(@RequestBody Group group) {
    return groupService.save(group);
  }

  @PutMapping("/{id}")
  public Group update(@PathVariable UUID id, @RequestBody Group group) {
    return groupService.update(id, group);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    groupService.delete(id);
  }
}

