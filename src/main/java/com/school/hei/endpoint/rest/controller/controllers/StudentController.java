package com.school.hei.endpoint.rest.controller.controllers;

import com.school.hei.model.Student;
import com.school.hei.service.services.StudentService;
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
@RequestMapping("/students")
@RequiredArgsConstructor
public class StudentController {

  private final StudentService studentService;

  @GetMapping
  public List<Student> findAll() {
    return studentService.findAll();
  }

  @GetMapping("/{id}")
  public Student findById(@PathVariable UUID id) {
    return studentService.findById(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Student save(@RequestBody Student student) {
    return studentService.save(student);
  }

  @PutMapping("/{id}")
  public Student update(@PathVariable UUID id, @RequestBody Student student) {
    return studentService.update(id, student);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    studentService.delete(id);
  }
}
