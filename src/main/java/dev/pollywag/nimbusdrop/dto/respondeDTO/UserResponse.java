package dev.pollywag.nimbusdrop.dto.respondeDTO;

import dev.pollywag.nimbusdrop.entity.Role;

public class UserResponse {

    private String username;
    private String email;
    private Role role;

    public UserResponse() { super();}

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
