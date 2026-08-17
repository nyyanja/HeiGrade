package com.school.hei.endpoint.rest.controller.controllers;

import com.school.hei.model.Promotion;
import com.school.hei.service.services.PromotionService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/promotions")
@RequiredArgsConstructor
public class PromotionController {

  private final PromotionService promotionService;

  @GetMapping
  public List<Promotion> findAll() {
    return promotionService.findAll();
  }

  @GetMapping("/{id}")
  public Promotion findById(@PathVariable UUID id) {
    return promotionService.findById(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Promotion save(@RequestBody Promotion promotion) {
    return promotionService.save(promotion);
  }

  @PutMapping("/{id}")
  public Promotion update(@PathVariable UUID id, @RequestBody Promotion promotion) {
    return promotionService.update(id, promotion);
  }

  @GetMapping("/year/{year}")
  public List<Promotion> findByYear(@PathVariable Integer year) {
    return promotionService.findByYear(year);
  }

  @GetMapping("/name/{name}")
  public List<Promotion> findByName(@PathVariable String name) {
    return promotionService.findByName(name);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    promotionService.delete(id);
  }
}


