package com.school.hei.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.school.hei.entity.JGroup;
import com.school.hei.entity.JStudent;
import com.school.hei.model.GraduateRanking;
import com.school.hei.model.GraduateStatus;
import com.school.hei.model.Transcript;
import com.school.hei.repository.GroupRepository;
import com.school.hei.repository.PromotionRepository;
import com.school.hei.repository.StudentRepository;
import com.school.hei.service.services.GraduateService;
import com.school.hei.service.services.TranscriptService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class GraduateServiceTest {

    @Mock private TranscriptService transcriptService;
    @Mock private StudentRepository studentRepository;
    @Mock private GroupRepository groupRepository;
    @Mock private PromotionRepository promotionRepository;

    @InjectMocks private GraduateService graduateService;

    private UUID studentId;
    private UUID promotionId;
    private UUID groupId;

    private JStudent student;
    private JGroup group;

    @BeforeEach
    void setUp() {
        studentId = UUID.randomUUID();
        promotionId = UUID.randomUUID();
        groupId = UUID.randomUUID();

        student =
                JStudent.builder()
                        .id(studentId)
                        .reference("STU-001")
                        .firstName("John")
                        .lastName("Doe")
                        .build();

        group =
                JGroup.builder()
                        .id(groupId)
                        .name("Group K1")
                        .build();
    }

    // ============================================================
    // GET GRADUATE STATUS
    // ============================================================

    @Test
    void should_return_graduate_status_when_student_completed_three_years() {
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));

        Transcript l1 =
                Transcript.builder()
                        .studentId(studentId)
                        .level(1)
                        .generalAverage(14.0)
                        .totalCredit(60)
                        .build();

        Transcript l2 =
                Transcript.builder()
                        .studentId(studentId)
                        .level(2)
                        .generalAverage(15.0)
                        .totalCredit(60)
                        .build();

        Transcript l3 =
                Transcript.builder()
                        .studentId(studentId)
                        .level(3)
                        .generalAverage(16.0)
                        .totalCredit(60)
                        .build();

        when(transcriptService.getStudentTranscript(studentId, 1)).thenReturn(l1);
        when(transcriptService.getStudentTranscript(studentId, 2)).thenReturn(l2);
        when(transcriptService.getStudentTranscript(studentId, 3)).thenReturn(l3);

        GraduateStatus result = graduateService.getGraduateStatus(studentId);

        assertThat(result).isNotNull();
        assertThat(result.getStudentId()).isEqualTo(studentId);
        assertThat(result.getReference()).isEqualTo("STU-001");
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");

        assertThat(result.isGraduated()).isTrue();
        assertThat(result.getTotalCredit()).isEqualTo(180);

        assertThat(result.getL1Credit()).isEqualTo(60);
        assertThat(result.getL2Credit()).isEqualTo(60);
        assertThat(result.getL3Credit()).isEqualTo(60);

        assertThat(result.getL1Average()).isEqualTo(14.0);
        assertThat(result.getL2Average()).isEqualTo(15.0);
        assertThat(result.getL3Average()).isEqualTo(16.0);

        assertThat(result.getGeneralAverage()).isEqualTo(15.0);

        verify(studentRepository).findById(studentId);
        verify(transcriptService).getStudentTranscript(studentId, 1);
        verify(transcriptService).getStudentTranscript(studentId, 2);
        verify(transcriptService).getStudentTranscript(studentId, 3);
    }

    @Test
    void should_return_not_graduated_when_total_credits_are_less_than_180() {
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));

        Transcript l1 =
                Transcript.builder()
                        .studentId(studentId)
                        .level(1)
                        .generalAverage(12.0)
                        .totalCredit(60)
                        .build();

        Transcript l2 =
                Transcript.builder()
                        .studentId(studentId)
                        .level(2)
                        .generalAverage(11.0)
                        .totalCredit(50)
                        .build();

        when(transcriptService.getStudentTranscript(studentId, 1)).thenReturn(l1);
        when(transcriptService.getStudentTranscript(studentId, 2)).thenReturn(l2);

        when(transcriptService.getStudentTranscript(studentId, 3))
                .thenThrow(
                        new ResponseStatusException(
                                HttpStatus.BAD_REQUEST, "year not completed"));

        GraduateStatus result = graduateService.getGraduateStatus(studentId);

        assertThat(result.isGraduated()).isFalse();
        assertThat(result.getTotalCredit()).isEqualTo(110);
        assertThat(result.getL1Credit()).isEqualTo(60);
        assertThat(result.getL2Credit()).isEqualTo(50);
        assertThat(result.getL3Credit()).isZero();
        assertThat(result.getGeneralAverage()).isNull();
    }

    @Test
    void should_return_zero_credits_when_no_transcript_exists() {
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));

        when(transcriptService.getStudentTranscript(any(), anyInt()))
                .thenThrow(
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "transcript not found"));

        GraduateStatus result = graduateService.getGraduateStatus(studentId);

        assertThat(result.isGraduated()).isFalse();
        assertThat(result.getTotalCredit()).isZero();
        assertThat(result.getL1Credit()).isZero();
        assertThat(result.getL2Credit()).isZero();
        assertThat(result.getL3Credit()).isZero();
        assertThat(result.getGeneralAverage()).isNull();
    }

    @Test
    void should_throw_when_student_does_not_exist() {
        when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> graduateService.getGraduateStatus(studentId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("student not found");

        verifyNoInteractions(transcriptService);
    }

    // ============================================================
    // GET GRADUATES BY PROMOTION
    // ============================================================

    @Test
    void should_return_graduates_sorted_by_average() {
        UUID studentId2 = UUID.randomUUID();

        JStudent student2 =
                JStudent.builder()
                        .id(studentId2)
                        .reference("STU-002")
                        .firstName("Jane")
                        .lastName("Smith")
                        .group(group)
                        .build();

        student.setGroup(group);

        when(promotionRepository.existsById(promotionId)).thenReturn(true);
        when(groupRepository.findByPromotion_Id(promotionId))
                .thenReturn(List.of(group));

        when(studentRepository.findByGroup_Id(groupId))
                .thenReturn(List.of(student, student2));

        GraduateStatus status1 =
                GraduateStatus.builder()
                        .studentId(studentId)
                        .reference("STU-001")
                        .firstName("John")
                        .lastName("Doe")
                        .graduated(true)
                        .totalCredit(180)
                        .generalAverage(14.0)
                        .build();

        GraduateStatus status2 =
                GraduateStatus.builder()
                        .studentId(studentId2)
                        .reference("STU-002")
                        .firstName("Jane")
                        .lastName("Smith")
                        .graduated(true)
                        .totalCredit(180)
                        .generalAverage(16.0)
                        .build();

        GraduateService spyService = spy(graduateService);

        doReturn(status1).when(spyService).getGraduateStatus(studentId);
        doReturn(status2).when(spyService).getGraduateStatus(studentId2);

        List<GraduateRanking> result =
                spyService.getGraduatesByPromotion(promotionId);

        assertThat(result).hasSize(2);

        assertThat(result.get(0).getStudentId()).isEqualTo(studentId2);
        assertThat(result.get(0).getGeneralAverage()).isEqualTo(16.0);
        assertThat(result.get(0).getRank()).isEqualTo(1);

        assertThat(result.get(1).getStudentId()).isEqualTo(studentId);
        assertThat(result.get(1).getGeneralAverage()).isEqualTo(14.0);
        assertThat(result.get(1).getRank()).isEqualTo(2);
    }

    @Test
    void should_return_empty_list_when_promotion_has_no_groups() {
        when(promotionRepository.existsById(promotionId)).thenReturn(true);
        when(groupRepository.findByPromotion_Id(promotionId)).thenReturn(List.of());

        List<GraduateRanking> result =
                graduateService.getGraduatesByPromotion(promotionId);

        assertThat(result).isEmpty();

        verifyNoInteractions(studentRepository);
    }

    @Test
    void should_ignore_students_who_are_not_graduated() {
        when(promotionRepository.existsById(promotionId)).thenReturn(true);
        when(groupRepository.findByPromotion_Id(promotionId))
                .thenReturn(List.of(group));

        when(studentRepository.findByGroup_Id(groupId))
                .thenReturn(List.of(student));

        GraduateStatus status =
                GraduateStatus.builder()
                        .studentId(studentId)
                        .graduated(false)
                        .totalCredit(120)
                        .generalAverage(12.0)
                        .build();

        GraduateService spyService = spy(graduateService);
        doReturn(status).when(spyService).getGraduateStatus(studentId);

        List<GraduateRanking> result =
                spyService.getGraduatesByPromotion(promotionId);

        assertThat(result).isEmpty();
    }

    @Test
    void should_ignore_graduates_without_general_average() {
        when(promotionRepository.existsById(promotionId)).thenReturn(true);
        when(groupRepository.findByPromotion_Id(promotionId))
                .thenReturn(List.of(group));

        when(studentRepository.findByGroup_Id(groupId))
                .thenReturn(List.of(student));

        GraduateStatus status =
                GraduateStatus.builder()
                        .studentId(studentId)
                        .graduated(true)
                        .totalCredit(180)
                        .generalAverage(null)
                        .build();

        GraduateService spyService = spy(graduateService);
        doReturn(status).when(spyService).getGraduateStatus(studentId);

        List<GraduateRanking> result =
                spyService.getGraduatesByPromotion(promotionId);

        assertThat(result).isEmpty();
    }

    @Test
    void should_throw_when_promotion_does_not_exist() {
        when(promotionRepository.existsById(promotionId)).thenReturn(false);

        assertThatThrownBy(() -> graduateService.getGraduatesByPromotion(promotionId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("promotion not found");

        verifyNoInteractions(groupRepository);
        verifyNoInteractions(studentRepository);
    }

    @Test
    void should_remove_duplicate_students_from_multiple_groups() {
        UUID groupId2 = UUID.randomUUID();

        JGroup group2 =
                JGroup.builder()
                        .id(groupId2)
                        .name("Group K2")
                        .build();

        when(promotionRepository.existsById(promotionId)).thenReturn(true);

        when(groupRepository.findByPromotion_Id(promotionId))
                .thenReturn(List.of(group, group2));

        when(studentRepository.findByGroup_Id(groupId))
                .thenReturn(List.of(student));

        when(studentRepository.findByGroup_Id(groupId2))
                .thenReturn(List.of(student));

        GraduateStatus status =
                GraduateStatus.builder()
                        .studentId(studentId)
                        .graduated(true)
                        .totalCredit(180)
                        .generalAverage(15.0)
                        .build();

        GraduateService spyService = spy(graduateService);
        doReturn(status).when(spyService).getGraduateStatus(studentId);

        List<GraduateRanking> result =
                spyService.getGraduatesByPromotion(promotionId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStudentId()).isEqualTo(studentId);

        verify(spyService, times(1)).getGraduateStatus(studentId);
    }
}