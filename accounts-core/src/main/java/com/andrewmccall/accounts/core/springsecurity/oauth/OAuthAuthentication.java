/*
 * Copyright (c) 2010. Andrew McCall [andrew@andrewmccall.com] - All Rights Reserved.
 */

package com.andrewmccall.accounts.core.springsecurity.oauth;

import com.andrewmccall.accounts.core.springsecurity.Authentication;
import com.andrewmccall.oauth.AccessToken;

public class OAuthAuthentication extends Authentication {

    AccessToken token;

    Object details;

    public OAuthAuthentication(AccessToken token, Object details) {
        this.token = token;
        this.details = details;
    }

    public AccessToken getToken() {
        return token;
    }

    @Override
    public Object getCredentials() {
        return getToken();
    }

    @Override
    public Object getDetails() {
        return details;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String toString() {
        return "OAuthAuthentication{" +
                "user=" + getUser() +
                ", token=" + token +
                '}';
    }
}
