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

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.MessageFormat;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;

/**
 * Comes from https://help.salesforce.com/articleView?id=remoteaccess_oauth_jwt_flow.htm&type=5
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
                "\"exp\": \"{3}\"" +
                "'}'";
        String privateKeyStr = "-----BEGIN PRIVATE KEY-----\n" +
                "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAMrBTtv5Yh0gn0MW\n" +
                "ufbANxrprRWlYaiS8aDVlT61F1pShyLysqeEjEYczwxQeCaBzDxYxdSRjnn0oQLI\n" +
                "8bZSM6W1VGEpA2jXsoXoNyJRNTy0VvsgFijrv2DP+kmO5H9EFSlRC9rzllyRMyeu\n" +
                "0WMWSvb1A5y6yQoomoKOvkv6G8xnAgMBAAECgYEAnVLJgd5LxxYc/c2QlmonV/ah\n" +
                "mv4sfMUoQAf6OiIB8M/Ak9mFzn4G6hBIh+GYmSh19Q1c08ftqaurk6GgDDxUXYph\n" +
                "rHHDIZZcilDtFuyd+zVgegXrrW4pthlqNBLIvJ9p4uuckhpPAbzxzKwTGFcMXwd/\n" +
                "22E4ZKB/Sf4XJN1aWyECQQDpNZh+Ylf5s0OAf9RbAwwTeMTczCyvTMhz7k6MqGAP\n" +
                "4ah1f5Et3RkBOytE5FDNT1wwVtWc4FEzDkMOHjPj5gorAkEA3pHPwe39834ZHf95\n" +
                "z6tvVnvoI854EY94WKmLQ6fL9RRhUNPi4OxKXm8wJ/V3ZtJ0WQdhE/FFeEG1f18E\n" +
                "WaHUtQJAQl86k12x5CMc5wl6ipyHZ1NL0/tYDFwyAKymNmoFTP/QTgCMdR0j7LHG\n" +
                "UskYJhacCjXsfcVp1roMY4w9AHOMGQJABdyVOihIbec+Rhn6XUvIjOCKhpbjdqLu\n" +
                "qnccodWDe5rjzTsnWIEgnEgVXpgKYvzb75RQLDRIfhhM7WPVO38VmQJBAKaPn7//\n" +
                "eafJZMpvsfAiIgaeV5Is3SwBhTWtmZaC+DJx3GOPin7fQzXXvQz19+haQdhMGE16\n" +
                "gb0l/65SHf4O8FM=\n" +
                "-----END PRIVATE KEY-----";

        try {
            StringBuilder token = new StringBuilder();

            //Encode the JWT Header and add it to our string to sign
            token.append(Encoders.BASE64URL.encode(header.getBytes(StandardCharsets.UTF_8)));

            //Separate with a period
            token.append(".");

            //Create the JWT Claims Object
            String[] claimArray = new String[4];
            claimArray[0] = "3MVG9mclR62wycM3f9iy572tIEVmeyMN8eFW5h2BK7eD96hD19zYvpx1vup07kfpCidboRyF56WF3QjL7LAYl";
            claimArray[1] = "jcameron@talentbeyondboundaries.org";
            claimArray[2] = "https://login.salesforce.com";
            claimArray[3] = Long.toString( ( System.currentTimeMillis()/1000 ) + 180);
            MessageFormat claims;
            claims = new MessageFormat(claimTemplate);
            String payload = claims.format(claimArray);

            //Add the encoded claims object
            token.append(Encoders.BASE64URL.encode(payload.getBytes(StandardCharsets.UTF_8)));

            PrivateKey privateKey = privateKeyFromPkcs8(privateKeyStr);

            //Sign the JWT Header + "." + JWT Claims Object
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(token.toString().getBytes(StandardCharsets.UTF_8));
            String signedPayload = Encoders.BASE64URL.encode(signature.sign());

            //Separate with a period
            token.append(".");

            //Add the encoded signature
            token.append(signedPayload);

            final String s = token.toString();
            System.out.println(s);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static PrivateKey privateKeyFromPkcs8(String privateKeyPem)
            throws GeneralSecurityException {

        // strip the headers and new lines
        privateKeyPem = privateKeyPem.replace("-----BEGIN PRIVATE KEY-----", "");
        privateKeyPem = privateKeyPem.replace("-----END PRIVATE KEY-----", "");
        privateKeyPem = privateKeyPem.replaceAll(System.lineSeparator(), "");

        byte[] encoded = Decoders.BASE64.decode(privateKeyPem);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
        return privateKey;
    }

}
