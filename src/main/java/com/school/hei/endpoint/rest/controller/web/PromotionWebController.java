package com.school.hei.endpoint.rest.controller.web;

import com.school.hei.model.Promotion;
import com.school.hei.service.services.GraduateExportService;
import com.school.hei.service.services.PromotionService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequiredArgsConstructor
@RequestMapping("/web/promotions")
public class PromotionWebController {

  private final PromotionService promotionService;
  private final GraduateExportService graduateExportService;

  @GetMapping
  public String listPromotions(Model model) {
    List<Promotion> promotions = promotionService.findAll();
    model.addAttribute("promotions", promotions);
    return "promotions/list";
  }

  @GetMapping("/{promotionId}/graduates/download")
  public RedirectView downloadGraduates(@PathVariable UUID promotionId) {
    String downloadUrl = graduateExportService.exportGraduatesToExcel(promotionId);
    return new RedirectView(downloadUrl);
  }
}

