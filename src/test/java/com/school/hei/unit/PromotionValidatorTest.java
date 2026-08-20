package com.school.hei.unit;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.school.hei.model.Promotion;
import java.time.Year;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import com.school.hei.validator.PromotionValidator;

class PromotionValidatorTest {

    private final PromotionValidator validator = new PromotionValidator();

    private Promotion validPromotion() {
        return Promotion.builder().name("Promo 2026").year(Year.now().getValue()).build();
    }

    @Test
    void should_accept_valid_promotion() {
        assertThatCode(() -> validator.accept(validPromotion())).doesNotThrowAnyException();
    }

    @Test
    void should_reject_null_promotion() {
        assertThatThrownBy(() -> validator.accept(null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("promotion cannot be null");
    }

    @Test
    void should_reject_null_name() {
        Promotion promotion = validPromotion();
        promotion.setName(null);

        assertThatThrownBy(() -> validator.accept(promotion))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("promotion name is required");
    }

    @Test
    void should_reject_blank_name() {
        Promotion promotion = validPromotion();
        promotion.setName("   ");

        assertThatThrownBy(() -> validator.accept(promotion))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("promotion name is required");
    }

    @Test
    void should_reject_null_year() {
        Promotion promotion = validPromotion();
        promotion.setYear(null);

        assertThatThrownBy(() -> validator.accept(promotion))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("promotion year is required");
    }

    @Test
    void should_reject_year_too_old() {
        Promotion promotion = validPromotion();
        promotion.setYear(1999);

        assertThatThrownBy(() -> validator.accept(promotion))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("invalid promotion year");
    }

    @Test
    void should_reject_year_too_far_in_future() {
        Promotion promotion = validPromotion();
        promotion.setYear(Year.now().getValue() + 2);

        assertThatThrownBy(() -> validator.accept(promotion))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("invalid promotion year");
    }
}
