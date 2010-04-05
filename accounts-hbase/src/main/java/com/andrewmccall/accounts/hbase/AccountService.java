/*
 * Copyright (c) 2010. Andrew McCall [andrew@andrewmccall.com] - All Rights Reserved.
 */

package com.andrewmccall.accounts.hbase;

import com.andrewmccall.accounts.core.User;
import com.andrewmccall.accounts.core.AccountsException;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.client.idx.IdxScan;
import org.apache.hadoop.hbase.client.idx.exp.Comparison;
import org.apache.hadoop.hbase.client.idx.exp.Expression;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.UUID;
import java.io.IOException;

/**
 * Account service implemented using Hbase as the backing data store. This class is responsible for updating and
 * maintaining the relationships between objects since that is not provided by Hbase.
 */
@Service
public class AccountService implements com.andrewmccall.accounts.core.AccountService<UUID> {

    public static final byte[] FAMILY = Bytes.toBytes("twitter");

    public static final String USERNAME_INDEX_ID = "USERNAME_INDEX";
    public static final String TWITTER_INDEX_ID = "TWITTER_INDEX";

    public static final byte[] USERNAME = Bytes.toBytes("username");
    public static final byte[] NAME = Bytes.toBytes("name");
    public static final byte[] ID = Bytes.toBytes("ID");
    public static final byte[] BIO = Bytes.toBytes("bio");
    public static final byte[] WEBSITE = Bytes.toBytes("website");
    public static final byte[] LOCATION = Bytes.toBytes("location");
    public static final byte[] TIMEZONE = Bytes.toBytes("timezone");
    public static final byte[] LANGUAGE = Bytes.toBytes("language");
    public static final byte[] COUNTRY = Bytes.toBytes("country");

    private static final byte[][] BASE_COLUMNS;

    static {
        BASE_COLUMNS = new byte[][]{NAME, BIO, WEBSITE, LOCATION, TIMEZONE, LANGUAGE, COUNTRY};
    }

    public static final byte[][] ALL_COLUMNS;

    static {
        ALL_COLUMNS = new byte[BASE_COLUMNS.length + 2][];
        System.arraycopy(BASE_COLUMNS, 0, ALL_COLUMNS, 0, BASE_COLUMNS.length);
        ALL_COLUMNS[BASE_COLUMNS.length] = ID;
        ALL_COLUMNS[BASE_COLUMNS.length + 1] = USERNAME;
    }

    private static final byte[][] BY_USERNAME_COLUMNS;

    static {
        BY_USERNAME_COLUMNS = new byte[BASE_COLUMNS.length + 1][];
        System.arraycopy(BASE_COLUMNS, 0, BY_USERNAME_COLUMNS, 0, BASE_COLUMNS.length);
        BY_USERNAME_COLUMNS[BASE_COLUMNS.length] = ID;
    }


    private static final byte[][] BY_TWITTER_COLUMNS;

    static {
        BY_TWITTER_COLUMNS = new byte[BASE_COLUMNS.length + 1][];
        System.arraycopy(BASE_COLUMNS, 0, BY_TWITTER_COLUMNS, 0, BASE_COLUMNS.length);
        BY_TWITTER_COLUMNS[BASE_COLUMNS.length] = USERNAME;
    }

    @Resource
    private TableFactory tableFactory;

    private Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * creates a new user in persistent storage. Implementations MUST set an Id such that user.getId() != 0, anything
     * else goes.
     *
     * @param user The user to create.
     */
    @Override
    public void createUser(User<UUID> user) throws AccountsException {
        if (log.isDebugEnabled())
            log.debug("Creating user: " + user);

        if (this.twitterIdExists(user.getTwitterId()))
            throw new AccountsException("TwitterId '" + user.getTwitterId() + "' already in use");
        
        // we need to create a user Id for this user.. how do we want to do that?
        try {
            while (user.getId() == null || tableFactory.getTable().exists(new Get(Bytes.toBytes(user.getId().toString()))))
                user.setId(UUID.randomUUID());
        } catch (IOException e) {
            throw new AccountsException("Couldn't determine if the ID existed.", e);
        }

        Put put = new Put(Bytes.toBytes(user.getId().toString()));
        Delete delete = new Delete(Bytes.toBytes(user.getId().toString()));

        toOperations(put, delete, user);
        try {
            if (log.isTraceEnabled())
                log.trace("Calling commit on PUT: " + put);
            tableFactory.getTable().put(put);

        } catch (IOException e) {
            if (log.isErrorEnabled())
                log.error("IOException thrown creating user: " + user, e);
            throw new AccountsException("IOException thrown creating user: " + user, e);
        }
        if (log.isDebugEnabled())
            log.debug("Created user: " + user);
    }

