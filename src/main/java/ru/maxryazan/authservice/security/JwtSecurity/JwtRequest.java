package ru.maxryazan.authservice.security.JwtSecurity;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class JwtRequest {

    private String login;
    private String password;

}