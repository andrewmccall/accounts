/*
 * Copyright (c) 2010. Andrew McCall [andrew@andrewmccall.com] - All Rights Reserved.
 */

package com.andrewmccall.accounts.hbase;

import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/com/andrewmccall/accounts/accounts-hbase-test-config.xml"})
public class AccountServiceTest {//} extends com.andrewmccall.accounts.core.AccountServiceTest<UUID> {

    @Resource
    TableFactory tableFactory;

    @Mock
    HTable table;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        reset(table);
        when(tableFactory.getTable()).thenReturn(table);
    }

    @Test
    public void testSetup() {
        assertTrue(tableFactory.getTable() != null);
    }

    /*
    @Override
    public void testCreateUser() throws Exception {
        // Mock up our HTable interactions.

        ArgumentCaptor<Get> get = ArgumentCaptor.forClass(Get.class);
        ArgumentCaptor<Put> put = ArgumentCaptor.forClass(Put.class);

        when(table.exists(get.capture())).thenReturn(false);
        doNothing().when(table).put(put.capture());

        super.testCreateUser();

        // Tests that we checked to see if the UUID existed.
        assertEquals(user.getId(), UUID.fromString(Bytes.toString(get.getValue().getRow())));

        // Test the Put.
    }

    

    @Override
    public UUID getId() {
        return UUID.randomUUID();
    }
    */

}
