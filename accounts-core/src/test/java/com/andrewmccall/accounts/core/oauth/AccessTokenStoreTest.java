/*
 * Copyright (c) 2010. Andrew McCall [andrew@andrewmccall.com] - All Rights Reserved.
 *
 * Unless explicitly stated otherwise, all rights are owned by or controlled by Andrew McCall.
 *
 * Except as otherwise expressly permitted under copyright law the content not be copied, reproduced,
 * republished, downloaded, posted, broadcast or transmitted in any way without first obtaining Andrew
 * McCall's written permission or that of the copyright owner.
 */

package com.andrewmccall.accounts.core.oauth;

import com.andrewmccall.oauth.AccessToken;

import com.andrewmccall.accounts.core.AccountService;
import com.andrewmccall.accounts.core.User;
import com.andrewmccall.accounts.core.RandomTestUtils;
import com.andrewmccall.accounts.core.AccountsException;

import javax.annotation.Resource;

import com.andrewmccall.oauth.Service;
import org.junit.Test;

import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.*;

import org.apache.commons.lang.RandomStringUtils;

public abstract class AccessTokenStoreTest {

    @Resource
    AccountService accountService;

    @Resource
    private AccessTokenStore accessTokenStore;

    @Test
    public void testCreate() throws AccountsException {

        //create a user.
        User user = new User();
        RandomTestUtils.generateUser(user);


        Service service = new com.andrewmccall.oauth.oauth_1_0.Service();
        service.setId("monkey");

        accountService.createUser(user);

        AccessToken token = new com.andrewmccall.oauth.oauth_1_0.AccessToken();
        token.setSecret(RandomStringUtils.random(25));
        token.setValue(RandomStringUtils.random(25));
        token.setService(service);


        accessTokenStore.storeToken(token, user);

        AccessToken ret = accessTokenStore.getToken(user, service);
        assertNotNull("The return value should not be null!", ret);
        assertEquals(token.getSecret(), ret.getSecret());
        assertEquals(token.getValue(), ret.getValue());
        assertEquals(service, token.getService());

    }

    @Test
    public void testCreatesDifferentVersions() throws AccountsException {
        //create a user.
        User user = new User();
        RandomTestUtils.generateUser(user);
        accountService.createUser(user);

        Service service = new com.andrewmccall.oauth.oauth_1_0.Service();
        service.setId("1.0");
        AccessToken token = new com.andrewmccall.oauth.oauth_1_0.AccessToken();
        token.setSecret(RandomStringUtils.random(25));
        token.setValue("Token-1.0");
        token.setService(service);

        accessTokenStore.storeToken(token, user);


        Service service2 = new com.andrewmccall.oauth.oauth_2_0.Service();
        service2.setId("2.0");
        AccessToken token2 = new com.andrewmccall.oauth.oauth_2_0.AccessToken();
        token2.setSecret(RandomStringUtils.random(25));
        token2.setValue("Token-2.0");
        token2.setService(service2);

        accessTokenStore.storeToken(token2, user);

        AccessToken ret = accessTokenStore.getToken(user, service);
        assertNotNull("The return value should not be null!", ret);
        assertTrue(ret instanceof com.andrewmccall.oauth.oauth_1_0.AccessToken);
        assertEquals(token.getSecret(), ret.getSecret());
        assertEquals(token.getValue(), ret.getValue());
        assertEquals(service, token.getService());

        ret = accessTokenStore.getToken(user, service2);
        assertNotNull("The return value should not be null!", ret);
        assertTrue(ret instanceof com.andrewmccall.oauth.oauth_2_0.AccessToken);
        assertEquals(token2.getSecret(), ret.getSecret());
        assertEquals(token2.getValue(), ret.getValue());
        assertEquals(service2, token2.getService());

    }

    @Test
    public void testCreateDupe() throws AccountsException {
        //create a user.
        User user = new User();
        RandomTestUtils.generateUser(user);

        Service service = new com.andrewmccall.oauth.oauth_1_0.Service();
        service.setId("monkey");

        accountService.createUser(user);
        //user = accountService.getUser(user.getId());

        AccessToken token = new AccessToken() {
        };
        token.setService(service);


        String secret = RandomStringUtils.randomAscii(25);
        String value = RandomStringUtils.randomAscii(25);

        token.setSecret(secret);
        token.setValue(value);

        accessTokenStore.storeToken(token, user);

        AccessToken ret = accessTokenStore.getToken(user, service);
        assertEquals(secret, ret.getSecret());
        assertEquals(value, ret.getValue());
        assertEquals(service, token.getService());

        String secret1 = RandomStringUtils.randomAscii(25);
        String value1 = RandomStringUtils.randomAscii(25);

        token.setSecret(secret1);
        token.setValue(value1);

        accessTokenStore.storeToken(token, user);
        ret = accessTokenStore.getToken(user, service);
        assertEquals(secret1, ret.getSecret());
        assertEquals(value1, ret.getValue());
        assertEquals(service, token.getService());

        assertThat(secret, not(ret.getSecret()));
        assertThat(value, not(ret.getValue()));

    }

}
