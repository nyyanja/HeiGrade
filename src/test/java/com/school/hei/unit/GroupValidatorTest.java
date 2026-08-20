package com.school.hei.unit;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.school.hei.model.Group;
import com.school.hei.model.Promotion;
import com.school.hei.model.Speciality;
import com.school.hei.repository.PromotionRepository;
import com.school.hei.repository.SpecialityRepository;
import com.school.hei.validator.GroupValidator;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class GroupValidatorTest {

  @Mock private PromotionRepository promotionRepository;
  @Mock private SpecialityRepository specialityRepository;

  private GroupValidator validator;

  private UUID specialityId;
  private UUID promotionId;

  @BeforeEach
  void setUp() {
    validator = new GroupValidator(promotionRepository, specialityRepository);
    specialityId = UUID.randomUUID();
    promotionId = UUID.randomUUID();
  }

  private Group validGroup() {
    return Group.builder()
        .name("Group A")
        .speciality(Speciality.builder().id(specialityId).build())
        .promotion(Promotion.builder().id(promotionId).build())
        .build();
  }

  @Test
  void should_accept_valid_group() {
    Group group = validGroup();
    when(specialityRepository.existsById(specialityId)).thenReturn(true);
    when(promotionRepository.existsById(promotionId)).thenReturn(true);

    assertThatCode(() -> validator.accept(group)).doesNotThrowAnyException();
  }

  @Test
  void should_reject_null_group() {
    assertThatThrownBy(() -> validator.accept(null))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("group cannot be null");
  }

  @Test
  void should_reject_blank_name() {
    Group group = validGroup();
    group.setName("  ");

    assertThatThrownBy(() -> validator.accept(group))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("group name is required");
  }

  @Test
  void should_reject_null_speciality() {
    Group group = validGroup();
    group.setSpeciality(null);

    assertThatThrownBy(() -> validator.accept(group))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("group speciality is required");
  }

  @Test
  void should_reject_when_speciality_not_found() {
    Group group = validGroup();
    when(specialityRepository.existsById(specialityId)).thenReturn(false);

    assertThatThrownBy(() -> validator.accept(group))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("speciality not found");
  }

  @Test
  void should_reject_null_promotion() {
    Group group = validGroup();
    group.setPromotion(null);
    when(specialityRepository.existsById(specialityId)).thenReturn(true);

    assertThatThrownBy(() -> validator.accept(group))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("group promotion is required");
  }

  @Test
  void should_reject_when_promotion_not_found() {
    Group group = validGroup();
    when(specialityRepository.existsById(specialityId)).thenReturn(true);
    when(promotionRepository.existsById(promotionId)).thenReturn(false);

    assertThatThrownBy(() -> validator.accept(group))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("promotion not found");
  }
}
