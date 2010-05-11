/*
 * Copyright (c) 2010. Andrew McCall [andrew@andrewmccall.com] - All Rights Reserved.
 *
 * Unless explicitly stated otherwise, all rights are owned by or controlled by Andrew McCall.
 *
 * Except as otherwise expressly permitted under copyright law the content not be copied, reproduced,
 * republished, downloaded, posted, broadcast or transmitted in any way without first obtaining Andrew
 * McCall's written permission or that of the copyright owner.
 */

package com.andrewmccall.accounts.hbase.oauth;

import com.andrewmccall.accounts.core.User;
import com.andrewmccall.accounts.core.oauth.AccessToken;
import com.andrewmccall.accounts.hbase.TableFactory;
import com.andrewmccall.oauth.Service;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.io.IOException;

@Repository
public class AccessTokenStore implements com.andrewmccall.accounts.core.oauth.AccessTokenStore {

    public static final String OAUTH_TOKEN_COLUMN_FAMILY = "oauth";
    public static final byte[] OAUTH_TOKEN = Bytes.toBytes(OAUTH_TOKEN_COLUMN_FAMILY);

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Resource
    private TableFactory tableFactory;

    @Override
    public AccessToken getToken(User user, Service service) {
        byte[] row = Bytes.toBytes(user.getId().toString());
        Get get = new Get(row);
        get.addColumn(OAUTH_TOKEN, Bytes.toBytes(service.getId()));
        Result result;
        try {
            result = tableFactory.getTable().get(get);
            if (!result.isEmpty()) {
                byte[] tokenB = result.getValue(OAUTH_TOKEN, Bytes.toBytes(service.getId()));
                if (tokenB != null) {

                    // The first byte is the boolean, the next two indicate the break point between the secret and value.

                    AccessToken token = new AccessToken();
                    token.setService(service);
                    token.setUser(user);

                    int bp = Bytes.toInt(tokenB, 0, Bytes.SIZEOF_INT);
                    token.setSecret(Bytes.toString(tokenB, Bytes.SIZEOF_INT, bp));
                    bp = Bytes.SIZEOF_INT + bp;
                    token.setValue((Bytes.toString(tokenB, bp, tokenB.length - bp)));
                    return token;
                }
            }

        } catch (IOException e) {
            if (log.isWarnEnabled())
                log.warn("Failed to store OAuthTokenUtil for user: " + user + " and service " + service, e);
        }
        return null;
    }

    @Override
    public void storeToken(AccessToken token) {

        byte[] secret = Bytes.toBytes(token.getSecret());
        byte[] value = Bytes.toBytes(token.getValue());

        Put put = new Put(Bytes.toBytes(token.getUser().getId().toString()));

        put.add(OAUTH_TOKEN, Bytes.toBytes(token.getService().getId()), Bytes.add(
                Bytes.toBytes(secret.length),
                secret,
                value
        ));
        try {
            tableFactory.getTable().put(put);
        } catch (IOException e) {
            if (log.isWarnEnabled())
                log.warn("Failed to store OAuthTokenUtil " + token, e);
        }
    }

}
