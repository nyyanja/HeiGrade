package com.school.hei.validator;

import com.school.hei.model.GroupExam;
import com.school.hei.repository.ExamRepository;
import com.school.hei.repository.GroupExamRepository;
import com.school.hei.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class GroupExamValidator implements SaveValidator<GroupExam> {

    private final GroupRepository groupRepository;
    private final ExamRepository examRepository;
    private final GroupExamRepository groupExamRepository;

    @Override
    public void accept(GroupExam groupExam) {
        if (groupExam == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "the parameter cannot be null");
        }
        if (groupExam.getGroup() == null || groupExam.getGroup().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "the group id cannot be null");
        }
        if (groupExam.getExam() == null || groupExam.getExam().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "the exam id cannot be null");
        }
        if (!groupRepository.existsById(groupExam.getGroup().getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "group with id " + groupExam.getGroup().getId() + " is not found");
        }
        if (!examRepository.existsById(groupExam.getExam().getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "group with id " + groupExam.getExam().getId() + " is not found");
        }

        groupExamRepository
                .findByGroup_IdAndExam_Id(groupExam.getGroup().getId(), groupExam.getExam().getId())
                .ifPresent(
                        existing -> {
                            boolean isSame = groupExam.getId() != null && existing.getId().equals(groupExam.getId());
                            if (!isSame) {
                                throw new ResponseStatusException(HttpStatus.CONFLICT, "this group exam already exists");
                            }
                        });
    }
}