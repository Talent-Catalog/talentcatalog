/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.service.db.impl;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.tbbtalent.server.exception.ReCaptchaInvalidException;
import org.tbbtalent.server.response.GoogleRecaptchaResponse;
import org.tbbtalent.server.service.db.CaptchaService;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Captcha service implementation
 * <p/>
 * See https://developers.google.com/recaptcha/docs/v3
 * and
 * https://www.baeldung.com/spring-security-registration-captcha
 * @author John Cameron
 */
@Service
public class CaptchaServiceImpl implements CaptchaService {
    private final LoadingCache<String, Integer> attemptsCache;

    private final static Logger log = LoggerFactory.getLogger(CaptchaServiceImpl.class);

    protected static final String RECAPTCHA_URL_TEMPLATE = 
            "https://www.google.com/recaptcha/api/siteverify?secret=%s&response=%s&remoteip=%s";

    @Autowired
    protected HttpServletRequest request;

    public CaptchaServiceImpl() {
        attemptsCache = CacheBuilder.newBuilder()
                .expireAfterWrite(4, TimeUnit.HOURS)
                .build(new CacheLoader<String, Integer>() {
            @Override
            public Integer load(final String key) {
                return 0;
            }
        });
    }

    @Override
    public void processCaptchaV3Token(String token, final String action) 
            throws ReCaptchaInvalidException {
        if(isBlocked(getClientIP())) {
            throw new ReCaptchaInvalidException(
                    "Client exceeded maximum number of failed reCaptcha attempts");
        }

        final URI verifyUri = URI.create(String.format(
                RECAPTCHA_URL_TEMPLATE, getReCaptchaSecret(), token, getClientIP()));
        try {
            RestTemplate restTemplate = new RestTemplate();
            final GoogleRecaptchaResponse googleRecaptchaResponse = 
                    restTemplate.getForObject(verifyUri, GoogleRecaptchaResponse.class);
            if (googleRecaptchaResponse == null) {
                throw new ReCaptchaInvalidException(
                        "No reCaptcha validation response received from Google");
            }
            
            log.info("Google's response: {} ", googleRecaptchaResponse.toString());

            if (!googleRecaptchaResponse.isSuccess() || 
                    !googleRecaptchaResponse.getAction().equals(action) || 
                    googleRecaptchaResponse.getScore() < 0.5) {
                reCaptchaFailed(getClientIP());
                throw new ReCaptchaInvalidException(
                        "reCaptcha was not successfully validated");
            }
        } catch (RestClientException rce) {
            throw new ReCaptchaInvalidException(
                    "System unavailable at this time.  Please try again later.", rce);
        }
        reCaptchaSucceeded(getClientIP());
    }


    private void reCaptchaSucceeded(final String key) {
        attemptsCache.invalidate(key);
    }

    private void reCaptchaFailed(final String key) {
        int attempts = attemptsCache.getUnchecked(key);
        attempts++;
        attemptsCache.put(key, attempts);
    }

    private boolean isBlocked(final String key) {
        int MAX_ATTEMPT = 4;
        return attemptsCache.getUnchecked(key) >= MAX_ATTEMPT;
    }


    protected String getClientIP() {
        final String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

    /**
     * From https://www.google.com/recaptcha/admin
     * @return Google reCaptcha site key for tbbtalent.org
     */
    @Override
    public String getReCaptchaSite() {
        return "6Lc_97cZAAAAAIDqR7gT3h_ROGU6P7Jif-wEk9Vu";
    }

    /**
     * From https://www.google.com/recaptcha/admin
     * @return Google reCaptcha secret key for tbbtalent.org
     */
    @Override
    public String getReCaptchaSecret() {
        return "6Lc_97cZAAAAAMq7btt9QX9jQeutnZAlN6FkWXAT";
    }
}
