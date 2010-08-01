/*
 * Copyright (c) 2010. Andrew McCall [andrew@andrewmccall.com] - All Rights Reserved.
 */

package com.andrewmccall.accounts.core.oauth;

import com.andrewmccall.oauth.AccessToken;
import com.andrewmccall.oauth.Service;

import com.andrewmccall.accounts.core.AccountsException;
import com.andrewmccall.accounts.core.User;

/**
 * Stores OAuthTokens
 */
public interface AccessTokenStore {

    AccessToken getToken(User user, Service service) throws AccountsException;

    void storeToken(AccessToken accessToken, User user);

}
