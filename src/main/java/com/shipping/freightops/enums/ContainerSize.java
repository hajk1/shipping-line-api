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
}
