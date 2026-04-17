package dev.umang.userauthservice_17_04_2026.models;

import jakarta.persistence.Entity;

@Entity
public class Role extends BaseModel{
    private String value; // e.g. "ADMIN", "USER", "MODERATOR"

    //LIST<PRIVILEGES>

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
