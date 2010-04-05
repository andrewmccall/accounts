/*
 * Copyright (c) 2010. Andrew McCall [andrew@andrewmccall.com] - All Rights Reserved.
 *
 * Unless explicitly stated otherwise, all rights are owned by or controlled by Andrew McCall.
 *
 * Except as otherwise expressly permitted under copyright law the content not be copied, reproduced,
 * republished, downloaded, posted, broadcast or transmitted in any way without first obtaining Andrew
 * McCall's written permission or that of the copyright owner.
 */

package com.andrewmccall.accounts.core.springsecurity;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import com.andrewmccall.accounts.core.User;

/**
 * Abstract Authentication object. The principal is a com.andrewmccall.accounts.User
 */
public abstract class Authentication extends AbstractAuthenticationToken {

    private User user;

    public Authentication() {
        super(SpringSecurityService.AUTHORITIES);
    }

    /**
     * The same as calling getUser()
     *
     * @return the user.
     */
    @Override
    public Object getPrincipal() {
        return getUser();
    }

    @Override
    public boolean isAuthenticated() {
        return getUser() != null;
    }

    @Override
    public void setAuthenticated(boolean b) throws IllegalArgumentException {
        throw new IllegalArgumentException("This implementation may not be used as an Authenticated Authentication.");
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Authentication{" +
                "user=" + getUser() +
                '}';
    }
}
