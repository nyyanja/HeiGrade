package com.school.hei.service.services;

import com.school.hei.mapper.SpecialityCourseMapper;
import com.school.hei.model.SpecialityCourse;
import com.school.hei.repository.SpecialityCourseRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
@Service
@RequiredArgsConstructor
public class SpecialityCourseService {

  private final SpecialityCourseRepository specialityCourseRepository;

  public List<SpecialityCourse> findAll() {
    return specialityCourseRepository.findAll().stream()
            .map(SpecialityCourseMapper::toModel)
            .toList();
  }

  public SpecialityCourse findById(UUID id) {
    return specialityCourseRepository
            .findById(id)
            .map(SpecialityCourseMapper::toModel)
            .orElseThrow(
                    () ->
                            new ResponseStatusException(
                                    HttpStatus.NOT_FOUND, "speciality course not found with id " + id));
  }
}