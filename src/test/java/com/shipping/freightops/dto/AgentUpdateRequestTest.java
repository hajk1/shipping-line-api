package com.shipping.freightops.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link AgentUpdateRequest} DTO. */
class AgentUpdateRequestTest {

  private static Validator validator;

  @BeforeAll
  static void setUpValidator() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Nested
  @DisplayName("Getters and setters")
  class GettersAndSetters {

    @Test
    @DisplayName("setCommissionPercent/getCommissionPercent works correctly")
    void commissionPercentGetterSetter() {
      AgentUpdateRequest request = new AgentUpdateRequest();
      BigDecimal commission = new BigDecimal("12.75");
      request.setCommissionPercent(commission);

      assertThat(request.getCommissionPercent()).isEqualByComparingTo("12.75");
    }

    @Test
    @DisplayName("setActive/getActive works correctly")
    void activeGetterSetter() {
      AgentUpdateRequest request = new AgentUpdateRequest();
      request.setActive(true);

      assertThat(request.getActive()).isTrue();

      request.setActive(false);

      assertThat(request.getActive()).isFalse();
    }

    @Test
    @DisplayName("default values are null")
    void defaultValuesAreNull() {
      AgentUpdateRequest request = new AgentUpdateRequest();

      assertThat(request.getCommissionPercent()).isNull();
      assertThat(request.getActive()).isNull();
    }
  }

  @Nested
  @DisplayName("Validation - valid cases")
  class ValidationValid {

