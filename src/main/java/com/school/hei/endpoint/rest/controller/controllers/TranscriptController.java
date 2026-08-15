package com.school.hei.endpoint.rest.controller.controllers;

import com.school.hei.model.Transcript;
import com.school.hei.service.services.TranscriptService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transcripts")
@RequiredArgsConstructor
public class TranscriptController {

  private final TranscriptService transcriptService;

  @GetMapping
  public List<Transcript> getAllCompletedTranscripts() {
    return transcriptService.getAllCompletedTranscripts();
  }

  @GetMapping("/student/{studentId}")
  public Transcript getStudentTranscript(@PathVariable UUID studentId) {

    return transcriptService.getStudentTranscript(studentId);
  }

  @GetMapping("/group/{groupId}")
  public List<Transcript> getGroupTranscripts(@PathVariable UUID groupId) {

    return transcriptService.getGroupTranscripts(groupId);
  }
}
