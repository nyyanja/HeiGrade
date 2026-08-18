package com.school.hei.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.school.hei.endpoint.event.model.SendTranscriptRequested;
import com.school.hei.entity.JStudent;
import com.school.hei.file.bucket.BucketComponent;
import com.school.hei.mail.Email;
import com.school.hei.mail.Mailer;
import com.school.hei.model.Transcript;
import com.school.hei.repository.StudentRepository;
import com.school.hei.service.event.SendTranscriptRequestedService;
import com.school.hei.service.services.TranscriptPdfService;
import com.school.hei.service.services.TranscriptService;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SendTranscriptRequestedServiceTest {

  @Mock private StudentRepository studentRepository;

  @Mock private TranscriptService transcriptService;

  @Mock private TranscriptPdfService transcriptPdfService;

  @Mock private BucketComponent bucketComponent;

  @Mock private Mailer mailer;

  @InjectMocks private SendTranscriptRequestedService service;

  private UUID studentId;
  private JStudent student;
  private Transcript transcript;

  @BeforeEach
  void setUp() {
    studentId = UUID.randomUUID();

    student =
        JStudent.builder()
            .id(studentId)
            .reference("STD001")
            .firstName("John")
            .lastName("Student")
            .email("student@heigrade.com")
            .build();

    transcript =
        Transcript.builder()
            .studentId(studentId)
            .reference("STD001")
            .firstName("John")
            .lastName("Student")
            .level(1)
            .generalAverage(14.5)
            .totalCredit(60)
            .courses(List.of())
            .build();
  }

  @Test
  void should_send_transcript_by_email() throws Exception {
    SendTranscriptRequested event =
        SendTranscriptRequested.builder().studentId(studentId).level(1).build();

    File pdfFile = Files.createTempFile("transcript-test-", ".pdf").toFile();
    URL downloadUrl = new URL("https://example.com/transcript.pdf");

    when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
    when(transcriptService.getStudentTranscriptForSystem(studentId, 1)).thenReturn(transcript);
    when(transcriptPdfService.generate(transcript)).thenReturn(pdfFile);
    when(bucketComponent.presign(
            eq("transcripts/STD001/L1/releve-STD001-L1.pdf"), any(Duration.class)))
        .thenReturn(downloadUrl);

    service.accept(event);

    verify(studentRepository).findById(studentId);
    verify(transcriptService).getStudentTranscriptForSystem(studentId, 1);
    verify(transcriptPdfService).generate(transcript);
    verify(bucketComponent).upload(eq(pdfFile), eq("transcripts/STD001/L1/releve-STD001-L1.pdf"));
    verify(bucketComponent)
        .presign(eq("transcripts/STD001/L1/releve-STD001-L1.pdf"), eq(Duration.ofHours(24)));

    ArgumentCaptor<Email> emailCaptor = ArgumentCaptor.forClass(Email.class);
    verify(mailer).accept(emailCaptor.capture());

    Email email = emailCaptor.getValue();

    assertThat(email.to()).isNotNull();
    assertThat(email.to().toString()).isEqualTo("student@heigrade.com");

    assertThat(email.subject()).contains("L1");
    assertThat(email.htmlBody()).contains("John");
    assertThat(email.htmlBody()).contains("STD001");
    assertThat(email.htmlBody()).contains("Moyenne");
    assertThat(email.htmlBody()).contains("14,50");
    assertThat(email.htmlBody()).contains("60");
    assertThat(email.htmlBody()).contains(downloadUrl.toString());

    assertThat(pdfFile).doesNotExist();
  }

  @Test
  void should_throw_when_student_does_not_exist() {
    SendTranscriptRequested event =
        SendTranscriptRequested.builder().studentId(studentId).level(1).build();

    when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.accept(event))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Student not found");

    verify(studentRepository).findById(studentId);
    verifyNoInteractions(transcriptService, transcriptPdfService, bucketComponent, mailer);
  }

  @Test
  void should_delete_temporary_pdf_when_sending_fails() throws Exception {
    SendTranscriptRequested event =
        SendTranscriptRequested.builder().studentId(studentId).level(1).build();

    File pdfFile = Files.createTempFile("transcript-test-", ".pdf").toFile();
    URL downloadUrl = new URL("https://example.com/transcript.pdf");

    when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
    when(transcriptService.getStudentTranscriptForSystem(studentId, 1)).thenReturn(transcript);
    when(transcriptPdfService.generate(transcript)).thenReturn(pdfFile);
    when(bucketComponent.presign(any(), any(Duration.class))).thenReturn(downloadUrl);
    doThrow(new RuntimeException("mail sending failed")).when(mailer).accept(any(Email.class));

    assertThatThrownBy(() -> service.accept(event))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("mail sending failed");

    assertThat(pdfFile).doesNotExist();
  }

  @Test
  void should_delete_temporary_pdf_when_upload_fails() throws Exception {
    SendTranscriptRequested event =
        SendTranscriptRequested.builder().studentId(studentId).level(1).build();

    File pdfFile = Files.createTempFile("transcript-test-", ".pdf").toFile();

    when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
    when(transcriptService.getStudentTranscriptForSystem(studentId, 1)).thenReturn(transcript);
    when(transcriptPdfService.generate(transcript)).thenReturn(pdfFile);

    doThrow(new RuntimeException("upload failed"))
        .when(bucketComponent)
        .upload(any(File.class), any(String.class));

    assertThatThrownBy(() -> service.accept(event))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("upload failed");

    assertThat(pdfFile).doesNotExist();

    verify(mailer, never()).accept(any(Email.class));
  }
}
