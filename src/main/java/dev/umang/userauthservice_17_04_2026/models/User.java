package dev.umang.userauthservice_17_04_2026.models;

import dev.umang.userauthservice_17_04_2026.dtos.UserDTO;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;

import java.util.ArrayList;
import java.util.List;

@Entity
public class User extends BaseModel{
    private String username;
    private String email;
    private String passwordHash;
    @ManyToMany
    private List<Role> roles = new ArrayList<>();

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

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public UserDTO convertToUserDTO(){
        UserDTO userDTO = new UserDTO();
        userDTO.setId(this.getId());
        userDTO.setName(this.username);
        userDTO.setEmail(this.email);
        userDTO.setRoles(this.roles);
        return userDTO;
    }
}

/*
User : Role
1 M
M 1 -> M:M
1 user can have m roles (read forwards)
m users to associated can role 1 (read backwards)
 */