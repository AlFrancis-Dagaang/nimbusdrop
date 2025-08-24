package dev.pollywag.nimbusdrop.dto.requestDTO;

import jakarta.validation.constraints.NotBlank;

public class ChangeUsernameRequest {
    @NotBlank(message = "Username is required")
    private String newUsername;

    public String getNewUsername() {
        return newUsername;
    }

    public void setNewUsername(String newUsername) {
        this.newUsername = newUsername;
    }
}
