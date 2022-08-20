package ru.maxryazan.authservice.service;

import io.jsonwebtoken.Claims;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import ru.maxryazan.authservice.model.Client;
import ru.maxryazan.authservice.security.JwtSecurity.JwtProvider;
import ru.maxryazan.authservice.security.JwtSecurity.JwtRequest;
import ru.maxryazan.authservice.security.JwtSecurity.JwtResponse;
import javax.security.auth.message.AuthException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final ClientService clientService;
    private final JwtProvider jwtProvider;
    private final BCryptPasswordEncoder passwordEncoder;
    Jedis jedis = new Jedis();


    public JwtResponse login(@NonNull JwtRequest authRequest) throws AuthException {
        final Client client = clientService.findByPhoneNumber(authRequest.getLogin())
                .orElseThrow(() -> new AuthException("client not found"));
        if (passwordEncoder.matches(authRequest.getPassword(), client.getHashPin())) {
            final String accessToken = jwtProvider.generateAccessToken(client);
            final String refreshToken = jwtProvider.generateRefreshToken(client);

            jedis.set(client.getPhoneNumber(), refreshToken);
            return new JwtResponse(accessToken, refreshToken);
        } else {
            throw new AuthException("password incorrect");
        }
    }


    public JwtResponse getAccessToken(@NonNull String refreshToken) throws AuthException {
        if (jwtProvider.validateRefreshToken(refreshToken)) {
            final Claims claims = jwtProvider.getRefreshClaims(refreshToken);
            final String phoneNumber = claims.getSubject();

            final String saveRefreshToken = jedis.get(phoneNumber);
            if (saveRefreshToken != null && saveRefreshToken.equals(refreshToken)) {
                final Client client = clientService.findByPhoneNumber(phoneNumber)
                        .orElseThrow(() -> new AuthException("Пользователь не найден"));
                final String accessToken = jwtProvider.generateAccessToken(client);
                return new JwtResponse(accessToken, null);
            }
        }
        return new JwtResponse(null, null);
    }


    public JwtResponse refresh(@NonNull String refreshToken) throws AuthException {
        if (jwtProvider.validateRefreshToken(refreshToken)) {
            final Claims claims = jwtProvider.getRefreshClaims(refreshToken);
            final String phoneNumber = claims.getSubject();

            final String saveRefreshToken = jedis.get(phoneNumber);

            if (saveRefreshToken != null && saveRefreshToken.equals(refreshToken)) {
                Client client = clientService.findByPhoneNumber(phoneNumber)
                        .orElseThrow(() -> new AuthException("Пользователь не найден"));
                final String accessToken = jwtProvider.generateAccessToken(client);
                final String newRefreshToken = jwtProvider.generateRefreshToken(client);

                jedis.set(client.getPhoneNumber(), newRefreshToken);
                return new JwtResponse(accessToken, refreshToken);
            }
        }
        throw new AuthException("Невалидный JWT токен");
    }

}
