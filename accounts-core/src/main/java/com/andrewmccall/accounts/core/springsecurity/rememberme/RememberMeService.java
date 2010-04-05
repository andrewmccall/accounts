/*
 * Copyright (c) 2010. Andrew McCall [andrew@andrewmccall.com] - All Rights Reserved.
 *
 * Unless explicitly stated otherwise, all rights are owned by or controlled by Andrew McCall.
 *
 * Except as otherwise expressly permitted under copyright law the content not be copied, reproduced,
 * republished, downloaded, posted, broadcast or transmitted in any way without first obtaining Andrew
 * McCall's written permission or that of the copyright owner.
 */

package com.andrewmccall.accounts.core.springsecurity.rememberme;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.rememberme.AbstractRememberMeServices;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationException;
import org.springframework.security.web.authentication.rememberme.CookieTheftException;
import org.springframework.security.web.authentication.rememberme.InvalidCookieException;
import org.springframework.stereotype.Service;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.annotation.Resource;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Date;

import com.andrewmccall.accounts.core.AccountService;
import com.andrewmccall.accounts.core.AccountsException;
import com.andrewmccall.accounts.core.User;

/**
 * A secure RememberMeService implementation. The one from Spring Security has two problems. First it doesn't support
 * JPA and we don't want to support more than one persistence framework. Second the SS implemenation may have colision
 * issues because it doesn't store the username. There is a potential issue that a user could be improperly informed
 * that their cookie had been intercepted. To counteract that, we're storing both the user as well as the series, also
 * we're making sure an exisiting series is not outstanding for the same user.
 */
@Service("rememberMeService")
public class RememberMeService extends AbstractRememberMeServices {

    private SecureRandom random;

    public static final int DEFAULT_SERIES_LENGTH = 16;
    public static final int DEFAULT_TOKEN_LENGTH = 16;

    private int seriesLength = DEFAULT_SERIES_LENGTH;
    private int tokenLength = DEFAULT_TOKEN_LENGTH;

    private static final String DEFAULT_KEY = "com.andrewmccall.accounts.RemembermeKey";
    private static final String DEFAULT_COOKIE = "cookieName";
    private static final String DEFAULT_PARAMETER = "_remember_me";

    private final Log log = LogFactory.getLog(this.getClass());

    @Resource
    private TokenRepository tokenRepository;

    @Resource
    private AccountService accountService;

    public RememberMeService() throws Exception {
        random = SecureRandom.getInstance("SHA1PRNG");
        setKey(DEFAULT_KEY);
        setParameter(DEFAULT_PARAMETER);
        setCookieName(DEFAULT_COOKIE);
    }

