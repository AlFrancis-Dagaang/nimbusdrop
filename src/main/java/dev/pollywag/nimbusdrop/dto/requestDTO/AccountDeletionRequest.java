package dev.pollywag.nimbusdrop.dto.requestDTO;

import jakarta.validation.constraints.NotBlank;

public class AccountDeletionRequest {

    @NotBlank(message = "token is required")
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
