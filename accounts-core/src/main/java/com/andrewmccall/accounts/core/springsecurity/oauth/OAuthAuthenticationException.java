/*
 * Copyright (c) 2010. Andrew McCall [andrew@andrewmccall.com] - All Rights Reserved.
 */

package com.andrewmccall.accounts.core.springsecurity.oauth;

import org.springframework.security.core.AuthenticationException;

public class OAuthAuthenticationException extends AuthenticationException {

    public OAuthAuthenticationException(String msg, Throwable t) {
        super(msg, t);
    }

    public OAuthAuthenticationException(String msg) {
        super(msg);
    }

    public OAuthAuthenticationException(String msg, Object extraInformation) {
        super(msg, extraInformation);
    }
}
