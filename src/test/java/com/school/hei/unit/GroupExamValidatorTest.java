package com.school.hei.unit;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.school.hei.entity.JGroupExam;
import com.school.hei.model.Exam;
import com.school.hei.model.Group;
import com.school.hei.model.GroupExam;
import com.school.hei.repository.ExamRepository;
import com.school.hei.repository.GroupExamRepository;
import com.school.hei.repository.GroupRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import com.school.hei.validator.GroupExamValidator;

@ExtendWith(MockitoExtension.class)
class GroupExamValidatorTest {

    @Mock private GroupRepository groupRepository;
    @Mock private ExamRepository examRepository;
    @Mock private GroupExamRepository groupExamRepository;

    private GroupExamValidator validator;

    private UUID groupId;
    private UUID examId;

    @BeforeEach
    void setUp() {
        validator = new GroupExamValidator(groupRepository, examRepository, groupExamRepository);
        groupId = UUID.randomUUID();
        examId = UUID.randomUUID();
    }

    private GroupExam validGroupExam() {
        return GroupExam.builder()
                .group(Group.builder().id(groupId).build())
                .exam(Exam.builder().id(examId).build())
                .build();
    }

    @Test
    void should_accept_valid_group_exam() {
        GroupExam groupExam = validGroupExam();
        when(groupRepository.existsById(groupId)).thenReturn(true);
        when(examRepository.existsById(examId)).thenReturn(true);
        when(groupExamRepository.findByGroup_IdAndExam_Id(groupId, examId))
                .thenReturn(Optional.empty());

        assertThatCode(() -> validator.accept(groupExam)).doesNotThrowAnyException();
    }

    @Test
    void should_reject_null_group_exam() {
        assertThatThrownBy(() -> validator.accept(null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("group exam cannot be null");
    }

    @Test
    void should_reject_null_group() {
        GroupExam groupExam = validGroupExam();
        groupExam.setGroup(null);

        assertThatThrownBy(() -> validator.accept(groupExam))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("group is required");
    }

    @Test
    void should_reject_null_exam() {
        GroupExam groupExam = validGroupExam();
        groupExam.setExam(null);

        assertThatThrownBy(() -> validator.accept(groupExam))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("exam is required");
    }

    @Test
    void should_reject_when_group_not_found() {
        GroupExam groupExam = validGroupExam();
        when(groupRepository.existsById(groupId)).thenReturn(false);

        assertThatThrownBy(() -> validator.accept(groupExam))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("group not found");
    }

    @Test
    void should_reject_when_exam_not_found() {
        GroupExam groupExam = validGroupExam();
        when(groupRepository.existsById(groupId)).thenReturn(true);
        when(examRepository.existsById(examId)).thenReturn(false);

        assertThatThrownBy(() -> validator.accept(groupExam))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("exam not found");
    }

    @Test
    void should_reject_when_association_already_exists() {
        GroupExam groupExam = validGroupExam();
        JGroupExam existing = new JGroupExam();
        existing.setId(UUID.randomUUID());
        when(groupRepository.existsById(groupId)).thenReturn(true);
        when(examRepository.existsById(examId)).thenReturn(true);
        when(groupExamRepository.findByGroup_IdAndExam_Id(groupId, examId))
                .thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> validator.accept(groupExam))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("this exam is already linked to this group");
    }
}
