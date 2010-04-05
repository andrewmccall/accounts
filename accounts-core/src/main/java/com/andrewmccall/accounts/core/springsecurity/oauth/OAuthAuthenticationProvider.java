/*
 * Copyright (c) 2010. Andrew McCall [andrew@andrewmccall.com] - All Rights Reserved.
 *
 * Unless explicitly stated otherwise, all rights are owned by or controlled by Andrew McCall.
 *
 * Except as otherwise expressly permitted under copyright law the content not be copied, reproduced,
 * republished, downloaded, posted, broadcast or transmitted in any way without first obtaining Andrew
 * McCall's written permission or that of the copyright owner.
 */

package com.andrewmccall.accounts.core.springsecurity.oauth;

import com.andrewmccall.accounts.core.oauth.AccessToken;
import com.andrewmccall.accounts.core.oauth.AccessTokenStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import javax.annotation.Resource;

/**
 * Simple superclass that makes it easy for subclasses to support the AuthenticationTokens they'll need.
 */
public abstract class OAuthAuthenticationProvider implements AuthenticationProvider {

    protected Logger log = LoggerFactory.getLogger(this.getClass());
    
    @Resource
    private AccessTokenStore accessTokenStore;

    public abstract void authenticate(OAuthAuthentication authentication) throws AuthenticationException;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (!supports(authentication.getClass())) {
            return null;
        }

        OAuthAuthentication oAuthAuthentication = (OAuthAuthentication) authentication;
        authenticate(oAuthAuthentication);


        if (oAuthAuthentication.isAuthenticated()) {
            AccessToken token = oAuthAuthentication.getToken();
            token.setUser(oAuthAuthentication.getUser());
            if (log.isDebugEnabled())
                log.debug("Storing token: " + token);
            accessTokenStore.storeToken(token);
        }

        return authentication;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return OAuthAuthentication.class.isAssignableFrom(authentication);
    }
}
