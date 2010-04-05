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


import java.util.TimeZone;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Tests the getter and setters of User.
 */
public class UserTest {

    @Test
    public void testTimezoneSet() {
        User andrew = new User();

        TimeZone tz = TimeZone.getTimeZone("GMT");
        andrew.setTimeZoneId("GMT");
        assertEquals("The timezone was incorrectly set. ", andrew.getTimeZone(), tz);

        tz = TimeZone.getTimeZone("PST");
        andrew.setTimeZoneId("PST");
        assertEquals("The timezone was incorrectly set. ", andrew.getTimeZone(), tz);

    }

}
