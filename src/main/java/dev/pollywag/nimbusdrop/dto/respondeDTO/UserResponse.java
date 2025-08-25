package dev.pollywag.nimbusdrop.dto.respondeDTO;

import dev.pollywag.nimbusdrop.entity.Role;

public class UserResponse {
    private Long id;
    private String userDisplayName;
    private String email;
    private Role role;

    public UserResponse() { super();}

    public String getUserDisplayName() {
        return userDisplayName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUserDisplayName(String userDisplayName) {
        this.userDisplayName = userDisplayName;
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
