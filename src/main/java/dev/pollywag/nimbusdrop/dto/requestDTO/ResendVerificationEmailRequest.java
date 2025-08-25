package dev.pollywag.nimbusdrop.dto.requestDTO;

import jakarta.validation.constraints.NotBlank;

public class ResendVerificationEmailRequest {
    @NotBlank(message = "email is required")
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
