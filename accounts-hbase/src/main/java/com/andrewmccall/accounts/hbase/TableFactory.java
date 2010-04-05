/*
 * Copyright (c) 2010. Andrew McCall [andrew@andrewmccall.com] - All Rights Reserved.
 *
 * Unless explicitly stated otherwise, all rights are owned by or controlled by Andrew McCall.
 *
 * Except as otherwise expressly permitted under copyright law the content not be copied, reproduced,
 * republished, downloaded, posted, broadcast or transmitted in any way without first obtaining Andrew
 * McCall's written permission or that of the copyright owner.
 */

package com.andrewmccall.accounts.hbase;

import com.andrewmccall.accounts.hbase.oauth.AccessTokenStore;
import com.andrewmccall.accounts.hbase.springsecurity.rememberme.TokenRepository;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.idx.IdxColumnDescriptor;
import org.apache.hadoop.hbase.client.idx.IdxIndexDescriptor;
import org.apache.hadoop.hbase.client.idx.IdxQualifierType;
import org.apache.hadoop.hbase.regionserver.IdxRegion;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 * Used to create and get the table.
 */
@Service
public class TableFactory {

    private String accountsTableName = "accounts";

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public TableFactory() throws MasterNotRunningException {
    }

    private HTable table;

    public HTable getTable() {
        return table;
    }

    @PostConstruct
    public void setup() throws IOException {


        if (log.isWarnEnabled())
            log.warn("Updating accounts schema.");

        byte[] tableName = Bytes.toBytes(accountsTableName);

        HBaseConfiguration conf = new HBaseConfiguration();
        conf.setClass(HConstants.REGION_IMPL, IdxRegion.class, IdxRegion.class);
        HBaseAdmin admin = new HBaseAdmin(conf);

        if (!admin.tableExists(tableName)) {

            if (log.isWarnEnabled())
                log.warn("Table '" + accountsTableName + "' does not exist creating.");

            HTableDescriptor table = new HTableDescriptor(tableName);

            IdxColumnDescriptor col = new IdxColumnDescriptor(AccountService.FAMILY);
            IdxIndexDescriptor index = new IdxIndexDescriptor(AccountService.ID, IdxQualifierType.LONG);
            col.addIndexDescriptor(index);

            table.addFamily(col);

            table.addFamily(new HColumnDescriptor(TokenRepository.REMEMBER_ME));
            table.addFamily(new HColumnDescriptor(AccessTokenStore.OAUTH_TOKEN));

            admin.createTable(table);

        } else if (log.isWarnEnabled())
            log.warn("Table '" + this.accountsTableName + "' exists.");

        table = new HTable(conf, tableName);

    }

}
