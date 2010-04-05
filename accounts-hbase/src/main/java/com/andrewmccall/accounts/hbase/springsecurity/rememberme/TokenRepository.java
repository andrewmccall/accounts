/*
 * Copyright (c) 2010. Andrew McCall [andrew@andrewmccall.com] - All Rights Reserved.
 *
 * Unless explicitly stated otherwise, all rights are owned by or controlled by Andrew McCall.
 *
 * Except as otherwise expressly permitted under copyright law the content not be copied, reproduced,
 * republished, downloaded, posted, broadcast or transmitted in any way without first obtaining Andrew
 * McCall's written permission or that of the copyright owner.
 */

package com.andrewmccall.accounts.hbase.springsecurity.rememberme;

import com.andrewmccall.accounts.core.springsecurity.rememberme.RememberMeToken;
import com.andrewmccall.accounts.core.User;
import com.andrewmccall.accounts.core.AccountsException;
import com.andrewmccall.accounts.hbase.TableFactory;
import org.springframework.stereotype.Service;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Date;

/**
 * Hbase TokenRepository implementation.
 */
@Service
public class TokenRepository implements com.andrewmccall.accounts.core.springsecurity.rememberme.TokenRepository {

    public static final String REMEMBER_ME_COLUMN_FAMILY = "rememberMe";
    public static final byte[] REMEMBER_ME = Bytes.toBytes(REMEMBER_ME_COLUMN_FAMILY);

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Resource
    private TableFactory tableFactory;

    /**
     * Stores a new RememberMeToken in the database.
     *
     * @param rememberMeToken the token to store.
     */
    @Override
    public void create(RememberMeToken rememberMeToken) throws AccountsException {
        Put put = new Put(Bytes.toBytes(rememberMeToken.getUser().getId().toString()));
        put.add(REMEMBER_ME, Bytes.toBytes(rememberMeToken.getSeries()), rememberMeToken.getDate().getTime(), Bytes.toBytes(rememberMeToken.getValue()));
        try {
            tableFactory.getTable().put(put);
        } catch (IOException e) {
            throw new AccountsException("Couldn't add remember me token: " + rememberMeToken, e);
        }
    }

    /**
     * Checks to see if we already have an existing token with the same series for a user. This should be exceedingly
     * rare, but it can cause some issues if we're warning a user that their cookie has been hijacked.
     *
     * @param series the series to check
     * @param user   the user to check for the series
     * @return true if the user is already using the series, false otherwise.
     */
    @Override
    public boolean exists(String series, User user) throws AccountsException {
        try {
            return tableFactory.getTable().exists(toGet(series, user));
        } catch (IOException e) {
            throw new AccountsException("Exception throw checking for existance of token", e);
        }
    }

    /**
     * gets the current token if one exists for a given series and user. If none can be found null is returned.
     *
     * @param series the series
     * @param user   the user
     * @return the current token, null if none exists.
     */
    @Override
    public RememberMeToken getToken(String series, User user) throws AccountsException {
        try {
            Result result = tableFactory.getTable().get(toGet(series, user));
            if (!result.isEmpty()) {
                Date date = new Date(result.getCellValue().getTimestamp());
                String value = Bytes.toString(result.getCellValue().getValue());
                return new RememberMeToken(series, user, value, date);
            }
            return null;
        } catch (IOException e) {
            throw new AccountsException("Exception thrown getting token", e);
        }
    }

    private static Get toGet(final String series, final User user) {
        Get get = new Get(Bytes.toBytes(user.getId().toString()));
        get.addColumn(REMEMBER_ME, Bytes.toBytes(series));
        return get;
    }

    /**
     * removes all tokens for a user.
     *
     * @param user the user
     */
    @Override
    public void removeUserTokens(User user) {
        byte[] row = Bytes.toBytes(user.getId().toString());
        Delete delete = new Delete(row);
        delete.deleteFamily(REMEMBER_ME);
        try {
            if (log.isTraceEnabled())
                log.trace("Calling delete (empty:" + delete.isEmpty() + " delete: " + delete);

            tableFactory.getTable().delete(delete);

        } catch (IOException e) {
            if (log.isWarnEnabled())
                log.warn("There was an error deleting the PasswordResetToken for user: " + user.getId() + ". Does not affect operation but this could be a security issue with old tokens remaining valid.");
        }
    }

    /**
     * updates a token.
     *
     * @param token the token to update
     */
    @Override
    public void update(RememberMeToken token) throws AccountsException {
        // for the purposes of hbase this is just a create
        create(token);
    }

}
