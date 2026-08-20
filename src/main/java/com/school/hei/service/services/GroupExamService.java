package com.school.hei.service.services;

import com.school.hei.entity.JGroupExam;
import com.school.hei.mapper.GroupExamMapper;
import com.school.hei.model.GroupExam;
import com.school.hei.repository.GroupExamRepository;
import com.school.hei.validator.GroupExamValidator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class GroupExamService {

  private final GroupExamRepository groupExamRepository;
  private final GroupExamValidator groupExamValidator;

  public List<GroupExam> findAll() {
    return groupExamRepository.findAll().stream().map(GroupExamMapper::toModel).toList();
  }

  public GroupExam findById(UUID id) {
    return groupExamRepository
        .findById(id)
        .map(GroupExamMapper::toModel)
        .orElseThrow(
            () ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "group exam not found with id " + id));
  }

  public GroupExam save(GroupExam groupExam) {
    groupExamValidator.accept(groupExam);

    JGroupExam entity = GroupExamMapper.toEntity(groupExam);
    return GroupExamMapper.toModel(groupExamRepository.save(entity));
  }

  public GroupExam update(UUID id, GroupExam groupExam) {
    findById(id);

    groupExam.setId(id);
    groupExamValidator.accept(groupExam);

    JGroupExam entity = GroupExamMapper.toEntity(groupExam);
    return GroupExamMapper.toModel(groupExamRepository.save(entity));
  }

  public void delete(UUID id) {
    if (!groupExamRepository.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "group exam not found with id " + id);
    }

    groupExamRepository.deleteById(id);
  }
}
