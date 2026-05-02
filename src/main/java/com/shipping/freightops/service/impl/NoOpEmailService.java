package com.shipping.freightops.service.impl;

import com.shipping.freightops.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnMissingBean(JavaMailSender.class)
public class NoOpEmailService implements EmailService {

  @Override
  public void sendEmail(String to, String subject, String body) {
    log.debug("NoOp email service: skipping email to {} with subject '{}'", to, subject);
  }

  @Override
  public void sendEmailWithAttachment(
      String to,
      String subject,
      String body,
      String attachmentName,
      byte[] attachmentContent,
      String mimeType) {
    log.debug(
        "NoOp email service: skipping email to {} with subject '{}' and attachment '{}'",
        to,
        subject,
        attachmentName);
  }
}
