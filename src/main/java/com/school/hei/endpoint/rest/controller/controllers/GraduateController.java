package com.school.hei.endpoint.rest.controller.controllers;

import com.school.hei.model.GraduateRanking;
import com.school.hei.model.GraduateStatus;
import com.school.hei.service.services.GraduateService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/graduates")
public class GraduateController {

  private final GraduateService graduateService;

  @GetMapping("/student/{studentId}")
  public GraduateStatus getStatus(@PathVariable UUID studentId) {
    return graduateService.getGraduateStatus(studentId);
  }

  @GetMapping("/promotion/{promotionId}")
  public List<GraduateRanking> getByPromotion(@PathVariable UUID promotionId) {
    return graduateService.getGraduatesByPromotion(promotionId);
  }
}


