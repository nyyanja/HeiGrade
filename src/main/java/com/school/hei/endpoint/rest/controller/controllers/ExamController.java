package com.school.hei.endpoint.rest.controller.controllers;

import com.school.hei.model.Exam;
import com.school.hei.service.services.ExamService;
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