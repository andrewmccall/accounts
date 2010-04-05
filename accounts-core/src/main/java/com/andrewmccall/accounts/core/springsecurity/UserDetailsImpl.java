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

import com.andrewmccall.accounts.core.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.List;

/**
 * Internal class used within the accounts package to pass things through Acegi. The user object for a username may be
 * retrieved using the getter method.
 */
public class UserDetailsImpl implements UserDetails {

    private User user;

    private List<GrantedAuthority> authorities;
    private static final long serialVersionUID = 772215784811033233L;

    public UserDetailsImpl(User user, List<GrantedAuthority> authorities) throws IllegalArgumentException {
        this.user = user;
        this.authorities = authorities;
    }

    public User getUser() {
        return user;
    }

    /**
     * In our implementation the username is the ID!!!
     *
     * @return user.getId();
     */
    public String getUsername() {
        return user.getId().toString();
    }

    public String getPassword() {
        return null;
    }

    public List<GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public boolean isEnabled() {
        return true;
    }

    public boolean isAccountNonExpired() {
        return true;
    }

    public boolean isCredentialsNonExpired() {
        return true;
    }

    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public String toString() {
        return (new ReflectionToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)).toString();
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj, false);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this, false);
    }
}
