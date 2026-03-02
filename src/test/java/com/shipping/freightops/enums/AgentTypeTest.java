package com.shipping.freightops.enums;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link AgentType} enum. */
class AgentTypeTest {

  @Nested
  @DisplayName("Enum values")
  class EnumValues {

    @Test
    @DisplayName("has INTERNAL value")
    void hasInternalValue() {
      AgentType type = AgentType.INTERNAL;

      assertThat(type).isNotNull();
      assertThat(type.name()).isEqualTo("INTERNAL");
    }

    @Test
    @DisplayName("has EXTERNAL value")
    void hasExternalValue() {
      AgentType type = AgentType.EXTERNAL;

      assertThat(type).isNotNull();
      assertThat(type.name()).isEqualTo("EXTERNAL");
    }

    @Test
    @DisplayName("has exactly 2 values")
    void hasExactlyTwoValues() {
      AgentType[] values = AgentType.values();

      assertThat(values).hasSize(2);
      assertThat(values).containsExactlyInAnyOrder(AgentType.INTERNAL, AgentType.EXTERNAL);
    }
  }

  @Nested
  @DisplayName("Enum comparison")
  class EnumComparison {

    @Test
    @DisplayName("INTERNAL equals itself")
    void internalEqualsItself() {
      AgentType type1 = AgentType.INTERNAL;
      AgentType type2 = AgentType.INTERNAL;

      assertThat(type1).isEqualTo(type2);
      assertThat(type1 == type2).isTrue();
    }

    @Test
    @DisplayName("EXTERNAL equals itself")
    void externalEqualsItself() {
      AgentType type1 = AgentType.EXTERNAL;
      AgentType type2 = AgentType.EXTERNAL;

      assertThat(type1).isEqualTo(type2);
      assertThat(type1 == type2).isTrue();
    }

    @Test
    @DisplayName("INTERNAL not equals EXTERNAL")
    void internalNotEqualsExternal() {
      AgentType internal = AgentType.INTERNAL;
      AgentType external = AgentType.EXTERNAL;

      assertThat(internal).isNotEqualTo(external);
      assertThat(internal == external).isFalse();
    }
  }

  @Nested
  @DisplayName("valueOf method")
  class ValueOfMethod {

    @Test
    @DisplayName("valueOf(\"INTERNAL\") returns INTERNAL")
    void valueOfInternal() {
      AgentType type = AgentType.valueOf("INTERNAL");

      assertThat(type).isEqualTo(AgentType.INTERNAL);
    }

    @Test
    @DisplayName("valueOf(\"EXTERNAL\") returns EXTERNAL")
    void valueOfExternal() {
      AgentType type = AgentType.valueOf("EXTERNAL");

      assertThat(type).isEqualTo(AgentType.EXTERNAL);
    }

    @Test
    @DisplayName("valueOf with invalid name throws IllegalArgumentException")
    void valueOfInvalidThrows() {
      try {
        AgentType.valueOf("INVALID");
        throw new AssertionError("Expected IllegalArgumentException");
      } catch (IllegalArgumentException e) {
        assertThat(e).isInstanceOf(IllegalArgumentException.class);
      }
    }

    @Test
    @DisplayName("valueOf is case-sensitive")
    void valueOfCaseSensitive() {
      try {
        AgentType.valueOf("internal");
        throw new AssertionError("Expected IllegalArgumentException");
      } catch (IllegalArgumentException e) {
        assertThat(e).isInstanceOf(IllegalArgumentException.class);
      }
    }
  }

  @Nested
  @DisplayName("name method")
  class NameMethod {

    @Test
    @DisplayName("INTERNAL.name() returns \"INTERNAL\"")
    void internalName() {
      assertThat(AgentType.INTERNAL.name()).isEqualTo("INTERNAL");
    }

    @Test
    @DisplayName("EXTERNAL.name() returns \"EXTERNAL\"")
    void externalName() {
      assertThat(AgentType.EXTERNAL.name()).isEqualTo("EXTERNAL");
    }
  }

  @Nested
  @DisplayName("ordinal method")
  class OrdinalMethod {

    @Test
    @DisplayName("enum values have sequential ordinals")
    void sequentialOrdinals() {
      AgentType[] values = AgentType.values();

      assertThat(values[0].ordinal()).isEqualTo(0);
      assertThat(values[1].ordinal()).isEqualTo(1);
    }

    @Test
    @DisplayName("INTERNAL and EXTERNAL have different ordinals")
    void differentOrdinals() {
      assertThat(AgentType.INTERNAL.ordinal()).isNotEqualTo(AgentType.EXTERNAL.ordinal());
    }
  }

  @Nested
  @DisplayName("Switch statement compatibility")
  class SwitchCompatibility {

    @Test
    @DisplayName("can be used in switch statement")
    void canBeUsedInSwitch() {
      String result = getTypeDescription(AgentType.INTERNAL);
      assertThat(result).isEqualTo("Internal Agent");

      result = getTypeDescription(AgentType.EXTERNAL);
      assertThat(result).isEqualTo("External Agent");
    }

    private String getTypeDescription(AgentType type) {
      switch (type) {
        case INTERNAL:
          return "Internal Agent";
        case EXTERNAL:
          return "External Agent";
        default:
          return "Unknown";
      }
    }
  }

  @Nested
  @DisplayName("toString method")
  class ToStringMethod {

    @Test
    @DisplayName("toString returns enum name")
    void toStringReturnsName() {
      assertThat(AgentType.INTERNAL.toString()).isEqualTo("INTERNAL");
      assertThat(AgentType.EXTERNAL.toString()).isEqualTo("EXTERNAL");
    }
  }

  @Nested
  @DisplayName("Usage scenarios")
  class UsageScenarios {

    @Test
    @DisplayName("can be used as map key")
    void canBeUsedAsMapKey() {
      java.util.Map<AgentType, String> map = new java.util.HashMap<>();
      map.put(AgentType.INTERNAL, "Internal agents");
      map.put(AgentType.EXTERNAL, "External agents");

      assertThat(map.get(AgentType.INTERNAL)).isEqualTo("Internal agents");
      assertThat(map.get(AgentType.EXTERNAL)).isEqualTo("External agents");
    }

    @Test
    @DisplayName("can be used in collections")
    void canBeUsedInCollections() {
      java.util.List<AgentType> types =
          java.util.Arrays.asList(AgentType.INTERNAL, AgentType.EXTERNAL, AgentType.INTERNAL);

      assertThat(types).hasSize(3);
      assertThat(types).containsExactly(AgentType.INTERNAL, AgentType.EXTERNAL, AgentType.INTERNAL);
    }

    @Test
    @DisplayName("can be compared with == operator")
    void canBeComparedWithEquals() {
      AgentType type = AgentType.INTERNAL;

      boolean isInternal = (type == AgentType.INTERNAL);
      boolean isExternal = (type == AgentType.EXTERNAL);

      assertThat(isInternal).isTrue();
      assertThat(isExternal).isFalse();
    }
  }
}
