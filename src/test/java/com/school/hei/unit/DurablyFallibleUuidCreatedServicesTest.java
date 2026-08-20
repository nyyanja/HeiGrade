package com.school.hei.unit;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

import com.school.hei.endpoint.event.model.DurablyFallibleUuidCreated1;
import com.school.hei.endpoint.event.model.DurablyFallibleUuidCreated2;
import com.school.hei.endpoint.event.model.UuidCreated;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.school.hei.service.event.DurablyFallibleUuidCreated1Service;
import com.school.hei.service.event.DurablyFallibleUuidCreated2Service;
import com.school.hei.service.event.UuidCreatedService;

@ExtendWith(MockitoExtension.class)
class DurablyFallibleUuidCreatedServicesTest {

    @Mock private UuidCreatedService uuidCreatedService;

    private DurablyFallibleUuidCreated1Service service1;
    private DurablyFallibleUuidCreated2Service service2;

    @BeforeEach
    void setUp() {
        service1 = new DurablyFallibleUuidCreated1Service(uuidCreatedService);
        service2 = new DurablyFallibleUuidCreated2Service(uuidCreatedService);
    }

    private UuidCreated uuidCreated() {
        return UuidCreated.builder().uuid("uuid-1").build();
    }

    @Test
    void service1_should_delegate_to_uuid_created_service_when_not_failing() {
        DurablyFallibleUuidCreated1 event =
                DurablyFallibleUuidCreated1.builder()
                        .uuidCreated(uuidCreated())
                        .waitDurationBeforeConsumingInSeconds(0)
                        .failureRate(0.0)
                        .build();

        assertThatCode(() -> service1.accept(event)).doesNotThrowAnyException();

        verify(uuidCreatedService).accept(event.getUuidCreated());
    }

    @Test
    void service1_should_throw_when_failure_rate_is_max() {
        DurablyFallibleUuidCreated1 event =
                DurablyFallibleUuidCreated1.builder()
                        .uuidCreated(uuidCreated())
                        .waitDurationBeforeConsumingInSeconds(0)
                        .failureRate(1.0)
                        .build();

        assertThatThrownBy(() -> service1.accept(event))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Oops, random fail!");
    }

    @Test
    void service2_should_delegate_to_uuid_created_service_when_not_failing() {
        DurablyFallibleUuidCreated2 event =
                DurablyFallibleUuidCreated2.builder()
                        .uuidCreated(uuidCreated())
                        .waitDurationBeforeConsumingInSeconds(0)
                        .failureRate(0.0)
                        .build();

        assertThatCode(() -> service2.accept(event)).doesNotThrowAnyException();

        verify(uuidCreatedService).accept(event.getUuidCreated());
    }

    @Test
    void service2_should_throw_when_failure_rate_is_max() {
        DurablyFallibleUuidCreated2 event =
                DurablyFallibleUuidCreated2.builder()
                        .uuidCreated(uuidCreated())
                        .waitDurationBeforeConsumingInSeconds(0)
                        .failureRate(1.0)
                        .build();

        assertThatThrownBy(() -> service2.accept(event))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Oops, random fail!");
    }
}
