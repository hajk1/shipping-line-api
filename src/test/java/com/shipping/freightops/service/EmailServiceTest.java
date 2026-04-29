package com.shipping.freightops.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.shipping.freightops.config.EmailProperties;
import com.shipping.freightops.service.impl.SmtpEmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

  @Mock private JavaMailSender mailSender;

  @InjectMocks private SmtpEmailService emailService;

  private EmailProperties emailProperties;

  @BeforeEach
  void setUp() {
    emailProperties = new EmailProperties();
    emailProperties.setEnabled(true);
    emailProperties.setFromAddress("noreply@apgl-shipping.com");
    emailProperties.setReplyTo("support@apgl-shipping.com");
    ReflectionTestUtils.setField(emailService, "emailProperties", emailProperties);
  }

  @Test
  @DisplayName("sends email when enabled")
  void sendsEmailWhenEnabled() throws MessagingException {
    MimeMessage mimeMessage = new MimeMessage((jakarta.mail.Session) null);
    when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

    emailService.sendEmail("customer@example.com", "Test Subject", "Test Body");

    verify(mailSender).send(mimeMessage);
  }

  @Test
  @DisplayName("does not send email when disabled")
  void doesNotSendEmailWhenDisabled() {
    emailProperties.setEnabled(false);

    emailService.sendEmail("customer@example.com", "Subject", "Body");

    verifyNoInteractions(mailSender);
  }

  @Test
  @DisplayName("sends email with attachment when enabled")
  void sendsEmailWithAttachmentWhenEnabled() throws MessagingException {
    MimeMessage mimeMessage = new MimeMessage((jakarta.mail.Session) null);
    when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

    byte[] pdfContent = {(byte) 0x25, (byte) 0x50, (byte) 0x44, (byte) 0x46};
    emailService.sendEmailWithAttachment(
        "customer@example.com",
        "Invoice",
        "See attachment",
        "invoice.pdf",
        pdfContent,
        "application/pdf");

    verify(mailSender).send(mimeMessage);
  }

  @Test
  @DisplayName("does not send email with attachment when disabled")
  void doesNotSendEmailWithAttachmentWhenDisabled() {
    emailProperties.setEnabled(false);

    byte[] pdfContent = {(byte) 0x25, (byte) 0x50, (byte) 0x44, (byte) 0x46};
    emailService.sendEmailWithAttachment(
        "customer@example.com",
        "Invoice",
        "See attachment",
        "invoice.pdf",
        pdfContent,
        "application/pdf");

    verifyNoInteractions(mailSender);
  }
}
