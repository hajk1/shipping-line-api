package com.shipping.freightops.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.email")
public class EmailProperties {

  /** Whether email sending is enabled. Default: true */
  private boolean enabled = true;

  /** "From" address for all outgoing emails. Default: noreply@apgl-shipping.com */
  private String fromAddress = "noreply@apgl-shipping.com";

  /** "Reply-To" address for all outgoing emails. Default: support@apgl-shipping.com */
  private String replyTo = "support@apgl-shipping.com";

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getFromAddress() {
    return fromAddress;
  }

  public void setFromAddress(String fromAddress) {
    this.fromAddress = fromAddress;
  }

  public String getReplyTo() {
    return replyTo;
  }

  public void setReplyTo(String replyTo) {
    this.replyTo = replyTo;
  }
}
