package ru.maxryazan.authservice.model;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

@RequiredArgsConstructor
public enum Role implements GrantedAuthority {
    USER("USER"),
    MANAGER("MANAGER");

    private final String value;

    @Override
    public String getAuthority() {
        return value;
    }
}
