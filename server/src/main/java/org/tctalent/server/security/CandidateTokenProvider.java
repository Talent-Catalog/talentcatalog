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
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Generates candidate token strings which encode a candidateNumber and an expiry time.
 * These strings are used to construct the obscure urls which display candidate cvs when
 * processed by the public-portal.
 * <p/>
 * The idea of these is:
 * <ul>
 *     <li>Hide the actual candidate number so that people can't generate urls to look at
 *     any candidate. They can only use the urls that we give them.</li>
 *     <li>To provide an expiry of these urls, so that they become unusable after a time.</li>
 * </ul>
 * <p/>
 * The strings are just JWT's - built using the excellent and well documented io.jsonwebtoken code.
 * See https://github.com/jwtk/jjwt
 * <p/>
 * It uses the same JWT secret key as the tokens we use for authorization in {@link JwtTokenProvider}
 *
 * @author John Cameron
 */
@Service
public class CandidateTokenProvider implements InitializingBean {

    @Value("${jwt.secret}")
    private String jwtSecretBase64;

    private Key jwtSecret;

    @Override
    public void afterPropertiesSet() {
        //Once the properties have been set from the config file, convert the
        //String version of the key into a Key object.
        this.jwtSecret = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecretBase64));
    }

    /**
     * Generates a JWT token string encoding a candidate number. The token will expire after the
     * given number of days.
     * @param candidateNumber A candidate number
     * @param expiryTimeInDays Token expiry in days from now
     * @return Candidate token string
     */
    public String generateToken(String candidateNumber, long expiryTimeInDays) {

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiryTimeInDays * 24 * 60 * 60 * 1000);

        return Jwts.builder()
                .setSubject(candidateNumber)
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(jwtSecret, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Generates a JWT token string encoding a candidate number. The token will expire after the
     * given number of days.
     * @param claims A candidate number, occupations to include
     * @param expiryTimeInDays Token expiry in days from now
     * @return Candidate token string
     */
    public String generateCvToken(CvClaims claims, long expiryTimeInDays) {

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiryTimeInDays * 24 * 60 * 60 * 1000);

        return Jwts.builder()
                .setSubject(claims.candidateNumber())
                .claim("RestrictCandidateOccupations", String.valueOf(claims.restrictCandidateOccupations()))
                .claim("CandidateOccupations", claims.candidateOccupationIds())
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(jwtSecret, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Retrieves the candidate number from the token, otherwise throws exceptions if the token
     * is not valid or has expired.
     * @param token Candidate token string
     * @return Candidate number
     * @throws JwtException if there are any problems with the token.
     */
    public String getCandidateNumberFromToken(String token) throws JwtException {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(jwtSecret)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    /**
     * Retrieves the candidate number and a list of candidate occupations from the token, otherwise throws
     * exceptions if the token is not valid or has expired.
     * @param token Candidate token string
     * @return CvClaims
     * @throws JwtException if there are any problems with the token.
     */
    public CvClaims getCvClaimsFromToken(String token) throws JwtException {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(jwtSecret)
                .build()
                .parseClaimsJws(token)
                .getBody();

        boolean restrictCandidateOccupations = "true".equals(claims.get("RestrictCandidateOccupations", String.class));
        ArrayList ids = claims.get("CandidateOccupations", ArrayList.class);

        return new CvClaims(claims.getSubject(), restrictCandidateOccupations, toList(ids));
    }

    private List<Long> toList(ArrayList ids) {
        ArrayList<Long> longs = new ArrayList<>();
        if( ids != null) {
            for (Object id : ids) {
                longs.add(((Number) id).longValue());
            }
        }
        return longs;
    }


    void setJwtSecretBase64(String jwtSecretBase64) {
        this.jwtSecretBase64 = jwtSecretBase64;
        this.jwtSecret = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecretBase64));
    }
}
