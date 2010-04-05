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

import com.andrewmccall.accounts.core.User;

import java.util.Date;
import java.io.Serializable;

/**
 * Secure JPA remember me token
 */
public class RememberMeToken implements Serializable {

    private String series;
    private User user;
    private String value;
    private Date date;
    private static final long serialVersionUID = -9165552646539037853L;

    public RememberMeToken(String series, User user) {
        this.series = series;
        this.user = user;
    }

    public RememberMeToken(){}

    public RememberMeToken(String series, User user, String value, Date date) {
        setSeries(series);
        setUser(user);
        setValue(value);
        setDate(date);
    }

    public String getSeries() {
        return series;
    }

    public void setSeries(String series) {
        this.series = series;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "RememberMeToken{series='" + series + '\'' +
                ", user=" + user +
                ", value='" + value + '\'' +
                ", date=" + date +
                '}';
    }
}
