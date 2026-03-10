package com.shipping.freightops.enums;

/** Standard ISO container sizes. */
public enum ContainerSize {
  TWENTY_FOOT,
  FORTY_FOOT;

  public int getTeu() {
    return switch (this) {
      case TWENTY_FOOT -> 1;
      case FORTY_FOOT -> 2;
    };
  }

  // Used for PDF construction, should reflect all values in the enum
  public String getPdfReadyValue() {
    return switch (this) {
      case TWENTY_FOOT -> "20ft";
      case FORTY_FOOT -> "40ft";
    };
  }
}
