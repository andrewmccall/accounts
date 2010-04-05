/*
 * Copyright (c) 2010. Andrew McCall [andrew@andrewmccall.com] - All Rights Reserved.
 */

package com.andrewmccall.accounts.core;

/**
 * Provides all the required methods for managing accounts, user and persisting them.
 */
public interface AccountService<T> {

    /**
     * creates a new user in persitent storage. Implementations MUST set an Id such that user.getId() != 0, anything
     * else goes.
     * @param user The user to create.
     * @throws AccountsException to wrap any underlying exception thrown.
     */
    void createUser(User<T> user) throws AccountsException;

    /**
     * Gets a user based on the id of the user.
     * @param id the User's id.
     * @return a user if one exists, null if one can't be found.
     * @throws AccountsException to wrap any underlying exception thrown.
     */
    User<T> getUser (T id) throws AccountsException;

    /**
     * Gets a user based on the id of the user as a String, required for spring stuff.
     * @param idString the User's id as a string
     * @return a user if one exists, null if one can't be found.
     * @throws AccountsException to wrap any underlying exception thrown.
     */
    User<T> getUser (String idString) throws AccountsException;

    /**
     * checks to see if a user exists for the given twitterId. Will return true if a user with the given oauth ID has
     * ever logged in (or hasn't subsequently deleted their account).
     * @param twitterId the oauth Id.
     * @return true if a users with this twitterId exists false if not.
     * @throws AccountsException to wrap any underlying exception thrown.
     */
    boolean twitterIdExists(long twitterId) throws AccountsException;

    /**
     * updates a user.
     * @param user the user to update.
     * @throws AccountsException to wrap any underlying exception thrown.
     */
    void update(User<T> user) throws AccountsException;

    /**
     * gets a User object for a given twitterId.
     *
     * @param twitterId the twitterId for the user,
     * @return a User if one exists with this twitterId, null if a user can't be found.
     * @throws AccountsException to wrap any underlying exception thrown.
     */
    User<T> getUserForTwitterId(long twitterId) throws AccountsException;
}
