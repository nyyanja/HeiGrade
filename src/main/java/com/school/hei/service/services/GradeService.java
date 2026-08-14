package com.school.hei.service.services;

import com.school.hei.entity.JGrade;
import com.school.hei.mapper.GradeMapper;
import com.school.hei.model.Grade;
import com.school.hei.repository.GradeRepository;
import com.school.hei.validator.GradeValidator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class GradeService {

  private final GradeRepository gradeRepository;
  private final GradeValidator gradeValidator;

  public List<Grade> findAll() {
    return gradeRepository.findAll().stream().map(GradeMapper::toModel).toList();
  }

  public Grade findById(UUID id) {
    return gradeRepository
        .findById(id)
        .map(GradeMapper::toModel)
        .orElseThrow(
            () ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "grade not found with id " + id));
  }

  public Grade save(Grade grade) {
    gradeValidator.accept(grade);

    JGrade entity = GradeMapper.toEntity(grade);
    return GradeMapper.toModel(gradeRepository.save(entity));
  }

  public Grade update(UUID id, Grade grade) {
    findById(id);

    grade.setId(id);
    gradeValidator.accept(grade);

    JGrade entity = GradeMapper.toEntity(grade);
    return GradeMapper.toModel(gradeRepository.save(entity));
  }

  public void delete(UUID id) {
    if (!gradeRepository.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "grade not found with id " + id);
    }

    gradeRepository.deleteById(id);
  }
}
