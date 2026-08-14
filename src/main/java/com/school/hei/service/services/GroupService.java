package com.school.hei.service.services;

import com.school.hei.entity.JGroup;
import com.school.hei.mapper.GroupMapper;
import com.school.hei.model.Group;
import com.school.hei.repository.GroupRepository;
import com.school.hei.validator.GroupValidator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class GroupService {

  private final GroupRepository groupRepository;
  private final GroupValidator groupValidator;

  public List<Group> findAll() {
    return groupRepository.findAll().stream().map(GroupMapper::toModel).toList();
  }

  public Group findById(UUID id) {
    return groupRepository
        .findById(id)
        .map(GroupMapper::toModel)
        .orElseThrow(
            () ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "group not found with id " + id));
  }

  public Group save(Group group) {
    groupValidator.accept(group);

    JGroup entity = GroupMapper.toEntity(group);
    return GroupMapper.toModel(groupRepository.save(entity));
  }

  public Group update(UUID id, Group group) {
    findById(id);

    group.setId(id);
    groupValidator.accept(group);

    JGroup entity = GroupMapper.toEntity(group);
    return GroupMapper.toModel(groupRepository.save(entity));
  }

  public void delete(UUID id) {
    if (!groupRepository.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "group not found with id " + id);
    }

    groupRepository.deleteById(id);
  }
}
