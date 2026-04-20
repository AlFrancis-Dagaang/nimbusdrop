package dev.pollywag.nimbusdrop.dto.respondeDTO;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class AuthResponse {

    private String accessToken;
    private String tokenType = "Bearer";
    private UserResponse user;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiresAt;

    // Constructors
    public AuthResponse() {
    }

    public AuthResponse(String accessToken, UserResponse userResponse, LocalDateTime expiresAt) {
        this.accessToken = accessToken;

        this.expiresAt = expiresAt;
        this.user = userResponse;
    }

    public UserResponse getUser() {
        return user;
    }

    public void setUser(UserResponse user) {
        this.user = user;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }


    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }


}