    @Test
    @DisplayName("empty request (all null) passes validation")
    void emptyRequestPassesValidation() {
      AgentUpdateRequest request = new AgentUpdateRequest();

      Set<ConstraintViolation<AgentUpdateRequest>> violations = validator.validate(request);

      assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("request with only commissionPercent passes validation")
    void onlyCommissionPercentPasses() {
      AgentUpdateRequest request = new AgentUpdateRequest();
      request.setCommissionPercent(new BigDecimal("15.00"));

      Set<ConstraintViolation<AgentUpdateRequest>> violations = validator.validate(request);

      assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("request with only active passes validation")
    void onlyActivePasses() {
      AgentUpdateRequest request = new AgentUpdateRequest();
      request.setActive(false);

      Set<ConstraintViolation<AgentUpdateRequest>> violations = validator.validate(request);

      assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("request with both fields passes validation")
    void bothFieldsPass() {
      AgentUpdateRequest request = new AgentUpdateRequest();
      request.setCommissionPercent(new BigDecimal("20.50"));
      request.setActive(true);

      Set<ConstraintViolation<AgentUpdateRequest>> violations = validator.validate(request);

      assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("minimum commission (0.0) passes validation")
    void minimumCommissionPasses() {
      AgentUpdateRequest request = new AgentUpdateRequest();
      request.setCommissionPercent(new BigDecimal("0.0"));

      Set<ConstraintViolation<AgentUpdateRequest>> violations = validator.validate(request);

      assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("maximum commission (100.0) passes validation")
    void maximumCommissionPasses() {
      AgentUpdateRequest request = new AgentUpdateRequest();
      request.setCommissionPercent(new BigDecimal("100.0"));

      Set<ConstraintViolation<AgentUpdateRequest>> violations = validator.validate(request);

      assertThat(violations).isEmpty();
    }
  }

  @Nested
  @DisplayName("Validation - commissionPercent field")
  class ValidationCommissionPercent {

    @Test
    @DisplayName("negative commissionPercent fails validation")
    void negativeCommissionFails() {
      AgentUpdateRequest request = new AgentUpdateRequest();
      request.setCommissionPercent(new BigDecimal("-0.01"));

      Set<ConstraintViolation<AgentUpdateRequest>> violations = validator.validate(request);

      assertThat(violations).isNotEmpty();
      assertThat(violations)
          .anyMatch(v -> v.getPropertyPath().toString().equals("commissionPercent"));
    }

    @Test
    @DisplayName("commissionPercent > 100 fails validation")
    void commissionOver100Fails() {
      AgentUpdateRequest request = new AgentUpdateRequest();
      request.setCommissionPercent(new BigDecimal("100.01"));

      Set<ConstraintViolation<AgentUpdateRequest>> violations = validator.validate(request);

      assertThat(violations).isNotEmpty();
      assertThat(violations)
          .anyMatch(v -> v.getPropertyPath().toString().equals("commissionPercent"));
    }

    @Test
    @DisplayName("very large commissionPercent fails validation")
    void veryLargeCommissionFails() {
      AgentUpdateRequest request = new AgentUpdateRequest();
      request.setCommissionPercent(new BigDecimal("1000.00"));

      Set<ConstraintViolation<AgentUpdateRequest>> violations = validator.validate(request);

      assertThat(violations).isNotEmpty();
      assertThat(violations)
          .anyMatch(v -> v.getPropertyPath().toString().equals("commissionPercent"));
    }

    @Test
    @DisplayName("very small negative commissionPercent fails validation")
    void verySmallNegativeCommissionFails() {
      AgentUpdateRequest request = new AgentUpdateRequest();
      request.setCommissionPercent(new BigDecimal("-50.00"));

      Set<ConstraintViolation<AgentUpdateRequest>> violations = validator.validate(request);

      assertThat(violations).isNotEmpty();
      assertThat(violations)
          .anyMatch(v -> v.getPropertyPath().toString().equals("commissionPercent"));
    }

    @Test
    @DisplayName("commission with high precision passes if within range")
    void highPrecisionCommissionPasses() {
      AgentUpdateRequest request = new AgentUpdateRequest();
      request.setCommissionPercent(new BigDecimal("12.345678"));

      Set<ConstraintViolation<AgentUpdateRequest>> violations = validator.validate(request);

      assertThat(violations).isEmpty();
    }
  }

  @Nested
  @DisplayName("Validation - active field")
  class ValidationActive {

    @Test
    @DisplayName("active=true passes validation")
    void activeTruePasses() {
      AgentUpdateRequest request = new AgentUpdateRequest();
      request.setActive(true);

      Set<ConstraintViolation<AgentUpdateRequest>> violations = validator.validate(request);

      assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("active=false passes validation")
    void activeFalsePasses() {
      AgentUpdateRequest request = new AgentUpdateRequest();
      request.setActive(false);

      Set<ConstraintViolation<AgentUpdateRequest>> violations = validator.validate(request);

      assertThat(violations).isEmpty();
    }
  }

  @Nested
  @DisplayName("Partial update scenarios")
  class PartialUpdateScenarios {

    @Test
    @DisplayName("can update only commission, leaving active null")
    void updateOnlyCommission() {
      AgentUpdateRequest request = new AgentUpdateRequest();
      request.setCommissionPercent(new BigDecimal("25.00"));

      assertThat(request.getCommissionPercent()).isEqualByComparingTo("25.00");
      assertThat(request.getActive()).isNull();
      assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    @DisplayName("can update only active, leaving commission null")
    void updateOnlyActive() {
      AgentUpdateRequest request = new AgentUpdateRequest();
      request.setActive(false);

      assertThat(request.getActive()).isFalse();
      assertThat(request.getCommissionPercent()).isNull();
      assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    @DisplayName("can deactivate with zero commission")
    void deactivateWithZeroCommission() {
      AgentUpdateRequest request = new AgentUpdateRequest();
      request.setCommissionPercent(new BigDecimal("0.00"));
      request.setActive(false);

      assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    @DisplayName("can activate with maximum commission")
    void activateWithMaxCommission() {
      AgentUpdateRequest request = new AgentUpdateRequest();
      request.setCommissionPercent(new BigDecimal("100.00"));
      request.setActive(true);

      assertThat(validator.validate(request)).isEmpty();
    }
  }

  @Nested
  @DisplayName("Edge cases")
  class EdgeCases {

    @Test
    @DisplayName("commission at boundary 0.0 is valid")
    void commissionAtZeroBoundary() {
      AgentUpdateRequest request = new AgentUpdateRequest();
      request.setCommissionPercent(new BigDecimal("0.0"));

      assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    @DisplayName("commission at boundary 100.0 is valid")
    void commissionAt100Boundary() {
      AgentUpdateRequest request = new AgentUpdateRequest();
      request.setCommissionPercent(new BigDecimal("100.0"));

      assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    @DisplayName("commission just below minimum fails")
    void commissionJustBelowMinimum() {
      AgentUpdateRequest request = new AgentUpdateRequest();
      request.setCommissionPercent(new BigDecimal("-0.000001"));

      assertThat(validator.validate(request)).isNotEmpty();
    }

    @Test
    @DisplayName("commission just above maximum fails")
    void commissionJustAboveMaximum() {
      AgentUpdateRequest request = new AgentUpdateRequest();
      request.setCommissionPercent(new BigDecimal("100.000001"));

      assertThat(validator.validate(request)).isNotEmpty();
    }
  }
}
