package com.andrewmccall.accounts.hibernate.springsecurity.rememberme;

import com.andrewmccall.accounts.core.springsecurity.rememberme.RememberMeToken;
import com.andrewmccall.accounts.core.User;
import com.andrewmccall.accounts.core.AccountsException;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import javax.annotation.Resource;

/**
 * Hbase TokenRepository implementation.
 */
@Repository
@Transactional(readOnly = true)
public class TokenRepository implements com.andrewmccall.accounts.core.springsecurity.rememberme.TokenRepository {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Resource
    private SessionFactory sessionFactory;

    @Override
    @Transactional(readOnly = false)
    public void create(RememberMeToken rememberMeToken) throws AccountsException {
        Session session = sessionFactory.getCurrentSession();
        session.persist(rememberMeToken);
        //session.flush();
    }

    @Override
    public boolean exists(String series, User user) throws AccountsException {
        boolean exists = (getToken(series, user) != null);
        if (log.isTraceEnabled())
            log.trace("Checking if token exists [" + exists + "] for series: " + series + " and user " + user);
        return exists;
    }

    @Override
    public RememberMeToken getToken(String series, User user) throws AccountsException {
        return (RememberMeToken) sessionFactory.getCurrentSession().get(RememberMeToken.class, new RememberMeToken(series, user));
    }

    @Override
    @Transactional(readOnly = false)
    public void removeUserTokens(User user) {
        if (log.isDebugEnabled())
            log.debug("Removing all tokens for User: " + user);
        Query query = sessionFactory.getCurrentSession().createQuery("delete from RememberMeToken where user = :user");
        query.setParameter("user", user);
        int updated = query.executeUpdate();
        if (log.isTraceEnabled())
            log.trace("Removed " + updated + " tokens.");
    }

    @Override
    @Transactional(readOnly = false)
    public void update(RememberMeToken token) throws AccountsException {
        sessionFactory.getCurrentSession().update(token);
    }
}
