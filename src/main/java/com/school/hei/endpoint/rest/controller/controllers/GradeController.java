package com.school.hei.endpoint.rest.controller.controllers;

import com.school.hei.model.Grade;
import com.school.hei.service.services.GradeService;
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
import org.springframework.web.bind.annotation.RequestParam;
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
  public Grade update(
      @PathVariable UUID id,
      @RequestBody Grade grade,
      @RequestParam String reason,
      @RequestParam UUID modifiedById) {

    return gradeService.update(id, grade, reason, modifiedById);
  }

  @PostMapping("/batch")
  public List<Grade> saveAll(@RequestBody List<Grade> grades) {
    return gradeService.saveAll(grades);
  }

  @GetMapping("/exam/{examId}")
  public List<Grade> findByExam(@PathVariable UUID examId) {
    return gradeService.findByExam(examId);
  }

  @GetMapping("/student/{studentId}")
  public List<Grade> findByStudent(@PathVariable UUID studentId) {
    return gradeService.findByStudent(studentId);
  }

  @GetMapping("/course/{courseId}")
  public List<Grade> findByCourse(@PathVariable UUID courseId) {
    return gradeService.findByCourse(courseId);
  }

  @GetMapping("/student/{studentId}/course/{courseId}")
  public List<Grade> findByStudentAndCourse(
      @PathVariable UUID studentId, @PathVariable UUID courseId) {
    return gradeService.findByStudentAndCourse(studentId, courseId);
  }

  @GetMapping("/group/{groupId}")
  public List<Grade> findByGroup(@PathVariable UUID groupId) {
    return gradeService.findByGroup(groupId);
  }

  @GetMapping("/group/{groupId}/exam/{examId}")
  public List<Grade> findByGroupAndExam(@PathVariable UUID groupId, @PathVariable UUID examId) {
    return gradeService.findByGroupAndExam(groupId, examId);
  }

  @GetMapping("/group/{groupId}/course/{courseId}")
  public List<Grade> findByGroupAndCourse(@PathVariable UUID groupId, @PathVariable UUID courseId) {
    return gradeService.findByGroupAndCourse(groupId, courseId);
  }

  @GetMapping("/speciality/{specialityId}")
  public List<Grade> findBySpeciality(@PathVariable UUID specialityId) {
    return gradeService.findBySpeciality(specialityId);
  }

  @GetMapping("/speciality/{specialityId}/exam/{examId}")
  public List<Grade> findBySpecialityAndExam(
      @PathVariable UUID specialityId, @PathVariable UUID examId) {
    return gradeService.findBySpecialityAndExam(specialityId, examId);
  }

  @GetMapping("/speciality/{specialityId}/course/{courseId}")
  public List<Grade> findBySpecialityAndCourse(
      @PathVariable UUID specialityId, @PathVariable UUID courseId) {
    return gradeService.findBySpecialityAndCourse(specialityId, courseId);
  }

  @GetMapping("/student/{studentId}/exam/{examId}")
  public Grade findByStudentAndExam(@PathVariable UUID studentId, @PathVariable UUID examId) {
    return gradeService.findByStudentAndExam(studentId, examId);
  }

  @GetMapping("/date/{date}")
  public List<Grade> findByDate(@PathVariable LocalDate date) {
    return gradeService.findByDate(date);
  }

  @GetMapping("/min-value/{minValue}")
  public List<Grade> findByMinValue(@PathVariable Double minValue) {
    return gradeService.findByMinValue(minValue);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    gradeService.delete(id);
  }
}


