package com.school.hei.service.services;

import com.school.hei.mapper.StudentGroupHistoryMapper;
import com.school.hei.model.StudentGroupHistory;
import com.school.hei.repository.StudentGroupHistoryRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class StudentGroupHistoryService {

  private final StudentGroupHistoryRepository studentGroupHistoryRepository;

  public List<StudentGroupHistory> findByStudent(UUID studentId) {
    return studentGroupHistoryRepository.findByStudent_Id(studentId).stream()
        .map(StudentGroupHistoryMapper::toModel)
        .toList();
  }

  public List<StudentGroupHistory> findByGroup(UUID groupId) {
    return studentGroupHistoryRepository.findByGroup_Id(groupId).stream()
        .map(StudentGroupHistoryMapper::toModel)
        .toList();
  }

  public StudentGroupHistory findById(UUID id) {
    return studentGroupHistoryRepository
        .findById(id)
        .map(StudentGroupHistoryMapper::toModel)
        .orElseThrow(
            () ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "student group history not found with id " + id));
  }

  public StudentGroupHistory findStudentGroupAtDate(UUID studentId, LocalDate date) {

    return studentGroupHistoryRepository
        .findStudentGroupAtDate(studentId, date)
        .map(StudentGroupHistoryMapper::toModel)
        .orElse(null);
  }
}

