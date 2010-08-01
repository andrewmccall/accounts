/*
 * Copyright (c) 2010. Andrew McCall [andrew@andrewmccall.com] - All Rights Reserved.
 *
 * Unless explicitly stated otherwise, all rights are owned by or controlled by Andrew McCall.
 *
 * Except as otherwise expressly permitted under copyright law the content not be copied, reproduced,
 * republished, downloaded, posted, broadcast or transmitted in any way without first obtaining Andrew
 * McCall's written permission or that of the copyright owner.
 */

package com.andrewmccall.accounts.core.springsecurity;

import org.mockito.Mockito;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import javax.annotation.Resource;

import com.andrewmccall.accounts.core.User;
import com.andrewmccall.accounts.core.RandomTestUtils;
import com.andrewmccall.accounts.core.AccountService;

import java.util.UUID;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/com/andrewmccall/accounts/accounts-core-config.xml", "classpath:/com/andrewmccall/accounts/accounts-services-config.xml"})
public class SpringSecurityServiceTest {

    @Resource
    SpringSecurityService securityService;

    @Resource
    AccountService accountService;

    private User<UUID> user;

    @Before
    public void setUp() throws Exception {
        SecurityContextHolder.clearContext();
        SecurityContextHolder.setContext(new SecurityContextImpl());


        reset(accountService);
        user = new User<UUID>();
        RandomTestUtils.generateUser(user);
        user.setId(UUID.randomUUID());
        when(accountService.getUser(user.getId().toString())).thenReturn(user);

    }

    /**
     * Tests that a user will be able to be loaded by any of it's email addresses.
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testLoadByUsername() throws Exception {
        // we need to interact with the Context here and make sure the host/account is correctly set.
        String login = user.getId().toString();
        UserDetails ret = securityService.loadUserByUsername(login);
        assertEquals("The User we got, should be equal to the one we put", new UserDetailsImpl(user, SpringSecurityService.AUTHORITIES), ret);
    }

    @Test
    public void testGetUser() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new TestAuthentication(user));
        assertEquals("The user on the context should be ours", user, securityService.getUser());
    }

    private class TestAuthentication extends Authentication {

        public TestAuthentication(User user) {
            super();
            super.setUser(user);
        }

        @Override
        public boolean isAuthenticated() {
            return true;
        }

        @Override
        public Object getCredentials() {
            return null;
        }
    }

}
