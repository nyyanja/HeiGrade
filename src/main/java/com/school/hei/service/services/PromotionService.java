package com.school.hei.service.services;

import com.school.hei.entity.JPromotion;
import com.school.hei.mapper.PromotionMapper;
import com.school.hei.model.Promotion;
import com.school.hei.repository.PromotionRepository;
import com.school.hei.validator.PromotionValidator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class PromotionService {

  private final PromotionRepository promotionRepository;
  private final PromotionValidator promotionValidator;

  public List<Promotion> findAll() {
    return promotionRepository.findAll().stream().map(PromotionMapper::toModel).toList();
  }

  public Promotion findById(UUID id) {
    return promotionRepository
        .findById(id)
        .map(PromotionMapper::toModel)
        .orElseThrow(
            () ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "promotion not found with id " + id));
  }

  public Promotion save(Promotion promotion) {
    promotionValidator.accept(promotion);

    JPromotion entity = PromotionMapper.toEntity(promotion);
    return PromotionMapper.toModel(promotionRepository.save(entity));
  }

  public Promotion update(UUID id, Promotion promotion) {
    findById(id);

    promotion.setId(id);
    promotionValidator.accept(promotion);

    JPromotion entity = PromotionMapper.toEntity(promotion);
    return PromotionMapper.toModel(promotionRepository.save(entity));
  }

  public void delete(UUID id) {
    if (!promotionRepository.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "promotion not found with id " + id);
    }

    promotionRepository.deleteById(id);
  }
}
