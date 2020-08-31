/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.security;

import javax.crypto.SecretKey;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;

/**
 * Creates a nice secret key in the form of a Base64 string which can
 * be read from configuration.
 * <p/>
 * Run run main and use the key displayed on System.out.
 * <p/>
 * See https://github.com/jwtk/jjwt#jws-key-create-secret
 * @author John Cameron
 */
public class MakeSecretKey {
    public static void main(String[] args) {
        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        String secretString = Encoders.BASE64.encode(key.getEncoded());
        System.out.println("'" + secretString + "'");
    }
}
