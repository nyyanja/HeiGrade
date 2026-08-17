package com.school.hei.service.event;

import com.school.hei.endpoint.event.model.SendTranscriptRequested;
import com.school.hei.entity.JStudent;
import com.school.hei.file.bucket.BucketComponent;
import com.school.hei.mail.Email;
import com.school.hei.mail.Mailer;
import com.school.hei.model.Transcript;
import com.school.hei.repository.StudentRepository;
import com.school.hei.service.services.TranscriptPdfService;
import com.school.hei.service.services.TranscriptService;
import jakarta.mail.internet.InternetAddress;
import java.io.File;
import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class SendTranscriptRequestedService implements Consumer<SendTranscriptRequested> {

  private final StudentRepository studentRepository;
  private final TranscriptService transcriptService;
  private final TranscriptPdfService transcriptPdfService;
  private final BucketComponent bucketComponent;
  private final Mailer mailer;

  @Override
  @SneakyThrows
  public void accept(SendTranscriptRequested event) {

    UUID studentId = event.getStudentId();
    Integer level = event.getLevel();

    JStudent student =
        studentRepository
            .findById(studentId)
            .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

    Transcript transcript = transcriptService.getStudentTranscriptForSystem(studentId, level);

    File pdfFile = null;

    try {
      pdfFile = transcriptPdfService.generate(transcript);

      String bucketKey =
          "transcripts/"
              + student.getReference()
              + "/L"
              + level
              + "/releve-"
              + student.getReference()
              + "-L"
              + level
              + ".pdf";

      bucketComponent.upload(pdfFile, bucketKey);

      URL downloadUrl = bucketComponent.presign(bucketKey, Duration.ofHours(24));

      sendEmail(student, transcript, downloadUrl);

      log.info("Transcript sent successfully to student {} for level {}", studentId, level);

    } finally {
      if (pdfFile != null && pdfFile.exists()) {
        if (!pdfFile.delete()) {
          log.warn("Unable to delete temporary PDF {}", pdfFile.getAbsolutePath());
        }
      }
    }
  }

  @SneakyThrows
  private void sendEmail(JStudent student, Transcript transcript, URL downloadUrl) {

    InternetAddress recipient = new InternetAddress(student.getEmail());

    String htmlBody = buildEmailBody(student, transcript, downloadUrl);

    Email email =
        new Email(
            recipient,
            List.of(),
            List.of(),
            "Votre relevé de notes - L" + transcript.getLevel(),
            htmlBody,
            List.of());

    mailer.accept(email);
  }

  private String buildEmailBody(JStudent student, Transcript transcript, URL downloadUrl) {

    return """
           <html>
             <body>
               <h2>Votre relevé de notes</h2>

               <p>
                 Bonjour %s %s,
               </p>

               <p>
                 Votre relevé de notes pour le niveau <strong>L%d</strong>
                 est disponible.
               </p>

               <p>
                 <strong>Référence étudiant :</strong> %s
               </p>

               <p>
                 <strong>Moyenne générale :</strong> %.2f
               </p>

               <p>
                 <strong>Crédits obtenus :</strong> %d
               </p>

               <p>
                 <a href="%s">
                   Télécharger mon relevé de notes
                 </a>
               </p>

               <p>
                 Ce lien est valable pendant 24 heures.
               </p>

               <p>
                 Cordialement,<br>
                 HEI
               </p>
             </body>
           </html>
           """
        .formatted(
            student.getFirstName(),
            student.getLastName() == null ? "" : student.getLastName(),
            transcript.getLevel(),
            transcript.getReference(),
            transcript.getGeneralAverage(),
            transcript.getTotalCredit(),
            downloadUrl);
  }
}
