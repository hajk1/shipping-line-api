package com.shipping.freightops.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.shipping.freightops.enums.AgentType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link AgentResponse} DTO. */
class AgentResponseTest {

  @Nested
  @DisplayName("Getters and setters")
  class GettersAndSetters {

    @Test
    @DisplayName("setId/getId works correctly")
    void idGetterSetter() {
      AgentResponse response = new AgentResponse();
      response.setId(42L);

      assertThat(response.getId()).isEqualTo(42L);
    }

    @Test
    @DisplayName("setName/getName works correctly")
    void nameGetterSetter() {
      AgentResponse response = new AgentResponse();
      response.setName("John Doe");

      assertThat(response.getName()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("setEmail/getEmail works correctly")
    void emailGetterSetter() {
      AgentResponse response = new AgentResponse();
      response.setEmail("john@example.com");

      assertThat(response.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    @DisplayName("setCommissionPercent/getCommissionPercent works correctly")
    void commissionPercentGetterSetter() {
      AgentResponse response = new AgentResponse();
      BigDecimal commission = new BigDecimal("15.75");
      response.setCommissionPercent(commission);

      assertThat(response.getCommissionPercent()).isEqualByComparingTo("15.75");
    }

    @Test
    @DisplayName("setType/getType works correctly")
    void typeGetterSetter() {
      AgentResponse response = new AgentResponse();
      response.setType(AgentType.EXTERNAL);

      assertThat(response.getType()).isEqualTo(AgentType.EXTERNAL);
    }

    @Test
    @DisplayName("setActive/isActive works correctly")
    void activeGetterSetter() {
      AgentResponse response = new AgentResponse();
      response.setActive(true);

      assertThat(response.isActive()).isTrue();

      response.setActive(false);

      assertThat(response.isActive()).isFalse();
    }

    @Test
    @DisplayName("setCreatedAt/getCreatedAt works correctly")
    void createdAtGetterSetter() {
      AgentResponse response = new AgentResponse();
      LocalDateTime now = LocalDateTime.now();
      response.setCreatedAt(now);

      assertThat(response.getCreatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("setUpdatedAt/getUpdatedAt works correctly")
    void updatedAtGetterSetter() {
      AgentResponse response = new AgentResponse();
      LocalDateTime now = LocalDateTime.now();
      response.setUpdatedAt(now);

      assertThat(response.getUpdatedAt()).isEqualTo(now);
    }
  }

  @Nested
  @DisplayName("Default values")
  class DefaultValues {

    @Test
    @DisplayName("new AgentResponse() has all null fields")
    void allFieldsNullByDefault() {
      AgentResponse response = new AgentResponse();

      assertThat(response.getId()).isNull();
      assertThat(response.getName()).isNull();
      assertThat(response.getEmail()).isNull();
      assertThat(response.getCommissionPercent()).isNull();
      assertThat(response.getType()).isNull();
      assertThat(response.isActive()).isFalse(); // boolean default
      assertThat(response.getCreatedAt()).isNull();
      assertThat(response.getUpdatedAt()).isNull();
    }
  }

  @Nested
  @DisplayName("Complete response object")
  class CompleteResponseObject {

    @Test
    @DisplayName("can create fully populated response")
    void fullyPopulatedResponse() {
      AgentResponse response = new AgentResponse();
      LocalDateTime created = LocalDateTime.of(2026, 1, 1, 10, 0);
      LocalDateTime updated = LocalDateTime.of(2026, 3, 1, 15, 30);

      response.setId(123L);
      response.setName("Alice Johnson");
      response.setEmail("alice@company.com");
      response.setCommissionPercent(new BigDecimal("8.50"));
      response.setType(AgentType.INTERNAL);
      response.setActive(true);
      response.setCreatedAt(created);
      response.setUpdatedAt(updated);

      assertThat(response.getId()).isEqualTo(123L);
      assertThat(response.getName()).isEqualTo("Alice Johnson");
      assertThat(response.getEmail()).isEqualTo("alice@company.com");
      assertThat(response.getCommissionPercent()).isEqualByComparingTo("8.50");
      assertThat(response.getType()).isEqualTo(AgentType.INTERNAL);
      assertThat(response.isActive()).isTrue();
      assertThat(response.getCreatedAt()).isEqualTo(created);
      assertThat(response.getUpdatedAt()).isEqualTo(updated);
    }

    @Test
    @DisplayName("can create inactive external agent response")
    void inactiveExternalAgent() {
      AgentResponse response = new AgentResponse();
      LocalDateTime timestamp = LocalDateTime.now();

      response.setId(999L);
      response.setName("Inactive Agent");
      response.setEmail("inactive@external.com");
      response.setCommissionPercent(new BigDecimal("20.00"));
      response.setType(AgentType.EXTERNAL);
      response.setActive(false);
      response.setCreatedAt(timestamp);
      response.setUpdatedAt(timestamp);

      assertThat(response.getId()).isEqualTo(999L);
      assertThat(response.getName()).isEqualTo("Inactive Agent");
      assertThat(response.getEmail()).isEqualTo("inactive@external.com");
      assertThat(response.getCommissionPercent()).isEqualByComparingTo("20.00");
      assertThat(response.getType()).isEqualTo(AgentType.EXTERNAL);
      assertThat(response.isActive()).isFalse();
      assertThat(response.getCreatedAt()).isEqualTo(timestamp);
      assertThat(response.getUpdatedAt()).isEqualTo(timestamp);
    }
  }

  @Nested
  @DisplayName("Edge cases and special values")
  class EdgeCases {

    @Test
    @DisplayName("can handle zero commission")
    void zeroCommission() {
      AgentResponse response = new AgentResponse();
      response.setCommissionPercent(new BigDecimal("0.00"));

      assertThat(response.getCommissionPercent()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("can handle maximum commission")
    void maximumCommission() {
      AgentResponse response = new AgentResponse();
      response.setCommissionPercent(new BigDecimal("100.00"));

      assertThat(response.getCommissionPercent()).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("commission preserves precision")
    void commissionPreservesPrecision() {
      AgentResponse response = new AgentResponse();
      response.setCommissionPercent(new BigDecimal("12.3456789"));

      assertThat(response.getCommissionPercent()).isEqualByComparingTo("12.3456789");
    }

    @Test
    @DisplayName("can update fields after initial setup")
    void canUpdateFields() {
      AgentResponse response = new AgentResponse();
      response.setId(1L);
      response.setName("Original");
      response.setActive(true);

      // Update fields
      response.setName("Updated");
      response.setActive(false);

      assertThat(response.getId()).isEqualTo(1L); // unchanged
      assertThat(response.getName()).isEqualTo("Updated");
      assertThat(response.isActive()).isFalse();
    }

    @Test
    @DisplayName("createdAt and updatedAt can be different")
    void createdAndUpdatedCanDiffer() {
      AgentResponse response = new AgentResponse();
      LocalDateTime created = LocalDateTime.of(2025, 1, 1, 0, 0);
      LocalDateTime updated = LocalDateTime.of(2026, 3, 2, 12, 30);

      response.setCreatedAt(created);
      response.setUpdatedAt(updated);

      assertThat(response.getCreatedAt()).isEqualTo(created);
      assertThat(response.getUpdatedAt()).isEqualTo(updated);
      assertThat(response.getUpdatedAt()).isAfter(response.getCreatedAt());
    }

    @Test
    @DisplayName("can handle both AgentType values")
    void bothAgentTypes() {
      AgentResponse internal = new AgentResponse();
      internal.setType(AgentType.INTERNAL);

      AgentResponse external = new AgentResponse();
      external.setType(AgentType.EXTERNAL);

      assertThat(internal.getType()).isEqualTo(AgentType.INTERNAL);
      assertThat(external.getType()).isEqualTo(AgentType.EXTERNAL);
    }
  }

  @Nested
  @DisplayName("Typical use cases")
  class TypicalUseCases {

    @Test
    @DisplayName("response for newly created agent")
    void newlyCreatedAgent() {
      LocalDateTime now = LocalDateTime.now();
      AgentResponse response = new AgentResponse();

      response.setId(1L);
      response.setName("New Agent");
      response.setEmail("new@test.com");
      response.setCommissionPercent(new BigDecimal("5.00"));
      response.setType(AgentType.INTERNAL);
      response.setActive(true);
      response.setCreatedAt(now);
      response.setUpdatedAt(now);

      assertThat(response.getId()).isNotNull();
      assertThat(response.isActive()).isTrue();
      assertThat(response.getCreatedAt()).isEqualTo(response.getUpdatedAt());
    }

    @Test
    @DisplayName("response for updated agent")
    void updatedAgent() {
      LocalDateTime created = LocalDateTime.of(2025, 1, 1, 0, 0);
      LocalDateTime updated = LocalDateTime.now();

      AgentResponse response = new AgentResponse();
      response.setId(1L);
      response.setName("Updated Agent");
      response.setEmail("updated@test.com");
      response.setCommissionPercent(new BigDecimal("12.00"));
      response.setType(AgentType.EXTERNAL);
      response.setActive(false);
      response.setCreatedAt(created);
      response.setUpdatedAt(updated);

      assertThat(response.getUpdatedAt()).isAfter(response.getCreatedAt());
      assertThat(response.getCommissionPercent()).isEqualByComparingTo("12.00");
      assertThat(response.isActive()).isFalse();
    }
  }
}