    /**
     * Gets a user based on the id of the user.
     *
     * @param id the User's id.
     * @return a user if one exists, null if one can't be found.
     * @throws com.andrewmccall.accounts.core.AccountsException
     *          to wrap any underlying exception thrown.
     */
    @Override
    public User<UUID> getUser(UUID id) throws AccountsException {

        Result result;

        if (log.isInfoEnabled())
            log.info("Getting user for id: " + id);

        Get get = new Get(Bytes.toBytes(id.toString()));
        get.addFamily(FAMILY);
        
        try {

            if (log.isDebugEnabled())
                log.debug("Calling get: " + get.toString());

            result = tableFactory.getTable().get(get);
        } catch (IOException e) {
            throw new AccountsException("Failed to get User for id: " + id, e);
        }
        if (!result.isEmpty()) {
            User<UUID> user = resultToUser(result);
            user.setId(id);

            if (log.isTraceEnabled())
                log.trace("Found user: " + user);

            //if (user.getId().equals(id))
            return user;
        }
        if (log.isDebugEnabled())
            log.debug("No user found for id.");
        throw new AccountsException("Failed to get User for id: " + id);
    }

    /**
     * Gets a user based on the id of the user.
     *
     * @param id the User's id.
     * @return a user if one exists, null if one can't be found.
     */
    @Override
    public User<UUID> getUser(final String id) throws AccountsException {
        try {
            return getUser(UUID.fromString(id));
        } catch (IllegalArgumentException e) {
            throw new AccountsException("Invalid ID", e);
        }
    }


    @Override
    public User<UUID> getUserForTwitterId(long twitterId) throws AccountsException {
        if (log.isTraceEnabled())
            log.debug("Getting user for twitterId: " + twitterId);
        try {
            IdxScan scan = new IdxScan();
            scan.setExpression(Expression.comparison(FAMILY, ID, Comparison.Operator.EQ, Bytes.toBytes(twitterId)));
            scan.setFilter(new SingleColumnValueFilter(FAMILY, ID, CompareFilter.CompareOp.EQUAL, Bytes.toBytes(twitterId)));
            ResultScanner scanner = tableFactory.getTable().getScanner(scan);
            Result row = scanner.next();
            scanner.close();
            if (row != null) {
                User<UUID> user = resultToUser(row);
                if (user != null) {
                    if (log.isDebugEnabled())
                        log.debug("Found user: " + user);
                    if (twitterId == (user.getTwitterId()))
                        return user;
                    else if (log.isTraceEnabled())
                        log.trace("That's not the user you're looking for expected: " + twitterId + " but was " + user.getTwitterId());
                }
            }
            if (log.isDebugEnabled())
                log.debug("No user found, returning null.");
            return null;
        } catch (IOException e) {
            throw new AccountsException("Failed to get User for twitterId: " + twitterId, e);
        }
    }

    @Override
    public boolean twitterIdExists(long twitterId) throws AccountsException {
        return getUserForTwitterId(twitterId) != null;
    }

    /**
     * updates a user.
     *
     * @param user the user to update.
     */
    @Override
    public void update(User user) throws AccountsException {
        if (user.getId() == null)
            throw new AccountsException("User does not have an ID, has never been stored! user: " + user);
        try {
            Put put = new Put(Bytes.toBytes(user.getId().toString()));
            Delete delete = new Delete(Bytes.toBytes(user.getId().toString()));
            toOperations(put, delete, user);

            tableFactory.getTable().put(put);

            if (!delete.isEmpty()) {
                if (log.isDebugEnabled())
                    log.debug("Calling delete: " + delete);
                tableFactory.getTable().delete(delete);
            } else if (log.isDebugEnabled())
                log.debug("Delete is empty - ignored.");

        } catch (IOException e) {
            throw new AccountsException("Updating user failed: " + user, e);
        }
    }

    protected User<UUID> resultToUser(Result result) {
        User<UUID> user = new User<UUID>();

        user.setId(UUID.fromString(Bytes.toString(result.getRow())));

        // set the fields
        user.setName(Bytes.toString(result.getValue(FAMILY, NAME)));
        user.setUsername(Bytes.toString(result.getValue(FAMILY, USERNAME)));

        user.setTwitterId(Bytes.toLong(result.getValue(FAMILY, ID)));
        user.setBio(Bytes.toString(result.getValue(FAMILY, BIO)));
        user.setWebsite(Bytes.toString(result.getValue(FAMILY, WEBSITE)));
        user.setLocation(Bytes.toString(result.getValue(FAMILY, LOCATION)));

        user.setTimeZoneId(Bytes.toString(result.getValue(FAMILY, TIMEZONE)));

        return user;
    }

    protected void toOperations(Put put, Delete delete, User user) {

        setField(put, delete, NAME, user.getName());
        setField(put, delete, USERNAME, user.getUsername());

        setField(put, delete, BIO, user.getBio());
        setField(put, delete, WEBSITE, user.getWebsite());
        setField(put, delete, LOCATION, user.getLocation());
        put.add(FAMILY, ID, Bytes.toBytes(user.getTwitterId()));

        // Timezone and Locale are transient, stored and rebuilt from components.
        setField(put, delete, TIMEZONE, user.getTimeZoneId());

    }

    protected void setField(Put put, Delete delete, byte[] column, String value) {
        if (StringUtils.trimToNull(value) != null) {
            put.add(FAMILY, column, Bytes.toBytes(value));
        } else
            delete.deleteColumn(FAMILY, column);
    }

}