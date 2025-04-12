package com.delivery.driverauthservice.security;

import com.delivery.driverauthservice.model.DriverCredential;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public String generateToken(DriverCredential driver) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", driver.getUsername());
        claims.put("driverId", driver.getDriverId());
        claims.put("roles", driver.getRoles());

        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .claims(claims)
                .subject(driver.getUsername())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(key, Jwts.SIG.HS512)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Authentication getAuthentication(String token) {
        Claims claims = getClaimsFromToken(token);

        Long driverId = claims.get("driverId", Long.class);

        // Get roles as a List
        Collection<? extends GrantedAuthority> authorities = ((List<String>) claims.get("roles"))
                .stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        CustomUserDetails userDetails = new CustomUserDetails(claims.getSubject(), "", authorities);
        userDetails.setDriverId(driverId);

        return new UsernamePasswordAuthenticationToken(userDetails, "", authorities);
    }

    public Long getDriverIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("driverId", Long.class);
    }

    public long getTokenExpirationInSeconds() {
        return jwtExpirationMs / 1000;
    }
}