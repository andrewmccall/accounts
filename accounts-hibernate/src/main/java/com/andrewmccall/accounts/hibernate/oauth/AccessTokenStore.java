package com.andrewmccall.accounts.hibernate.oauth;

import com.andrewmccall.oauth.Service;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.andrewmccall.accounts.core.User;

import javax.annotation.Resource;

@Repository
@Transactional(readOnly = true)
public class AccessTokenStore implements com.andrewmccall.accounts.core.oauth.AccessTokenStore {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Resource
    private SessionFactory sessionFactory;

    @Override
    public com.andrewmccall.accounts.core.oauth.AccessToken getToken(User user, Service service) {
        if (log.isTraceEnabled())
            log.trace(new StringBuffer("Getting token for Service: ").append(service).append(" and user: ").append(user).toString());
        return (AccessToken) sessionFactory.getCurrentSession().get(AccessToken.class, new AccessToken(service, user));
    }

    @Override
    @Transactional(readOnly = false)
    public void storeToken(com.andrewmccall.accounts.core.oauth.AccessToken token) {
        if (log.isTraceEnabled())
            log.trace("Storing OAuthToken: " + token);

        AccessToken at;

        if (!(token.getService() instanceof Service))
            throw new IllegalArgumentException("Could not store service.");

        Service service = (Service) token.getService();

        if ((at = (AccessToken) sessionFactory.getCurrentSession().get(AccessToken.class, new AccessToken(service, token.getUser()))) != null) {
            at.setSecret(token.getSecret());
            at.setValue(token.getValue());

            if (log.isDebugEnabled())
                log.debug("OAuthTokenUtil exists - calling update");
            sessionFactory.getCurrentSession().update(at);
        } else {

            at = new AccessToken();
            at.setSecret(token.getSecret());
            at.setValue(token.getValue());

            at.setService(token.getService());
            at.setUser(token.getUser());

            if (log.isDebugEnabled())
                log.debug("OAuthTokenUtil doesn't exists - calling persist");
            sessionFactory.getCurrentSession().persist(at);
        }
    }
}
