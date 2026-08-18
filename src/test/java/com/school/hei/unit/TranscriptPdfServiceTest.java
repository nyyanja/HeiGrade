package com.school.hei.unit;

import static org.assertj.core.api.Assertions.assertThat;

import com.school.hei.model.Transcript;
import com.school.hei.model.TranscriptCourseLine;
import com.school.hei.service.services.TranscriptPdfService;
import java.io.File;
import java.util.List;
import org.junit.jupiter.api.Test;

class TranscriptPdfServiceTest {

  private final TranscriptPdfService transcriptPdfService = new TranscriptPdfService();

  @Test
  void should_generate_pdf_with_courses() {
    TranscriptCourseLine course =
        TranscriptCourseLine.builder()
            .reference("CS101")
            .title("Programming")
            .average(15.0)
            .obtainedCredit(60)
            .build();

    Transcript transcript =
        Transcript.builder()
            .reference("STD001")
            .firstName("John")
            .lastName("Doe")
            .level(1)
            .generalAverage(15.0)
            .totalCredit(60)
            .courses(List.of(course))
            .build();

    File pdf = transcriptPdfService.generate(transcript);

    assertThat(pdf).isNotNull();
    assertThat(pdf.exists()).isTrue();
    assertThat(pdf.length()).isGreaterThan(0);

    pdf.delete();
  }

  @Test
  void should_generate_pdf_with_empty_course_list() {
    Transcript transcript =
        Transcript.builder()
            .reference("STD001")
            .firstName("John")
            .lastName("Doe")
            .level(1)
            .generalAverage(14.5)
            .totalCredit(60)
            .courses(List.of())
            .build();

    File pdf = transcriptPdfService.generate(transcript);

    assertThat(pdf).isNotNull();
    assertThat(pdf.exists()).isTrue();
    assertThat(pdf.length()).isGreaterThan(0);

    pdf.delete();
  }

  @Test
  void should_generate_pdf_when_courses_are_null() {
    Transcript transcript =
        Transcript.builder()
            .reference("STD001")
            .firstName("John")
            .lastName("Doe")
            .level(1)
            .generalAverage(14.5)
            .totalCredit(60)
            .courses(null)
            .build();

    File pdf = transcriptPdfService.generate(transcript);

    assertThat(pdf).isNotNull();
    assertThat(pdf.exists()).isTrue();
    assertThat(pdf.length()).isGreaterThan(0);

    pdf.delete();
  }
}
