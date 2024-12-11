/*
 * Copyright (c) 2024 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import java.security.Key;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.Role;

/**
 * Originally taken from
 * https://www.callicoder.com/spring-boot-spring-security-jwt-mysql-react-app-part-2/
 * <p/>
 * Updated to use latest version of io.jsonwebtoken library.
 * See https://github.com/jwtk/jjwt
 * <p/>
 * See also MakeSecretKey in the test source which is simple way of generating
 * a Base64 secret key suitable for putting in the configuration.
 */
@Component
@Slf4j
public class JwtTokenProvider implements InitializingBean {

    public static final String EXPIRED_OR_INVALID_TOKEN_MSG = "Expired or invalid JWT token";

    @Value("${jwt.secret}")
    private String jwtSecretBase64;

    @Value("${jwt.expirationInMs}")
    private int jwtExpirationInMs;

    private Key jwtSecret;

    @Override
    public void afterPropertiesSet() {
        //Once the properties have been set from the config file, convert the
        //String version of the key into a Key object.
        this.jwtSecret = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecretBase64));
    }

    public String generateToken(Authentication authentication) {

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);
        String subject = "";

        if (authentication.getPrincipal() instanceof TcUserDetails user) {
            subject = user.getUsername();

            //Candidates can stay logged in forever
            if (Role.user.equals(user.getUser().getRole())) {
                expiryDate = null;
            }
        }

        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(jwtSecret, SignatureAlgorithm.HS512)
                .compact();
    }

    public String getUsernameFromJwt(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(jwtSecret)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(jwtSecret).build().parseClaimsJws(authToken);
            return true;
        } catch (SignatureException ex) {
            LogBuilder.builder(log)
                .action("validateToken")
                .message("Invalid JWT signature: " + ex.getMessage())
                .logError();
        } catch (MalformedJwtException ex) {
            LogBuilder.builder(log)
                .action("validateToken")
                .message("Invalid JWT token: " + ex.getMessage())
                .logError();
        } catch (ExpiredJwtException ex) {
            LogBuilder.builder(log)
                .action("validateToken")
                .message("Expired JWT token for: " + ex.getClaims().getSubject())
                .logError();
        } catch (UnsupportedJwtException ex) {
            LogBuilder.builder(log)
                .action("validateToken")
                .message("Unsupported JWT token: " + ex.getMessage())
                .logError();
        } catch (IllegalArgumentException ex) {
            LogBuilder.builder(log)
                .action("validateToken")
                .message("JWT claims string is empty: " + ex.getMessage())
                .logError();
        }
        return false;
    }
}
