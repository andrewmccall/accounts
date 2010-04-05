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
import com.andrewmccall.accounts.core.AccountsException;

/**
 * Used to store tokens
 */
public interface TokenRepository {


    /**
     * Stores a new RememberMeToken in the database.   
     * @param rememberMeToken the token to store.
     */
    void create (RememberMeToken rememberMeToken) throws AccountsException;

    /**
     * Checks to see if we already have an existing token with the same series for a user. This should be exceedingly
     * rare, but it can cause some issues if we're warning a user that their cookie has been hijacked.
     *
     * @param series the series to check
     * @param user the user to check for the series
     * @return true if the user is already using the series, false otherwise.
     */
    boolean exists (String series, User user) throws AccountsException;

    /**
     * gets the current token if one exists for a given series and user. If none can be found null is returned.
     * @param series the series
     * @param user the user
     * @return the current token, null if none exists.
     */
    RememberMeToken getToken (String series, User user) throws AccountsException;

    /**
     * removes all tokens for a user.
     * @param user the user
     */
    void removeUserTokens(User user);

    /**
     * updates a token. 
     * @param token the token to update
     */
    void update(RememberMeToken token) throws AccountsException;

}
