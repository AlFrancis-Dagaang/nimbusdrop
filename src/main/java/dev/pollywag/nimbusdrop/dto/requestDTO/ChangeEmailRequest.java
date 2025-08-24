package dev.pollywag.nimbusdrop.dto.requestDTO;

import jakarta.validation.constraints.NotBlank;

public class ChangeEmailRequest {
    @NotBlank(message = "New email is required")
    private String newEmail;

    @NotBlank(message = "New password is required")
    private String password;

    public String getNewEmail() {
        return newEmail;
    }

    public void setNewEmail(String newEmail) {
        this.newEmail = newEmail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
