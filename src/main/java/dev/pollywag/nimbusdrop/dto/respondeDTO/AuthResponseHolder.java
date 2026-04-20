package dev.pollywag.nimbusdrop.dto.respondeDTO;

public class AuthResponseHolder {
    private final AuthResponse authResponse;
    private final String refreshToken;

    public AuthResponseHolder(AuthResponse authResponse, String refreshToken) {
        this.authResponse = authResponse;
        this.refreshToken = refreshToken;
    }

    public AuthResponse getAuthResponse() {
        return authResponse;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

}
