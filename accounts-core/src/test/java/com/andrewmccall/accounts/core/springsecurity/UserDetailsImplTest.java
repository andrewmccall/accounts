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

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import com.andrewmccall.accounts.core.User;
import com.andrewmccall.accounts.core.RandomTestUtils;

import java.util.UUID;

import static junit.framework.Assert.assertTrue;

/**
 * Just a few tests to make sure everything works and to get 100% coverage.
 */
public class UserDetailsImplTest {

    @Test
    public void testUserDetails() {
        User<UUID> user = new User<UUID>();
        RandomTestUtils.generateUser(user);
        user.setId(UUID.randomUUID());
        UserDetailsImpl u = new UserDetailsImpl(user, null);

        assertEquals(user.getId().toString(), u.getUsername());
        assertTrue(u.isAccountNonExpired());
        assertTrue(u.isAccountNonLocked());
        assertTrue(u.isCredentialsNonExpired());
        assertTrue(u.isEnabled());

    }

}
