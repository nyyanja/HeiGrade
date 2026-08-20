package com.school.hei.validator;

import com.school.hei.model.Promotion;
import java.time.Year;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class PromotionValidator implements SaveValidator<Promotion> {

  @Override
  public void accept(Promotion promotion) {
    if (promotion == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "promotion cannot be null");
    }
    if (promotion.getName() == null || promotion.getName().isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "promotion name is required");
    }
    if (promotion.getYear() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "promotion year is required");
    }
    int currentYear = Year.now().getValue();
    if (promotion.getYear() < 2000 || promotion.getYear() > currentYear + 1) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid promotion year");
    }
  }
}

