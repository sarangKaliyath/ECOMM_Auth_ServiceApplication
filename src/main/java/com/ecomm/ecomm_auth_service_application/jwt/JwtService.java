package com.ecomm.ecomm_auth_service_application.jwt;

import com.ecomm.ecomm_auth_service_application.model.Role;
import com.ecomm.ecomm_auth_service_application.model.User;
import com.ecomm.ecomm_auth_service_application.security.UserPrincipal;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class JwtService {

    @Autowired
    private SecretKey secretKey;

    public String createAccessToken(UserPrincipal userPrincipal) {

        Map<String, Object> claims = new HashMap<>();

        claims.put("userId", userPrincipal.getId());
        claims.put(
                "roles",
                userPrincipal.getRoles()
        );
        claims.put("email", userPrincipal.getEmail());

        long now = System.currentTimeMillis();

        Date issuedAt = new Date(now);
        Date expiryAt = new Date(now + TimeUnit.MINUTES.toMillis(15));

        return Jwts.builder()
                .subject(userPrincipal.getId().toString())
                .claims(claims)
                .issuedAt(issuedAt)
                .expiration(expiryAt)
                .issuer("ecommerce-auth-service")
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public void validateAccessToken(String token) {

        JwtParser parser = Jwts.parser()
                .verifyWith(secretKey)
                .build();

        parser.parseSignedClaims(token);
    }

}