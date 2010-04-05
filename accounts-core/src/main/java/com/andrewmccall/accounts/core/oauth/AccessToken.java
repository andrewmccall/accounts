/*
 * Copyright (c) 2010. Andrew McCall [andrew@andrewmccall.com] - All Rights Reserved.
 */

package com.andrewmccall.accounts.core.oauth;

import com.andrewmccall.accounts.core.User;

import java.io.Serializable;

/**
 * AccessToken for use in accounts - has a link to the User that owns it.
 */
public class AccessToken extends com.andrewmccall.oauth.AccessToken implements Serializable {

    User user;

    public AccessToken() {
    }

    public AccessToken(com.andrewmccall.oauth.AccessToken token) {
        super();
        this.setService(token.getService());
        this.setSecret(token.getSecret());
        this.setValue(token.getValue());
    }

    public AccessToken(Service service, User user) {
        super();
        setService(service);
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public void setService(com.andrewmccall.oauth.Service service) {
        if (!(service instanceof Service))
            throw new IllegalArgumentException("The service is not of the correct type! Must be " + Service.class.toString());
        super.setService(service);
    }

    @Override
    public String toString() {
        return "AccessToken{" +
                "user=" + user +
                ", service=" + getService() +
                ", secret=" + getSecret() +
                ", value=" + getValue() +
                '}';
    }
}
