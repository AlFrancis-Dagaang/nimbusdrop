package dev.pollywag.nimbusdrop.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final SimpleMailMessage message = new SimpleMailMessage();

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendConfirmationEmail(String toEmail, String token) {
        String confirmUrl = "http://localhost:8085/auth/confirm?token=" + token;

        message.setTo(toEmail);
        message.setSubject("Confirm your account");
        message.setText("Click the link to confirm your account: " + confirmUrl);

        mailSender.send(message);
    }

    public void sendConfirmationNewEmail(String toEmail, String token) {
        String confirmUrl = "http://localhost:8085/auth/new-email?token=" + token;

        message.setTo(toEmail);
        message.setSubject("Confirm your email");
        message.setText("Click the link to confirm your email: " + confirmUrl);

        mailSender.send(message);
    }

    public void sendTokenCodeForDeletion(String toEmail, String token) {
        message.setTo(toEmail);
        message.setSubject("Confirm Your Account Deletion");

        String body = String.format(
                "Hello,\n\n" +
                        "We received a request to delete your account. " +
                        "If you really want to proceed, please enter the following confirmation code in the app:\n\n" +
                        "    %s\n\n" +
                        "⚠️ This code will expire in 10 minutes. If you did not request account deletion, please ignore this email.\n\n" +
                        "Thanks,\n" +
                        "YourApp Team",
                token
        );

        message.setText(body);
        mailSender.send(message);
    }

    public void sendForgotPasswordLink(String toEmail, String token) {
        message.setTo(toEmail);
        message.setSubject("Reset Your Password");

        String resetUrl = String.format("http://localhost:8085/auth/reset-password?token=%s", token);

        String body = String.format(
                "Hello,\n\n" +
                        "Click the link below to reset your password:\n\n" +
                        "    %s\n\n" +
                        "This link will expire in 10 minutes.\n\n" +
                        "If you didn’t request a password reset, please ignore this email.\n\n" +
                        "Thanks,\n" +
                        "Nimbusdrop Team",
                resetUrl
        );

        message.setText(body);
        mailSender.send(message);
    }






}