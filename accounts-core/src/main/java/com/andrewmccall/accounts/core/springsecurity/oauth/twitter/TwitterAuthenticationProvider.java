/*
 * Copyright (c) 2010. Andrew McCall [andrew@andrewmccall.com] - All Rights Reserved.
 *
 * Unless explicitly stated otherwise, all rights are owned by or controlled by Andrew McCall.
 *
 * Except as otherwise expressly permitted under copyright law the content not be copied, reproduced,
 * republished, downloaded, posted, broadcast or transmitted in any way without first obtaining Andrew
 * McCall's written permission or that of the copyright owner.
 */

package com.andrewmccall.accounts.core.springsecurity.oauth.twitter;

import com.andrewmccall.accounts.core.springsecurity.oauth.OAuthAuthentication;
import com.andrewmccall.accounts.core.springsecurity.oauth.OAuthAuthenticationException;
import com.andrewmccall.accounts.core.springsecurity.oauth.OAuthAuthenticationProvider;
import com.andrewmccall.oauth.AccessToken;
import com.andrewmccall.oauth.OAuthConsumer;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.json.JSONObject;
import org.json.JSONException;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import com.andrewmccall.accounts.core.AccountService;
import com.andrewmccall.accounts.core.User;
import com.andrewmccall.accounts.core.AccountsException;

import javax.annotation.Resource;
import java.io.*;

import java.util.TimeZone;

public class TwitterAuthenticationProvider extends OAuthAuthenticationProvider {

    @Resource
    private AccountService<Long> accountService;

    @Resource
    private HttpClient httpClient;

    @Resource
    private OAuthConsumer oAuthConsumer;

    private String url = "http://twitter.com/account/verify_credentials.json";

    public TwitterAuthenticationProvider() {
        if (log.isInfoEnabled())
            log.info("Creating new instance of: " + this.getClass().getName());
    }

    @Override
    public void authenticate(OAuthAuthentication oAuthAuthentication) throws AuthenticationException {

        AccessToken token = oAuthAuthentication.getToken();

        if (log.isDebugEnabled())
            log.debug("Authenticating: " + oAuthAuthentication);

        HttpGet get = new HttpGet(url);
        try {
            oAuthConsumer.sign(get, token);
        } catch (Exception e) {
            throw new OAuthAuthenticationException("Signing request failed!", e);
        }

        HttpResponse response = null;
        try {
            response = httpClient.execute(get);
        } catch (IOException e) {
            throw new OAuthAuthenticationException("IOException with twitter get.", e);
        }

        if (log.isInfoEnabled())
            log.info(new StringBuffer().append("verify_credentials response: ").append(response.getStatusLine().getStatusCode()).append(" message: ").append(response.getStatusLine().getReasonPhrase()).toString());

        InputStream is;
        BufferedReader br;
        try {

            is = response.getEntity().getContent();
            br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuffer buf = new StringBuffer();
            String line;
            while (null != (line = br.readLine())) {
                buf.append(line).append("\n");
            }
            is.close();
            response.getEntity().consumeContent();

            String json = buf.toString();
            if (log.isTraceEnabled())
                log.trace("JSON returned: " + json);
            User user = getAndUpdateUser(new JSONObject(json));
            oAuthAuthentication.setUser(user);

        } catch (JSONException e) {
            throw new AuthenticationServiceException("There was an error parsing the JSON output.", e);
        } catch (IOException e) {
            throw new AuthenticationServiceException("There was an IO error", e);
        } catch (AccountsException e) {
            if (log.isInfoEnabled())
                log.info("Authentication failed - AccountsException - " + e.getMessage(), e);
            throw new AuthenticationServiceException("There was an AccountsException", e);
        }

    }

    private User getAndUpdateUser(JSONObject json) throws JSONException, AccountsException {

        User<Long> user;
        long twitterId = json.getLong("id");

        if (log.isInfoEnabled())
            log.info("getting and updating user for oauth id: " + twitterId + " from accountService: " + accountService.getClass());

        if (accountService.twitterIdExists(twitterId)) {
            user = accountService.getUserForTwitterId(twitterId);
            if (log.isDebugEnabled())
                log.debug("Twitter ID exists, user: " + user);
        } else {
            if (log.isDebugEnabled())
                log.debug("Twitter ID doesn't exist, creating new user.");
            user = new User<Long>();
            user.setTwitterId(twitterId);
        }
        int hashCode = user.toString().hashCode();

        user.setName(json.getString("name"));

        user.setUsername(json.getString("screen_name"));
        user.setLocation(json.getString("location"));
        user.setBio(json.getString("description"));

        user.setWebsite(json.getString("url"));

        user.setFollowers(json.getInt("followers_count"));
        user.setFriends(json.getInt("friends_count"));

        TimeZone tz = TimeZone.getTimeZone(json.getString("time_zone"));
        user.setTimeZoneId(tz.getID());

        if (user.getId() == null) {
            if (log.isTraceEnabled())
                log.trace("Creating new user: " + user);
            accountService.createUser(user);
        } else if (hashCode != user.toString().hashCode()) {
            if (log.isTraceEnabled())
                log.trace("Updating user: " + user);
            accountService.update(user);
        } else if (log.isTraceEnabled()) log.trace("User unchanged. No update required.");

        return user;
    }

}
