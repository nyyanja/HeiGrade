package com.school.hei.service.services;

import com.school.hei.entity.JExam;
import com.school.hei.mapper.ExamMapper;
import com.school.hei.model.Exam;
import com.school.hei.repository.ExamRepository;
import com.school.hei.validator.ExamValidator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ExamService {

  private final ExamRepository examRepository;
  private final ExamValidator examValidator;

  public List<Exam> findAll() {
    return examRepository.findAll().stream().map(ExamMapper::toModel).toList();
  }

  public Exam findById(UUID id) {
    return examRepository
        .findById(id)
        .map(ExamMapper::toModel)
        .orElseThrow(
            () ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "exam not found with id " + id));
  }

  public Exam save(Exam exam) {
    examValidator.accept(exam);

    JExam entity = ExamMapper.toEntity(exam);
    return ExamMapper.toModel(examRepository.save(entity));
  }

  public Exam update(UUID id, Exam exam) {
    findById(id);

    exam.setId(id);
    examValidator.accept(exam);

    JExam entity = ExamMapper.toEntity(exam);
    return ExamMapper.toModel(examRepository.save(entity));
  }

  public void delete(UUID id) {
    if (!examRepository.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "exam not found with id " + id);
    }

    examRepository.deleteById(id);
  }
}
