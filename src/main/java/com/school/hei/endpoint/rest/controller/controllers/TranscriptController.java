package com.school.hei.endpoint.rest.controller.controllers;

import com.school.hei.endpoint.event.EventProducer;
import com.school.hei.endpoint.event.model.SendTranscriptRequested;
import com.school.hei.model.Transcript;
import com.school.hei.service.services.TranscriptService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/transcripts")
public class TranscriptController {

  private final TranscriptService transcriptService;

  @GetMapping("/student/{studentId}/L1")
  public Transcript getL1Transcript(@PathVariable UUID studentId) {

    return transcriptService.getStudentTranscript(studentId, 1);
  }

  @GetMapping("/student/{studentId}/L2")
  public Transcript getL2Transcript(@PathVariable UUID studentId) {

    return transcriptService.getStudentTranscript(studentId, 2);
  }

  @GetMapping("/student/{studentId}/L3")
  public Transcript getL3Transcript(@PathVariable UUID studentId) {

    return transcriptService.getStudentTranscript(studentId, 3);
  }

  @GetMapping("/L1")
  public List<Transcript> getAllL1Transcripts() {

    return transcriptService.getAllTranscripts(1);
  }

  @GetMapping("/L2")
  public List<Transcript> getAllL2Transcripts() {

    return transcriptService.getAllTranscripts(2);
  }

  @GetMapping("/L3")
  public List<Transcript> getAllL3Transcripts() {

    return transcriptService.getAllTranscripts(3);
  }
  private final EventProducer<SendTranscriptRequested> eventProducer;

  @PostMapping("/student/{studentId}/L{level}/send-email")
  public String sendTranscriptByEmail(
          @PathVariable UUID studentId,
          @PathVariable Integer level) {


    eventProducer.accept(
            List.of(
                    SendTranscriptRequested.builder()
                            .studentId(studentId)
                            .level(level)
                            .build()));

    return "Transcript email requested for student " + studentId + " level L" + level;
  }
}
