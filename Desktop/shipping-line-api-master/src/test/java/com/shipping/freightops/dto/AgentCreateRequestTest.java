package com.shipping.freightops.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.shipping.freightops.enums.AgentType;
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

/** Unit tests for {@link AgentCreateRequest} DTO. */
class AgentCreateRequestTest {

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
    @DisplayName("setName/getName works correctly")
    void nameGetterSetter() {
      AgentCreateRequest request = new AgentCreateRequest();
      request.setName("Test Agent");

      assertThat(request.getName()).isEqualTo("Test Agent");
    }

    @Test
    @DisplayName("setEmail/getEmail works correctly")
    void emailGetterSetter() {
      AgentCreateRequest request = new AgentCreateRequest();
      request.setEmail("test@example.com");

      assertThat(request.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("setCommissionPercent/getCommissionPercent works correctly")
    void commissionPercentGetterSetter() {
      AgentCreateRequest request = new AgentCreateRequest();
      BigDecimal commission = new BigDecimal("15.50");
      request.setCommissionPercent(commission);

      assertThat(request.getCommissionPercent()).isEqualByComparingTo("15.50");
    }

    @Test
    @DisplayName("setType/getType works correctly")
    void typeGetterSetter() {
      AgentCreateRequest request = new AgentCreateRequest();
      request.setType(AgentType.EXTERNAL);

      assertThat(request.getType()).isEqualTo(AgentType.EXTERNAL);
    }
  }

  @Nested
  @DisplayName("Validation - valid cases")
  class ValidationValid {

    @Test
    @DisplayName("valid request with all fields passes validation")
    void validRequestPassesValidation() {
      AgentCreateRequest request = new AgentCreateRequest();
      request.setName("Valid Agent");
      request.setEmail("valid@example.com");
      request.setCommissionPercent(new BigDecimal("10.00"));
      request.setType(AgentType.INTERNAL);

      Set<ConstraintViolation<AgentCreateRequest>> violations = validator.validate(request);

      assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("valid request with minimum commission (0.0)")
    void validWithMinimumCommission() {
      AgentCreateRequest request = new AgentCreateRequest();
      request.setName("Zero Commission");
      request.setEmail("zero@example.com");
      request.setCommissionPercent(new BigDecimal("0.0"));
      request.setType(AgentType.EXTERNAL);

      Set<ConstraintViolation<AgentCreateRequest>> violations = validator.validate(request);

      assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("valid request with maximum commission (100.0)")
    void validWithMaximumCommission() {
      AgentCreateRequest request = new AgentCreateRequest();
      request.setName("Max Commission");
      request.setEmail("max@example.com");
      request.setCommissionPercent(new BigDecimal("100.0"));
      request.setType(AgentType.INTERNAL);

      Set<ConstraintViolation<AgentCreateRequest>> violations = validator.validate(request);

      assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("valid request with both agent types")
    void validWithBothAgentTypes() {
      AgentCreateRequest internal = new AgentCreateRequest();
      internal.setName("Internal Agent");
      internal.setEmail("internal@example.com");
      internal.setCommissionPercent(new BigDecimal("5.0"));
      internal.setType(AgentType.INTERNAL);

      AgentCreateRequest external = new AgentCreateRequest();
      external.setName("External Agent");
      external.setEmail("external@example.com");
      external.setCommissionPercent(new BigDecimal("5.0"));
      external.setType(AgentType.EXTERNAL);

      assertThat(validator.validate(internal)).isEmpty();
      assertThat(validator.validate(external)).isEmpty();
    }
  }

  @Nested
  @DisplayName("Validation - name field")
  class ValidationName {

    @Test
    @DisplayName("null name fails validation")
    void nullNameFails() {
      AgentCreateRequest request = new AgentCreateRequest();
      request.setName(null);
      request.setEmail("test@example.com");
      request.setCommissionPercent(new BigDecimal("5.0"));
      request.setType(AgentType.INTERNAL);

      Set<ConstraintViolation<AgentCreateRequest>> violations = validator.validate(request);

      assertThat(violations).isNotEmpty();
      assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("name"));
    }

    @Test
    @DisplayName("empty name fails validation")
    void emptyNameFails() {
      AgentCreateRequest request = new AgentCreateRequest();
      request.setName("");
      request.setEmail("test@example.com");
      request.setCommissionPercent(new BigDecimal("5.0"));
      request.setType(AgentType.INTERNAL);

      Set<ConstraintViolation<AgentCreateRequest>> violations = validator.validate(request);

      assertThat(violations).isNotEmpty();
      assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("name"));
    }

    @Test
    @DisplayName("blank name (whitespace only) fails validation")
    void blankNameFails() {
      AgentCreateRequest request = new AgentCreateRequest();
      request.setName("   ");
      request.setEmail("test@example.com");
      request.setCommissionPercent(new BigDecimal("5.0"));
      request.setType(AgentType.INTERNAL);

      Set<ConstraintViolation<AgentCreateRequest>> violations = validator.validate(request);

      assertThat(violations).isNotEmpty();
      assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("name"));
    }
  }

  @Nested
  @DisplayName("Validation - email field")
  class ValidationEmail {

    @Test
    @DisplayName("null email fails validation")
    void nullEmailFails() {
      AgentCreateRequest request = new AgentCreateRequest();
      request.setName("Test Agent");
      request.setEmail(null);
      request.setCommissionPercent(new BigDecimal("5.0"));
      request.setType(AgentType.INTERNAL);

      Set<ConstraintViolation<AgentCreateRequest>> violations = validator.validate(request);

      assertThat(violations).isNotEmpty();
      assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    @DisplayName("empty email fails validation")
    void emptyEmailFails() {
      AgentCreateRequest request = new AgentCreateRequest();
      request.setName("Test Agent");
      request.setEmail("");
      request.setCommissionPercent(new BigDecimal("5.0"));
      request.setType(AgentType.INTERNAL);

      Set<ConstraintViolation<AgentCreateRequest>> violations = validator.validate(request);

      assertThat(violations).isNotEmpty();
      assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    @DisplayName("invalid email format fails validation")
    void invalidEmailFormatFails() {
      AgentCreateRequest request = new AgentCreateRequest();
      request.setName("Test Agent");
      request.setEmail("not-an-email");
      request.setCommissionPercent(new BigDecimal("5.0"));
      request.setType(AgentType.INTERNAL);

      Set<ConstraintViolation<AgentCreateRequest>> violations = validator.validate(request);

      assertThat(violations).isNotEmpty();
      assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    @DisplayName("email without @ fails validation")
    void emailWithoutAtFails() {
      AgentCreateRequest request = new AgentCreateRequest();
      request.setName("Test Agent");
      request.setEmail("bademail.com");
      request.setCommissionPercent(new BigDecimal("5.0"));
      request.setType(AgentType.INTERNAL);

      Set<ConstraintViolation<AgentCreateRequest>> violations = validator.validate(request);

      assertThat(violations).isNotEmpty();
      assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }
  }

  @Nested
  @DisplayName("Validation - commissionPercent field")
  class ValidationCommissionPercent {

    @Test
    @DisplayName("null commissionPercent fails validation")
    void nullCommissionFails() {
      AgentCreateRequest request = new AgentCreateRequest();
      request.setName("Test Agent");
      request.setEmail("test@example.com");
      request.setCommissionPercent(null);
      request.setType(AgentType.INTERNAL);

      Set<ConstraintViolation<AgentCreateRequest>> violations = validator.validate(request);

      assertThat(violations).isNotEmpty();
      assertThat(violations)
          .anyMatch(v -> v.getPropertyPath().toString().equals("commissionPercent"));
    }

    @Test
    @DisplayName("negative commissionPercent fails validation")
    void negativeCommissionFails() {
      AgentCreateRequest request = new AgentCreateRequest();
      request.setName("Test Agent");
      request.setEmail("test@example.com");
      request.setCommissionPercent(new BigDecimal("-1.0"));
      request.setType(AgentType.INTERNAL);

      Set<ConstraintViolation<AgentCreateRequest>> violations = validator.validate(request);

      assertThat(violations).isNotEmpty();
      assertThat(violations)
          .anyMatch(v -> v.getPropertyPath().toString().equals("commissionPercent"));
    }

    @Test
    @DisplayName("commissionPercent > 100 fails validation")
    void commissionOver100Fails() {
      AgentCreateRequest request = new AgentCreateRequest();
      request.setName("Test Agent");
      request.setEmail("test@example.com");
      request.setCommissionPercent(new BigDecimal("100.01"));
      request.setType(AgentType.INTERNAL);

      Set<ConstraintViolation<AgentCreateRequest>> violations = validator.validate(request);

      assertThat(violations).isNotEmpty();
      assertThat(violations)
          .anyMatch(v -> v.getPropertyPath().toString().equals("commissionPercent"));
    }

    @Test
    @DisplayName("commissionPercent of 50.5 passes validation")
    void midRangeCommissionPasses() {
      AgentCreateRequest request = new AgentCreateRequest();
      request.setName("Test Agent");
      request.setEmail("test@example.com");
      request.setCommissionPercent(new BigDecimal("50.5"));
      request.setType(AgentType.INTERNAL);

      Set<ConstraintViolation<AgentCreateRequest>> violations = validator.validate(request);

      assertThat(violations).isEmpty();
    }
  }

  @Nested
  @DisplayName("Validation - type field")
  class ValidationType {

    @Test
    @DisplayName("null type fails validation")
    void nullTypeFails() {
      AgentCreateRequest request = new AgentCreateRequest();
      request.setName("Test Agent");
      request.setEmail("test@example.com");
      request.setCommissionPercent(new BigDecimal("5.0"));
      request.setType(null);

      Set<ConstraintViolation<AgentCreateRequest>> violations = validator.validate(request);

      assertThat(violations).isNotEmpty();
      assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("type"));
    }
  }

  @Nested
  @DisplayName("Multiple validation errors")
  class MultipleValidationErrors {

    @Test
    @DisplayName("request with all null fields has multipations")
    void allNullFieldsHaveMultipleViolations() {
      AgentCreateRequest request = new AgentCreateRequest();

      Set<ConstraintViolation<AgentCreateRequest>> violations = validator.validate(request);

      assertThat(violations).hasSizeGreaterThanOrEqualTo(4);
    }

    @Test
    @DisplayName("request with multiple invalid fields reports all violations")
    void multipleInvalidFieldsReportedTogether() {
      AgentCreateRequest request = new AgentCreateRequest();
      request.setName(""); // invalid
      request.setEmail("bad-email"); // invalid
      request.setCommissionPercent(new BigDecimal("-5.0")); // invalid
      request.setType(null); // invalid

      Set<ConstraintViolation<AgentCreateRequest>> violations = validator.validate(request);

      assertThat(violations).hasSizeGreaterThanOrEqualTo(4);
    }
  }
}
