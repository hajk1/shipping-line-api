package com.shipping.freightops.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.shipping.freightops.enums.AgentType;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link Agent} entity. */
class AgentTest {

  @Nested
  @DisplayName("Constructor and defaults")
  class ConstructorAndDefaults {

    @Test
    @DisplayName("new Agent() sets active=true by default")
    void defaultActiveIsTrue() {
      Agent agent = new Agent();

      assertThat(agent.isActive()).isTrue();
    }

    @Test
    @DisplayName("new Agent() has null fields except active")
    void defaultFieldsAreNull() {
      Agent agent = new Agent();

      assertThat(agent.getName()).isNull();
      assertThat(agent.getEmail()).isNull();
      assertThat(agent.getCommissionPercent()).isNull();
      assertThat(agent.getType()).isNull();
      assertThat(agent.getId()).isNull();
      assertThat(agent.getCreatedAt()).isNull();
      assertThat(agent.getUpdatedAt()).isNull();
    }
  }

  @Nested
  @DisplayName("Getters and setters")
  class GettersAndSetters {

    @Test
    @DisplayName("setName/getName works correctly")
    void nameGetterSetter() {
      Agent agent = new Agent();
      agent.setName("John Doe");

      assertThat(agent.getName()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("setEmail/getEmail works correctly")
    void emailGetterSetter() {
      Agent agent = new Agent();
      agent.setEmail("john@example.com");

      assertThat(agent.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    @DisplayName("setCommissionPercent/getCommissionPercent works correctly")
    void commissionPercentGetterSetter() {
      Agent agent = new Agent();
      BigDecimal commission = new BigDecimal("12.50");
      agent.setCommissionPercent(commission);

      assertThat(agent.getCommissionPercent()).isEqualByComparingTo("12.50");
    }

    @Test
    @DisplayName("setType/getType works correctly")
    void typeGetterSetter() {
      Agent agent = new Agent();
      agent.setType(AgentType.EXTERNAL);

      assertThat(agent.getType()).isEqualTo(AgentType.EXTERNAL);
    }

    @Test
    @DisplayName("setActive/isActive works correctly")
    void activeGetterSetter() {
      Agent agent = new Agent();
      agent.setActive(false);

      assertThat(agent.isActive()).isFalse();

      agent.setActive(true);

      assertThat(agent.isActive()).isTrue();
    }
  }

  @Nested
  @DisplayName("BaseEntity inherited methods")
  class BaseEntityInheritance {

    @Test
    @DisplayName("setId/getId works correctly")
    void idGetterSetter() {
      Agent agent = new Agent();
      agent.setId(123L);

      assertThat(agent.getId()).isEqualTo(123L);
    }
  }

  @Nested
  @DisplayName("Field validation constraints")
  class ValidationConstraints {

    @Test
    @DisplayName("can set valid email format")
    void validEmailFormat() {
      Agent agent = new Agent();
      agent.setEmail("valid.email@domain.com");

      assertThat(agent.getEmail()).isEqualTo("valid.email@domain.com");
    }

    @Test
    @DisplayName("can set commission within valid range (0-100)")
    void validCommissionRange() {
      Agent agent = new Agent();

      agent.setCommissionPercent(new BigDecimal("0.00"));
      assertThat(agent.getCommissionPercent()).isEqualByComparingTo("0.00");

      agent.setCommissionPercent(new BigDecimal("50.00"));
      assertThat(agent.getCommissionPercent()).isEqualByComparingTo("50.00");

      agent.setCommissionPercent(new BigDecimal("100.00"));
      assertThat(agent.getCommissionPercent()).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("can set all valid AgentType values")
    void validAgentTypes() {
      Agent agent = new Agent();

      agent.setType(AgentType.INTERNAL);
      assertThat(agent.getType()).isEqualTo(AgentType.INTERNAL);

      agent.setType(AgentType.EXTERNAL);
      assertThat(agent.getType()).isEqualTo(AgentType.EXTERNAL);
    }
  }

  @Nested
  @DisplayName("Complete entity setup")
  class CompleteEntitySetup {

    @Test
    @DisplayName("can create fully populated agent")
    void fullyPopulatedAgent() {
      Agent agent = new Agent();
      agent.setId(1L);
      agent.setName("Jane Smith");
      agent.setEmail("jane.smith@company.com");
      agent.setCommissionPercent(new BigDecimal("7.25"));
      agent.setType(AgentType.INTERNAL);
      agent.setActive(true);

      assertThat(agent.getId()).isEqualTo(1L);
      assertThat(agent.getName()).isEqualTo("Jane Smith");
      assertThat(agent.getEmail()).isEqualTo("jane.smith@company.com");
      assertThat(agent.getCommissionPercent()).isEqualByComparingTo("7.25");
      assertThat(agent.getType()).isEqualTo(AgentType.INTERNAL);
      assertThat(agent.isActive()).isTrue();
    }

    @Test
    @DisplayName("can create inactive external agent")
    void inactiveExternalAgent() {
      Agent agent = new Agent();
      agent.setName("Inactive Agent");
      agent.setEmail("inactive@external.com");
      agent.setCommissionPercent(new BigDecimal("15.00"));
      agent.setType(AgentType.EXTERNAL);
      agent.setActive(false);

      assertThat(agent.getName()).isEqualTo("Inactive Agent");
      assertThat(agent.getEmail()).isEqualTo("inactive@external.com");
      assertThat(agent.getCommissionPercent()).isEqualByComparingTo("15.00");
      assertThat(agent.getType()).isEqualTo(AgentType.EXTERNAL);
      assertThat(agent.isActive()).isFalse();
    }
  }

  @Nested
  @DisplayName("Edge cases")
  class EdgeCases {

    @Test
    @DisplayName("can handle null values for nullable fields")
    void handlesNullValues() {
      Agent agent = new Agent();
      agent.setName("Name");
      agent.setEmail("email@test.com");
      agent.setCommissionPercent(new BigDecimal("5.00"));
      agent.setType(AgentType.INTERNAL);

      // Try setting null where allowed by entity (though validation might prevent it in real use)
      agent.setId(null);

      assertThat(agent.getId()).isNull();
    }

    @Test
    @DisplayName("can update existing agent fields")
    void canUpdateFields() {
      Agent agent = new Agent();
      agent.setName("Original Name");
      agent.setEmail("original@test.com");
      agent.setCommissionPercent(new BigDecimal("5.00"));
      agent.setType(AgentType.INTERNAL);
      agent.setActive(true);

      // Update fields
      agent.setName("Updated Name");
      agent.setEmail("updated@test.com");
      agent.setCommissionPercent(new BigDecimal("10.00"));
      agent.setType(AgentType.EXTERNAL);
      agent.setActive(false);

      assertThat(agent.getName()).isEqualTo("Updated Name");
      assertThat(agent.getEmail()).isEqualTo("updated@test.com");
      assertThat(agent.getCommissionPercent()).isEqualByComparingTo("10.00");
      assertThat(agent.getType()).isEqualTo(AgentType.EXTERNAL);
      assertThat(agent.isActive()).isFalse();
    }

    @Test
    @DisplayName("commission percent maintains precision")
    void commissionPrecision() {
      Agent agent = new Agent();

      agent.setCommissionPercent(new BigDecimal("12.345"));
      assertThat(agent.getCommissionPercent()).isEqualByComparingTo("12.345");

      agent.setCommissionPercent(new BigDecimal("0.01"));
      assertThat(agent.getCommissionPercent()).isEqualByComparingTo("0.01");

      agent.setCommissionPercent(new BigDecimal("99.999"));
      assertThat(agent.getCommissionPercent()).isEqualByComparingTo("99.999");
    }
  }
}
