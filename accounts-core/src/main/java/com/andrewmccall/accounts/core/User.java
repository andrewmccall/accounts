/*
 * Copyright (c) 2010. Andrew McCall [andrew@andrewmccall.com] - All Rights Reserved.
 */

package com.andrewmccall.accounts.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.validation.constraints.Size;
import java.util.*;
import java.io.Serializable;

import com.andrewmccall.validation.NotEmpty;
import com.andrewmccall.validation.FieldsEqual;

/**
 * The User, everyone that logs in is a user.
 */
@FieldsEqual(fields = {"password", "confirmPassword"}, message = "password.mismatch")
public class User<T> implements Serializable {

    private static final long serialVersionUID = -3847458699405927276L;
    private static final Log log = LogFactory.getLog(User.class);

    /**
     * The user ID for this user, the id is a number used to itdentify the user and it's realtionship database,
     * filesystem and other forms of storage.
     */
    private T id;

    private Profile profile;

    /**
     * The user's username, this is used to login and also in the URL to identify the user.
     */
    private String username;

    /**
     * The user's twitterID, this is used to tie the account to oauth.
     */
    private long twitterId;

    /**
     * The User's firstname.
     */
    private String name;

    /**
     * The users's short Bio.
     */
    private String bio;

    /**
     * The users's website
     */
    private String website;

    /**
     * The user's location - for privacy reasons this is text a user can just enter.
     */
    private String location;

    /**
     * Users oauth followers
     */
    private int followers;

    /**
     * Users this person follows.
     */
    private int friends;

    /**
     * The User's timezone. This is used for scheduling, local display and internationalization.
     */
    private transient TimeZone timeZone = null;

    public User() {
        if (log.isTraceEnabled())
            log.trace("Creating new instance of " + this.getClass().getName());
    }

    public T getId() {
        return id;
    }

    public void setId(T id) {
        this.id = id;
    }

    @NotEmpty(message = "username.required")
    @Size(max = 15, message = "username.size")
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getTwitterId() {
        return twitterId;
    }

    public void setTwitterId(long twitterId) {
        this.twitterId = twitterId;
    }

    @NotEmpty(message = "name.required")
    @Size(min = 0, max = 20, message = "name.size")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Size(min = 1, max = 160, message = "bio.size")
    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    @Size(min = 1, max = 100)
    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    @Size(min = 1, max = 30)
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getFollowers() {
        return followers;
    }

    public void setFollowers(int followers) {
        this.followers = followers;
    }

    public int getFriends() {
        return friends;
    }

    public void setFriends(int friends) {
        this.friends = friends;
    }

    public TimeZone getTimeZone() {
        if (timeZone == null)
            timeZone = TimeZone.getDefault();
        return timeZone;
    }

    public void setTimeZoneId(String timeZoneId) {
        if (timeZoneId != null)
            this.timeZone = TimeZone.getTimeZone(timeZoneId);
        else this.timeZone = null;
    }

    public String getTimeZoneId() {
        return getTimeZone().getID();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (id != null ? !id.equals(user.id) : user.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("User");
        sb.append("{id=").append(id);
        sb.append(", username='").append(username).append('\'');
        sb.append(", twitterId=").append(twitterId);
        sb.append(", name='").append(name).append('\'');
        sb.append(", bio='").append(bio).append('\'');
        sb.append(", website='").append(website).append('\'');
        sb.append(", location='").append(location).append('\'');
        sb.append(", followers=").append(followers);
        sb.append(", friends=").append(friends);
        if (timeZone != null)
            sb.append(", timeZone=").append(timeZone.getID());
        else sb.append(", timeZone=null");
        sb.append('}');
        return sb.toString();
    }
}
