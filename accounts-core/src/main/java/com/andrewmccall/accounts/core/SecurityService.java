/*
 * Copyright (c) 2010. Andrew McCall [andrew@andrewmccall.com] - All Rights Reserved.
 */

package com.andrewmccall.accounts.core;

/**
 * The SecurityService provides access to the currently authenticated User.
 */
public interface SecurityService {

    /**
     * Gets the user that has been authenticated in the context of the currently executing Thread.
     *
     * @return the current user if one has been authenticated, none if no user has been authenticated
     */
    User getUser();

}
