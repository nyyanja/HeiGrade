package com.school.hei.endpoint.rest.controller.controllers;

import com.school.hei.model.Exam;
import com.school.hei.service.services.ExamService;
import java.time.LocalDate;
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
@RequestMapping("/exams")
@RequiredArgsConstructor
public class ExamController {

  private final ExamService examService;

  @GetMapping
  public List<Exam> findAll() {
    return examService.findAll();
  }

  @GetMapping("/course/{courseId}/remaining-coeff")
  public Double remainingCoeff(@PathVariable UUID courseId) {
    return examService.remainingCoeff(courseId);
  }

  @GetMapping("/course/{courseId}")
  public List<Exam> findByCourse(@PathVariable UUID courseId) {
    return examService.findByCourse(courseId);
  }

  @GetMapping("/title/{title}")
  public List<Exam> findByTitle(@PathVariable String title) {
    return examService.findByTitle(title);
  }

  @GetMapping("/date/{date}")
  public List<Exam> findByDate(@PathVariable LocalDate date) {
    return examService.findByDate(date);
  }

  @GetMapping("/coeff/{coeff}")
  public List<Exam> findByCoeff(@PathVariable Double coeff) {
    return examService.findByCoeff(coeff);
  }

  @GetMapping("/group/{groupId}")
  public List<Exam> findByGroup(@PathVariable UUID groupId) {
    return examService.findByGroup(groupId);
  }

  @GetMapping("/speciality/{specialityId}")
  public List<Exam> findBySpeciality(@PathVariable UUID specialityId) {
    return examService.findBySpeciality(specialityId);
  }

  @GetMapping("/{id}")
  public Exam findById(@PathVariable UUID id) {
    return examService.findById(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Exam save(@RequestBody Exam exam) {
    return examService.save(exam);
  }

  @PutMapping("/{id}")
  public Exam update(@PathVariable UUID id, @RequestBody Exam exam) {
    return examService.update(id, exam);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    examService.delete(id);
  }
}
