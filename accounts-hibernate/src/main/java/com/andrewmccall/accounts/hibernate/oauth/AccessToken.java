/*
 * Copyright (c) 2010. Andrew McCall [andrew@andrewmccall.com] - All Rights Reserved.
 */

package com.andrewmccall.accounts.hibernate.oauth;

import com.andrewmccall.accounts.core.User;
import com.andrewmccall.accounts.core.oauth.Service;

/**
 * Intermediate AccessToken object we store in the database. 
 */
public class AccessToken extends com.andrewmccall.accounts.core.oauth.AccessToken {

    private String serviceId;

    public AccessToken() {}

    public AccessToken(Service service, User user) {
        super(service, user);
        this.serviceId = service.getId();
    }

    @Override
    public void setService(com.andrewmccall.oauth.Service service) {
        super.setService(service);
        this.setServiceId(((Service)service).getId());
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AccessToken)) return false;
        if (!super.equals(o)) return false;

        AccessToken that = (AccessToken) o;

        if (serviceId != null ? !serviceId.equals(that.serviceId) : that.serviceId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (serviceId != null ? serviceId.hashCode() : 0);
        return result;
    }
}
