package com.shipping.freightops.service.impl;

import com.shipping.freightops.config.EmailProperties;
import com.shipping.freightops.service.EmailService;
import jakarta.activation.DataSource;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@ConditionalOnBean(JavaMailSender.class)
public class SmtpEmailService implements EmailService {

  @Autowired private JavaMailSender mailSender;

  @Autowired private EmailProperties emailProperties;

  @Override
  public void sendEmail(String to, String subject, String body) {
    validateEmailInput(to, subject, body);

    if (!emailProperties.isEnabled()) {
      log.debug("Email sending is disabled. Skipping email to: {}", to);
      return;
    }

    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

      helper.setFrom(emailProperties.getFromAddress());
      helper.setReplyTo(emailProperties.getReplyTo());
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setText(body, true); // true = HTML content

      mailSender.send(message);
      log.info("Email sent successfully to: {}", to);
    } catch (MessagingException e) {
      log.error("Failed to send email to: {}", to, e);
      throw new RuntimeException("Email send failed: " + e.getMessage(), e);
    }
  }

  @Override
  public void sendEmailWithAttachment(
      String to,
      String subject,
      String body,
      String attachmentName,
      byte[] attachmentContent,
      String mimeType) {
    validateEmailInput(to, subject, body);
    validateAttachmentInput(attachmentName, attachmentContent, mimeType);

    if (!emailProperties.isEnabled()) {
      log.debug("Email sending is disabled. Skipping email with attachment to: {}", to);
      return;
    }

    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setFrom(emailProperties.getFromAddress());
      helper.setReplyTo(emailProperties.getReplyTo());
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setText(body, true); // true = HTML content

      DataSource dataSource =
          new jakarta.mail.util.ByteArrayDataSource(attachmentContent, mimeType);
      helper.addAttachment(attachmentName, dataSource);

      mailSender.send(message);
      log.info("Email with attachment '{}' sent successfully to: {}", attachmentName, to);
    } catch (MessagingException e) {
      log.error("Failed to send email with attachment to: {}", to, e);
      throw new RuntimeException("Email send failed: " + e.getMessage(), e);
    }
  }

  private void validateEmailInput(String to, String subject, String body) {
    if (!StringUtils.hasText(to)) {
      throw new IllegalArgumentException("Recipient email address cannot be null or empty");
    }
    if (!StringUtils.hasText(subject)) {
      throw new IllegalArgumentException("Email subject cannot be null or empty");
    }
    if (!StringUtils.hasText(body)) {
      throw new IllegalArgumentException("Email body cannot be null or empty");
    }
  }

  private void validateAttachmentInput(String attachmentName, byte[] content, String mimeType) {
    if (!StringUtils.hasText(attachmentName)) {
      throw new IllegalArgumentException("Attachment name cannot be null or empty");
    }
    if (content == null || content.length == 0) {
      throw new IllegalArgumentException("Attachment content cannot be null or empty");
    }
    if (!StringUtils.hasText(mimeType)) {
      throw new IllegalArgumentException("MIME type cannot be null or empty");
    }
  }
}
