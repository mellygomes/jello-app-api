package com.jello.jello_app.security.jwt;

import com.jello.jello_app.auth.exception.ExpiredTokenException;
import com.jello.jello_app.auth.exception.InvalidTokenException;
import com.jello.jello_app.security.user.AppUserDetails;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtils {
    @Value("${auth.token.jwtSecret}")
    private String jwtSecret;

    @Value("${auth.token.expirationInMils}")
    private Integer expirationTime;

    public String generateTokenForUser(Authentication authentication) {
        AppUserDetails userPrincipal = (AppUserDetails) authentication.getPrincipal();

        List<String> roles = userPrincipal.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority).toList();

        return Jwts.builder()
                .subject(userPrincipal.getId().toString())
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(key())
                .compact();
    }

    public String getJwtFromCookie(HttpServletRequest request) {

        if (request.getCookies() == null) {
            return null;
        }

        for (Cookie cookie : request.getCookies()) {
            String TOKEN = "access_token";
            if (TOKEN.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith((SecretKey) key())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException ex) {
            throw new ExpiredTokenException(ex);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new InvalidTokenException("Token inválido", ex);
        }
    }

    public Long getUserIdFromToken(String token) {
        String id = Jwts.parser()
                .verifyWith((SecretKey) key())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
        return Long.valueOf(id);
    }

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }
}
