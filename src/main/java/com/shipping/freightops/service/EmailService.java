package com.shipping.freightops.service;

public interface EmailService {

  void sendEmail(String to, String subject, String body);

  void sendEmailWithAttachment(
      String to,
      String subject,
      String body,
      String attachmentName,
      byte[] attachmentContent,
      String mimeType);
}