    public UserDetails processAutoLoginCookie(String[] cookieTokens, HttpServletRequest request, HttpServletResponse response) throws RememberMeAuthenticationException, UsernameNotFoundException {

        if (cookieTokens.length != 3) {
            if (log.isDebugEnabled())
                log.debug("Cookie token did not contain 3 tokens, it contained '" + Arrays.asList(cookieTokens) + "'");
            throw new InvalidCookieException("Cookie token did not contain 3 tokens, it contained '" + cookieTokens.length + "'");
        }

        final String presentedId = cookieTokens[0];
        final String presentedSeries = cookieTokens[1];
        final String presentedToken = cookieTokens[2];

        if (log.isInfoEnabled()) {
            StringBuffer buff = new StringBuffer("Processing auto login cookie for cookie: ");
            buff.append(presentedId);
            buff.append(" series: ");
            buff.append(presentedSeries);
            buff.append(" token: ");
            buff.append(presentedToken);
            log.info(buff);
        }

        User user;
        try {
            user = accountService.getUser(presentedId);
        } catch (AccountsException e) {
            if (log.isWarnEnabled())
                log.warn("Could not load user, AccountsException thrown.", e);
            throw new RememberMeAuthenticationException("Exception loading user.");
        }
        if (user == null) {
            if (log.isDebugEnabled())
                log.debug("A user for the id provided does not exist.");
            throw new RememberMeAuthenticationException("No user could be loaded for presentedId: " + presentedId);
        }

        RememberMeToken token = null;
        try {
            token = tokenRepository.getToken(presentedSeries, user);
        } catch (AccountsException e) {
            if (log.isWarnEnabled())
                log.warn("Exception getting RememberMeToken, treating as invalid.", e);
        }
        if (token == null) {
            // No series match, so we can't authenticate using this cookie
            throw new RememberMeAuthenticationException("No persistent token found for series id: " + presentedSeries);
        }

        // We have a match for this user/series combination
        if (!presentedToken.equals(token.getValue())) {
            // Token doesn't match series value. Delete all logins for this user and throw an exception to warn them.
            tokenRepository.removeUserTokens(user);
            throw new CookieTheftException(messages.getMessage("PersistentTokenBasedRememberMeServices.cookieStolen",
                    "Invalid remember-me token (Series/token) mismatch. Implies previous cookie theft attack."));
        }

        if (log.isTraceEnabled())
            log.trace("Checking token is newer than " + (new Date(System.currentTimeMillis() - (getTokenValiditySeconds() * 1000))) + " Token date: " + token.getDate());

        if (token.getDate().getTime() + (getTokenValiditySeconds() * 1000) < System.currentTimeMillis()) {
            if (log.isInfoEnabled())
                log.info("The token has expired.");
            throw new RememberMeAuthenticationException("Remember-me getUserForLogin has expired");
        }

        // Token also matches, so getUserForLogin is valid. Update the token value, keeping the *same* series number.
        if (log.isDebugEnabled()) {
            log.debug("Refreshing persistent login token for user '" + token.getUser() + "', series '" +
                    token.getSeries() + "'");
        }

        try {
            token.setDate(new Date());
            token.setValue(generateTokenData());
            tokenRepository.update(token);

            addCookie(token, request, response);
        } catch (AccountsException e) {
            // failure to store the token because of an exception is an issue, not one to show the user though. Worst
            // case they just have to enter their password next time.
            if (log.isWarnEnabled())
                log.warn("Error processing login success. The user will have to enter their password again when they next login.", e);
        }
        return getUserDetailsService().loadUserByUsername(user.getId().toString());
    }

    /**
     * Creates a new persistent login token with a new series number, stores the data in the
     * persistent token repository and adds the corresponding cookie to the response.
     */
    protected void onLoginSuccess(HttpServletRequest request, HttpServletResponse response, Authentication successfulAuthentication) {
        User user;
        try {
            user = accountService.getUser(successfulAuthentication.getName());
        } catch (AccountsException e) {
            log.error("Exception thrown getting user. Cannot process login, no cookies or tokens will be stored.", e);
            return;
        }

        if (log.isDebugEnabled())
            log.debug("Creating new persistent getUserForLogin for user " + user);

        String series = generateSeriesData();
        try {
            while (tokenRepository.exists(series, user)) {
                if (log.isTraceEnabled())
                    log.trace("The series was in use - generating a new one.");
                series = generateSeriesData();
            }
            RememberMeToken persistentToken = new RememberMeToken(series, user, generateTokenData(), new Date());
            tokenRepository.create(persistentToken);
            addCookie(persistentToken, request, response);
        } catch (AccountsException e) {
            // failure to store the token because of an exception is an issue, not one to show the user though. Worst
            // case they just have to enter their password next time.
            if (log.isWarnEnabled())
                log.warn("Error processing auto login with cookie. The user's will not be updated.", e);
        }

    }

    protected void addCookie(RememberMeToken token, HttpServletRequest request, HttpServletResponse response) {
        setCookie(new String[]{token.getUser().getId().toString(), token.getSeries(), token.getValue()}, getTokenValiditySeconds(), request, response);
    }

    protected String generateSeriesData() {
        byte[] newSeries = new byte[seriesLength];
        random.nextBytes(newSeries);
        return new String(Base64.encodeBase64(newSeries));
    }

    protected String generateTokenData() {
        byte[] newToken = new byte[tokenLength];
        random.nextBytes(newToken);
        return new String(Base64.encodeBase64(newToken));
    }

    public int getSeriesLength() {
        return seriesLength;
    }

    public void setSeriesLength(int seriesLength) {
        this.seriesLength = seriesLength;
    }

    public int getTokenLength() {
        return tokenLength;
    }

    public void setTokenLength(int tokenLength) {
        this.tokenLength = tokenLength;
    }

    @Override
    protected UserDetailsService getUserDetailsService() {
        return super.getUserDetailsService();
    }

    @Resource
    @Override
    public void setUserDetailsService(UserDetailsService userDetailsService) {
        super.setUserDetailsService(userDetailsService);
    }

    @Override
    public int getTokenValiditySeconds() {
        return super.getTokenValiditySeconds();
    }
}
