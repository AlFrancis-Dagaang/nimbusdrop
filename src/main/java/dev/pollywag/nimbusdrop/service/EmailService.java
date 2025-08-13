package dev.pollywag.nimbusdrop.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendConfirmationEmail(String toEmail, String token) {
        String confirmUrl = "http://localhost:8085/auth/confirm?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Confirm your account");
        message.setText("Click the link to confirm your account: " + confirmUrl);

        mailSender.send(message);
    }
}