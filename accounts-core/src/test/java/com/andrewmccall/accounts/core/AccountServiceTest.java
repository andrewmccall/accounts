/*
 * Copyright (c) 2010. Andrew McCall [andrew@andrewmccall.com] - All Rights Reserved.
 *
 * Unless explicitly stated otherwise, all rights are owned by or controlled by Andrew McCall.
 *
 * Except as otherwise expressly permitted under copyright law the content not be copied, reproduced,
 * republished, downloaded, posted, broadcast or transmitted in any way without first obtaining Andrew
 * McCall's written permission or that of the copyright owner.
 */

package com.andrewmccall.accounts.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;

import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

/**
 * Creates an Account and first user as they'd be created on teh website.
 */

public abstract class AccountServiceTest<T> {

    protected User<T> user;

    protected Log log = LogFactory.getLog(this.getClass());

    @Resource
    protected AccountService<T> accountService;

    @Before
    public void prepareUser() {
        user = new User();
        RandomTestUtils.generateUser(user);
    }

    @Test
    @Transactional(readOnly=false)
    public void testCreateUser() throws Exception {

        accountService.createUser(user);

        if (log.isInfoEnabled())
            log.info("Created user: " + user);

        assertNotNull("The user should have an ID! ", user.getId());
        assertEquals("We should be able to get the same user via it's ID.", user, accountService.getUser(user.getId()));
        assertEquals("We should be able to get the same user via a string of it's ID.", user, accountService.getUser(user.getId().toString()));


        User ret;
        
        if (log.isInfoEnabled())
            log.info("Checking that we can get a user by their twitterId.");

        assertTrue("The twitterId should exist!", accountService.twitterIdExists(user.getTwitterId()));
        ret = accountService.getUserForTwitterId (user.getTwitterId());
        assertEquals("We should be able to find the User by their twitterId!", user, ret);

    }

    @Test
    @Transactional
    public void testUpdateUser() throws AccountsException {
        accountService.createUser(user);

        assertNotNull("The user should have an ID! ", user.getId());
        assertEquals("We should be able to get the same user via it's ID.", user, accountService.getUser(user.getId()));
        if (log.isDebugEnabled())
            log.debug("Modifying user.");
        RandomTestUtils.generateUser(user);
        accountService.update(user);

        User ret = accountService.getUser(user.getId());
        assertEquals("The user should have been update", user, ret);

    }

    @Test(expected = AccountsException.class)
    @Transactional
    public void testNoUserReturnsNull() throws AccountsException {
        accountService.createUser(user);

        assertNotNull("The user should have an ID! ", user.getId());
        user = accountService.getUser(getId());
        log.warn("Got user: " + user);
    }

    @Test(expected = AccountsException.class)
    @Transactional
    public void testNoUserStringReturnsNull() throws Exception {

        user = new User();
        RandomTestUtils.generateUser(user);
        accountService.createUser(user);

        assertNotNull("The user should have an ID! ", user.getId());
        user = accountService.getUser(user.getId().toString() + 1);
        log.warn("Got user: " + user);
    }

    public abstract T getId();

}
