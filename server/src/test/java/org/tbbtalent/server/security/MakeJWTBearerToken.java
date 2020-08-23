/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.security;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.Signature;
import java.text.MessageFormat;

import io.jsonwebtoken.io.Encoders;

/**
 * TODO JC Doc
 *
 * @author John Cameron
 */
public class MakeJWTBearerToken {
    public static void main(String[] args) {
        String header = "{\"alg\":\"RS256\"}";
        String claimTemplate = "'{'" +
                "\"iss\": \"{0}\", " +
                "\"sub\": \"{1}\", " +
                "\"aud\": \"{2}\", " +
                "\"exp\": \"{3}\", " +
                "\"jti\": \"{4}\"" +
                "'}'";

        try {
            StringBuffer token = new StringBuffer();

            //Encode the JWT Header and add it to our string to sign
            token.append(Encoders.BASE64.encode(header.getBytes(StandardCharsets.UTF_8)));

            //Separate with a period
            token.append(".");

            //Create the JWT Claims Object
            String[] claimArray = new String[4];
            claimArray[0] = "3MVG9mclR62wycM3f9iy572tIEVmeyMN8eFW5h2BK7eD96hD19zYvpx1vup07kfpCidboRyF56WF3QjL7LAYl";
            claimArray[1] = "jcameron@talentbeyondboundaries.org";
            claimArray[2] = "https://login.salesforce.com";
            claimArray[3] = Long.toString( ( System.currentTimeMillis()/1000 ) + 300);
            MessageFormat claims;
            claims = new MessageFormat(claimTemplate);
            String payload = claims.format(claimArray);

            //Add the encoded claims object
            token.append(Encoders.BASE64.encode(payload.getBytes(StandardCharsets.UTF_8)));

            //Load the private key from a keystore
            PrivateKey privateKey = null; //todo Load from text

            //Sign the JWT Header + "." + JWT Claims Object
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(token.toString().getBytes(StandardCharsets.UTF_8));
            String signedPayload = Encoders.BASE64.encode(signature.sign());

            //Separate with a period
            token.append(".");

            //Add the encoded signature
            token.append(signedPayload);

            System.out.println(token.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
            
    }
}
