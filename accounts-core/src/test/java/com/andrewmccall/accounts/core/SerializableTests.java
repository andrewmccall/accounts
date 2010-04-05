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

import org.junit.Test;

import java.io.*;

import static org.junit.Assert.assertEquals;

/**
 * Performs some very basic tests on the Account object.
 */
public class SerializableTests {
    
    @Test
    public void testUser() throws IOException, ClassNotFoundException {

        User user = new User();
        user = RandomTestUtils.generateUser(user);
        testSerialize(user);
    }

    public void testSerialize(Serializable object) throws IOException, ClassNotFoundException {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(bos);

        os.writeObject(object);

        byte[] objectBytes = bos.toByteArray();

        ByteArrayInputStream bis = new ByteArrayInputStream(objectBytes);
        ObjectInputStream ois = new ObjectInputStream(bis);

        assertEquals("The object should serialise, deserialise properly!", object, ois.readObject());

    }

}
