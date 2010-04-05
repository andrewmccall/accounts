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

import org.apache.commons.lang.RandomStringUtils;

import java.util.Random;
import java.util.UUID;
import java.util.TimeZone;

import com.andrewmccall.accounts.core.User;


/**
 * Helper class to generate random User objects for tests. I can't see any other reason for them, so this lives in tests
 */
@SuppressWarnings({"StaticMethodOnlyUsedInOneClass"})
public class RandomTestUtils {

    private static Random random = new Random();

    /**
     * Generates a random User object with everything except the ID set. The ID is generally reserved for the DAO
     * implementation to set.
     * @return a randomly genreated User object to use with the test cases.
     * @param user the User to genreate data for. 
     */
    public static User generateUser(User user) {

        user.setUsername(RandomStringUtils.randomAlphabetic(15));

        while (user.getTwitterId() == 0)
            user.setTwitterId(random.nextLong());

        String pass = RandomStringUtils.randomAlphanumeric(10);

        user.setName(RandomStringUtils.randomAlphanumeric(20));
        user.setBio(RandomStringUtils.randomAlphanumeric(160));
        user.setLocation(RandomStringUtils.randomAlphanumeric(30));
        user.setWebsite(generateUrl());
        user.setTimeZoneId(TimeZone.getDefault().getID());
        user.setFollowers(Math.abs(random.nextInt()));
        user.setFriends(Math.abs(random.nextInt()));
        
        return user;
    }

    public static String generateRandomEmailAddress () {
        StringBuffer buff = new StringBuffer(RandomStringUtils.randomAlphabetic(10).toLowerCase());
        buff.append("@");
        generateDomain(buff);
        return buff.toString();
    }

    public static String generateUrl() {
        StringBuffer  buff = new StringBuffer("http://");
        generateDomain(buff);
        return buff.toString();
    }


    public static void generateDomain (StringBuffer buff) {
        buff.append(RandomStringUtils.randomAlphabetic(10).toLowerCase());
        buff.append(".com");
    }

    /**
     * This is the equiv to calling accountService.create(user).. so that it can be used with easymock.
     * @param user the User.
     * @return the user with their id set.
     */
    public static User setId(User user) {
        while (user.getId() == null)
            user.setId(UUID.randomUUID().toString());
        return user;
    }

}
