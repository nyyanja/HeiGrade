package com.school.hei.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.school.hei.entity.JPromotion;
import com.school.hei.model.Promotion;
import com.school.hei.repository.PromotionRepository;
import com.school.hei.service.services.PromotionService;
import com.school.hei.validator.PromotionValidator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class PromotionServiceTest {

    @Mock
    private PromotionRepository promotionRepository;

    @Mock
    private PromotionValidator promotionValidator;

    @InjectMocks
    private PromotionService promotionService;

    @Test
    void should_find_all_promotions() {
        JPromotion promotion1 =
                JPromotion.builder()
                        .id(UUID.randomUUID())
                        .name("L1")
                        .year(2024)
                        .build();

        JPromotion promotion2 =
                JPromotion.builder()
                        .id(UUID.randomUUID())
                        .name("L2")
                        .year(2025)
                        .build();

        when(promotionRepository.findAll()).thenReturn(List.of(promotion1, promotion2));

        List<Promotion> result = promotionService.findAll();

        assertThat(result).hasSize(2);
        verify(promotionRepository).findAll();
    }

    @Test
    void should_find_promotion_by_id() {
        UUID id = UUID.randomUUID();

        JPromotion entity =
                JPromotion.builder()
                        .id(id)
                        .name("L3")
                        .year(2026)
                        .build();

        when(promotionRepository.findById(id)).thenReturn(Optional.of(entity));

        Promotion result = promotionService.findById(id);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getName()).isEqualTo("L3");
    }

    @Test
    void should_throw_when_promotion_not_found() {
        UUID id = UUID.randomUUID();

        when(promotionRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(
                ResponseStatusException.class,
                () -> promotionService.findById(id));
    }

    @Test
    void should_save_promotion() {
        Promotion promotion =
                Promotion.builder()
                        .name("L1")
                        .year(2024)
                        .build();

        JPromotion savedEntity =
                JPromotion.builder()
                        .id(UUID.randomUUID())
                        .name("L1")
                        .year(2024)
                        .build();

        doNothing().when(promotionValidator).accept(promotion);

        when(promotionRepository.save(any(JPromotion.class)))
                .thenReturn(savedEntity);

        Promotion result = promotionService.save(promotion);

        assertThat(result.getId()).isNotNull();

        verify(promotionValidator).accept(promotion);
        verify(promotionRepository).save(any(JPromotion.class));
    }

    @Test
    void should_update_promotion() {
        UUID id = UUID.randomUUID();

        Promotion promotion =
                Promotion.builder()
                        .name("Updated")
                        .year(2025)
                        .build();

        JPromotion existing =
                JPromotion.builder()
                        .id(id)
                        .name("Old")
                        .year(2024)
                        .build();

        JPromotion updated =
                JPromotion.builder()
                        .id(id)
                        .name("Updated")
                        .year(2025)
                        .build();

        when(promotionRepository.findById(id))
                .thenReturn(Optional.of(existing));

        when(promotionRepository.save(any(JPromotion.class)))
                .thenReturn(updated);

        Promotion result = promotionService.update(id, promotion);

        assertThat(result.getName()).isEqualTo("Updated");

        verify(promotionValidator).accept(promotion);
        verify(promotionRepository).save(any(JPromotion.class));
    }

    @Test
    void should_delete_promotion() {
        UUID id = UUID.randomUUID();

        when(promotionRepository.existsById(id)).thenReturn(true);

        promotionService.delete(id);

        verify(promotionRepository).deleteById(id);
    }

    @Test
    void should_throw_when_deleting_unknown_promotion() {
        UUID id = UUID.randomUUID();

        when(promotionRepository.existsById(id)).thenReturn(false);

        assertThrows(
                ResponseStatusException.class,
                () -> promotionService.delete(id));
    }
}