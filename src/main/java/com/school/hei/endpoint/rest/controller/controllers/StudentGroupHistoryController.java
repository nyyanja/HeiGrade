package com.school.hei.endpoint.rest.controller.controllers;

import com.school.hei.model.StudentGroupHistory;
import com.school.hei.service.services.StudentGroupHistoryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/student-group-histories")
public class StudentGroupHistoryController {

    private final StudentGroupHistoryService studentGroupHistoryService;

    @GetMapping("/{id}")
    public StudentGroupHistory findById(@PathVariable UUID id) {
        return studentGroupHistoryService.findById(id);
    }

    @GetMapping("/student/{studentId}")
    public List<StudentGroupHistory> findByStudent(
            @PathVariable UUID studentId) {
        return studentGroupHistoryService.findByStudent(studentId);
    }

    @GetMapping("/group/{groupId}")
    public List<StudentGroupHistory> findByGroup(
            @PathVariable UUID groupId) {
        return studentGroupHistoryService.findByGroup(groupId);
    }
}