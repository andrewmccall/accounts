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

import org.junit.Test;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import com.andrewmccall.accounts.core.User;
import com.andrewmccall.accounts.core.AccountService;
import com.andrewmccall.accounts.core.*;

import javax.annotation.Resource;
import java.util.Random;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests basic operations of the JpaTokenRepository to make sure it does what it's meant to do.
 */
public abstract class TokenRepositoryTest {

    private Random random = new Random();

    @Resource
    AccountService accountService;

    @Resource
    private TokenRepository tokenRepository;

    private Log log = LogFactory.getLog(this.getClass());

    @Test
    @Transactional
    public void testCreate() throws AccountsException {

        User user = new User();
        RandomTestUtils.generateUser(user);
        accountService.createUser(user);
        //user = accountService.getUser(user.getId());

        if (log.isTraceEnabled())
            log.trace("testing create of a new token for user: " + user);

        RememberMeToken token = generateToken(user);

        if (log.isTraceEnabled())
            log.trace("Generated token: " + token);

        assertFalse("We haven't created the token yet, it shouldn't exist!", tokenRepository.exists(token.getSeries(), user));
        tokenRepository.create(token);
        assertTrue("The token should exist. ", tokenRepository.exists(token.getSeries(), user));

    }

    /**
     * generates a new random token for a user.
     *
     * @param user the user
     * @return a new random token
     */
    static RememberMeToken generateToken(User user) {
        String series = RandomStringUtils.randomAlphanumeric(12);
        String value = RandomStringUtils.randomAlphanumeric(12);
        return new RememberMeToken(series, user, value, null);
    }

    /**
     * Should create a few tokens for a user, then successfully remove them all.
     */
    @Test
    public void testRemoveForUser() throws AccountsException {

        String[] series = new String[random.nextInt(10)];

        User user = new User();
        RandomTestUtils.generateUser(user);
        accountService.createUser(user);
        user = accountService.getUser(user.getId());

        if (log.isTraceEnabled())
            log.trace("testing create of a new token for user: " + user);

        RememberMeToken token;


        for (int i = 0; i < series.length; i++) {

            token = generateToken(user);

            if (log.isTraceEnabled())
                log.trace("Generated token: " + token);

            series[i] = token.getSeries();

            assertFalse("We haven't created the token yet, it shouldn't exist!", tokenRepository.exists(token.getSeries(), user));
            tokenRepository.create(token);
            assertTrue("The token should exist. ", tokenRepository.exists(token.getSeries(), user));

        }



        if (log.isTraceEnabled())
            log.trace("Deleting tokens for the user");
        tokenRepository.removeUserTokens(user);

        for (String sery : series) {
            assertFalse("The token should not exist. ", tokenRepository.exists(sery, user));
        }

    }


}
