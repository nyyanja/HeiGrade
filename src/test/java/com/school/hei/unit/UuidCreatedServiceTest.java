package com.school.hei.unit;

import static org.mockito.Mockito.verify;

import com.school.hei.endpoint.event.model.UuidCreated;
import com.school.hei.repository.DummyUuidRepository;
import com.school.hei.repository.model.DummyUuid;
import com.school.hei.service.event.UuidCreatedService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UuidCreatedServiceTest {

  @Mock private DummyUuidRepository dummyUuidRepository;

  private UuidCreatedService service;

  @BeforeEach
  void setUp() {
    service = new UuidCreatedService(dummyUuidRepository);
  }

  @Test
  void should_save_dummy_uuid_from_event() {
    UuidCreated event = UuidCreated.builder().uuid("some-uuid").build();

    service.accept(event);

    ArgumentCaptor<DummyUuid> captor = ArgumentCaptor.forClass(DummyUuid.class);
    verify(dummyUuidRepository).save(captor.capture());
    org.assertj.core.api.Assertions.assertThat(captor.getValue().getId()).isEqualTo("some-uuid");
  }
}
