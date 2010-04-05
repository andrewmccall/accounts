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

import com.andrewmccall.accounts.core.AccountService;
import com.andrewmccall.accounts.core.User;
import com.andrewmccall.accounts.core.RandomTestUtils;
import com.andrewmccall.accounts.core.AccountsException;

import javax.annotation.Resource;

import org.junit.Test;

import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.apache.commons.lang.RandomStringUtils;

import java.util.Random;

public abstract class AccessTokenStoreTest {

    private Random random = new Random();

    @Resource
    AccountService accountService;

    @Resource
    private AccessTokenStore accessTokenStore;

    @Test
    public void testCreate() throws AccountsException {

        //create a user.
        User user = new User();
        RandomTestUtils.generateUser(user);


        Service service = new Service ();
        service.setId("monkey");

        accountService.createUser(user);
        //user = accountService.getUser(user.getId());

        AccessToken token = new AccessToken();
        token.setSecret(RandomStringUtils.random(25));
        token.setValue(RandomStringUtils.random(25));
        token.setService(service);
        token.setUser(user);

        accessTokenStore.storeToken(token);

        AccessToken ret = accessTokenStore.getToken(user, service);
        assertNotNull("The return value should not be null!", ret);
        assertEquals(token.getSecret(), ret.getSecret());
        assertEquals(token.getValue(), ret.getValue());
        
    }


    @Test
    public void testCreateDupe () throws AccountsException {
         //create a user.
        User user = new User();
        RandomTestUtils.generateUser(user);

        Service service = new Service ();
        service.setId("monkey");

        accountService.createUser(user);
        //user = accountService.getUser(user.getId());

        AccessToken token = new AccessToken();
        token.setService(service);
        token.setUser(user);

        String secret = RandomStringUtils.randomAscii(25);
        String value = RandomStringUtils.randomAscii(25);

        token.setSecret(secret);
        token.setValue(value);

        accessTokenStore.storeToken(token);

        AccessToken ret = accessTokenStore.getToken(user, service);
        assertEquals(secret, ret.getSecret());
        assertEquals(value, ret.getValue());

        String secret1 = RandomStringUtils.randomAscii(25);
        String value1 = RandomStringUtils.randomAscii(25);

        token.setSecret(secret1);
        token.setValue(value1);

        accessTokenStore.storeToken(token);
        ret = accessTokenStore.getToken(user, service);
        assertEquals(secret1, ret.getSecret());
        assertEquals(value1, ret.getValue());

        assertThat(secret, not(ret.getSecret()));
        assertThat(value, not(ret.getValue()));

    }

}
