package com.andrewmccall.accounts.hibernate;

import com.andrewmccall.accounts.core.User;
import com.andrewmccall.accounts.core.AccountsException;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * Account service implemented using Hbase as the backing data store. This class is responsible for updating and
 * maintaining the relationships between objects since that is not provided by Hbase.
 */
@Repository
@Transactional(readOnly = true)
public class AccountService implements com.andrewmccall.accounts.core.AccountService<Long> {

    private static final String TWITTER_QUERY = "from User as user where user.twitterId = :twitterId";
    private static final String USER_QUERY = "from User as user where user.username = :username";

    @Resource
    private SessionFactory sessionFactory;

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public AccountService() {
        if (log.isInfoEnabled())
            log.info("Created new instance of " + this.getClass().getName());
    }

    @Override
    @Transactional(readOnly = false)
    public void createUser(User user) throws AccountsException {

        if (log.isDebugEnabled())
            log.debug("Create called for user: " + user);

        Session session = sessionFactory.getCurrentSession();

        if (session.contains(user))
            throw new AccountsException("User exists.");

        if (this.twitterIdExists(user.getTwitterId()))
            throw new AccountsException("Twitter id exists.");

        session.persist(user);

        if (log.isTraceEnabled())
            log.trace("User created with ID: " + user.getId());

    }


    @Override
    public User getUser(Long id) throws AccountsException {
        if (log.isTraceEnabled())
            log.trace("Getting user for id: " + id);

        Session session = sessionFactory.getCurrentSession();
        User u = (User) session.get(User.class, id);

        if (u != null) {
            if (log.isDebugEnabled())
                log.debug("ID: " + id + " returned user: " + u);
            return u;
        }
        if (log.isDebugEnabled())
            log.debug("No user could be found for id: " + id);
        throw new AccountsException("No user could be found for id: " + id);
    }

    /**
     * Gets a user based on the id of the user as a String, required for spring stuff.
     *
     * @param idString the User's id as a string
     * @return a user if one exists, null if one can't be found.
     * @throws com.andrewmccall.accounts.core.AccountsException
     *          to wrap any underlying exception thrown.
     */
    @Override
    public User getUser(String idString) throws AccountsException {
        return getUser(Long.valueOf(idString));
    }

    @Override
    public boolean twitterIdExists(long twitterId) throws AccountsException {
        if (log.isTraceEnabled())
            log.trace("Checking if user exists for twitterId: " + twitterId);
        boolean exists = !(getUserForTwitterId(twitterId) == null);
        if (log.isDebugEnabled())
            log.trace("User exists for twitterId: " + twitterId + "? " + exists);
        return exists;
    }

    @Override
    @Transactional(readOnly = false)
    public void update(User user) throws AccountsException {
        if (log.isDebugEnabled())
            log.debug("Updating user: " + user);
        Session session = sessionFactory.getCurrentSession();
        session.update(user);
    }

    @Override
    public User getUserForTwitterId(long twitterId) throws AccountsException {
        if (log.isTraceEnabled())
            log.trace("Getting user for twitterId: " + twitterId);
        Session session = sessionFactory.getCurrentSession();

        if (log.isTraceEnabled())
            log.trace("got session" +session);

        Query q = session.createQuery(TWITTER_QUERY);
        q.setParameter("twitterId", twitterId);

        if (log.isTraceEnabled())
            log.trace("QUERY: " + q.toString());
        
        User user = (User) q.uniqueResult();
        if (log.isDebugEnabled())
            log.debug("User for twitterId: " + twitterId + " returned: " + user);
        return user;
    }
}