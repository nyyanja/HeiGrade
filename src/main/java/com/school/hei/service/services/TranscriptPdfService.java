package com.school.hei.service.services;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.school.hei.model.Transcript;
import com.school.hei.model.TranscriptCourseLine;
import java.io.File;
import java.io.FileOutputStream;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

@Service
public class TranscriptPdfService {

  @SneakyThrows
  public File generate(Transcript transcript) {
    File file =
        File.createTempFile(
            "releve-" + transcript.getReference() + "-L" + transcript.getLevel() + "-", ".pdf");

    Document document = new Document();
    PdfWriter.getInstance(document, new FileOutputStream(file));
    document.open();

    document.add(new Paragraph("Relevé de notes - L" + transcript.getLevel()));
    document.add(
        new Paragraph(
            transcript.getLastName()
                + " "
                + transcript.getFirstName()
                + " ("
                + transcript.getReference()
                + ")"));
    document.add(new Paragraph(" "));

    if (transcript.getCourses() != null) {
      for (TranscriptCourseLine line : transcript.getCourses()) {
        document.add(
            new Paragraph(
                line.getReference()
                    + " - "
                    + line.getTitle()
                    + " | moy="
                    + line.getAverage()
                    + " | crédit obtenu="
                    + line.getObtainedCredit()));
      }
    }

    document.add(new Paragraph(" "));
    document.add(new Paragraph("Moyenne générale : " + transcript.getGeneralAverage()));
    document.add(new Paragraph("Crédits obtenus : " + transcript.getTotalCredit()));

    document.close();
    return file;
  }
}

