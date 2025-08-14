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

    public void sendDeleteTokenEmail(String toEmail, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("NimbusDrop - Confirm Deletion Request");
        message.setText(
                "Hello,\n\n" +
                        "A request was made to delete an item in your NimbusDrop account.\n" +
                        "Please confirm this action by entering the following token in the app:\n\n" +
                        token + "\n\n" +
                        "This token will expire in 5 minutes. If you did not request this, you can ignore this email.\n\n" +
                        "Stay safe,\n" +
                        "NimbusDrop Security Team"
        );
        mailSender.send(message);
    }
}