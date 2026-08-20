package com.school.hei.service.services;

import com.school.hei.entity.JGradeHistory;
import com.school.hei.mapper.GradeHistoryMapper;
import com.school.hei.model.GradeHistory;
import com.school.hei.repository.GradeHistoryRepository;
import com.school.hei.validator.GradeHistoryValidator;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class GradeHistoryService {

  private final GradeHistoryRepository gradeHistoryRepository;
  private final GradeHistoryValidator gradeHistoryValidator;

  public List<GradeHistory> findAll() {
    return gradeHistoryRepository.findAll().stream().map(GradeHistoryMapper::toModel).toList();
  }

  public GradeHistory findById(UUID id) {
    return gradeHistoryRepository
        .findById(id)
        .map(GradeHistoryMapper::toModel)
        .orElseThrow(
            () ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "grade history not found with id " + id));
  }

  public GradeHistory save(GradeHistory history) {
    gradeHistoryValidator.accept(history);

    if (history.getDate() == null) {
      history.setDate(LocalDateTime.now());
    }

    JGradeHistory entity = GradeHistoryMapper.toEntity(history);
    return GradeHistoryMapper.toModel(gradeHistoryRepository.save(entity));
  }

  public void delete(UUID id) {
    if (!gradeHistoryRepository.existsById(id)) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND, "grade history not found with id " + id);
    }

    gradeHistoryRepository.deleteById(id);
  }
}
