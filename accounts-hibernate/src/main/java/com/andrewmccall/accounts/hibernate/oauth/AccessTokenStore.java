package com.andrewmccall.accounts.hibernate.oauth;

import com.andrewmccall.oauth.AccessToken;
import com.andrewmccall.oauth.Service;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.andrewmccall.accounts.core.User;

import javax.annotation.Resource;
import java.io.Serializable;

@Repository
@Transactional(readOnly = true)
public class AccessTokenStore implements com.andrewmccall.accounts.core.oauth.AccessTokenStore {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Resource
    private SessionFactory sessionFactory;

    @Override
    public AccessToken getToken(User user, Service service) {
        if (log.isTraceEnabled())
            log.trace(new StringBuffer("Getting token for Service: ").append(service).append(" and user: ").append(user).toString());

        StoredToken token;
        if (service instanceof com.andrewmccall.oauth.oauth_2_0.Service)
            token = new StoredToken_2_0(service.getId(), user);
        else if (service instanceof com.andrewmccall.oauth.oauth_1_0.Service)
            token = new StoredToken_1_0(service.getId(), user);
        else throw new IllegalArgumentException("Service is wrong type.");

        token = (StoredToken) sessionFactory.getCurrentSession().get(StoredToken.class, token);
        if (token == null)
            return null;
        AccessToken at = token.getAccessToken();
        at.setService(service);
        return at;
    }

    @Override
    @Transactional(readOnly = false)
    public void storeToken(AccessToken token, User user) {
        if (log.isTraceEnabled())
            log.trace("Storing OAuthToken: " + token);

        Service service = token.getService();

        StoredToken storedToken;
        if (service instanceof com.andrewmccall.oauth.oauth_2_0.Service)
            storedToken = new StoredToken_2_0(service.getId(), user);
        else if (service instanceof com.andrewmccall.oauth.oauth_1_0.Service)
            storedToken = new StoredToken_1_0(service.getId(), user);
        else throw new IllegalArgumentException("Service is wrong type.");


        if ((storedToken = (StoredToken) sessionFactory.getCurrentSession().get(StoredToken.class, storedToken)) != null) {

            if (log.isDebugEnabled())
                log.debug("OAuthTokenUtil exists - calling update");
            storedToken.setAccessToken(token);
            sessionFactory.getCurrentSession().update(storedToken);
        } else {
            if (service instanceof com.andrewmccall.oauth.oauth_2_0.Service)
                storedToken = new StoredToken_2_0(service.getId(), user);
            else
                storedToken = new StoredToken_1_0(service.getId(), user);
            storedToken.setAccessToken(token);
            storedToken.setServiceId(token.getService().getId());
            storedToken.setUser(user);

            if (log.isDebugEnabled())
                log.debug("OAuthTokenUtil doesn't exists - calling persist");
            sessionFactory.getCurrentSession().persist(storedToken);
        }
    }

    public abstract class StoredToken implements Serializable {
        private String serviceId;
        private User user;
        private AccessToken accessToken;

        public StoredToken() {
        }

        public StoredToken(String serviceId, User user) {
            this.serviceId = serviceId;
            this.user = user;
        }

        public String getServiceId() {
            return serviceId;
        }

        public void setServiceId(String serviceId) {
            this.serviceId = serviceId;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }

        public AccessToken getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(AccessToken accessToken) {
            this.accessToken = accessToken;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            StoredToken that = (StoredToken) o;

            if (!serviceId.equals(that.serviceId)) return false;
            if (!user.equals(that.user)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = serviceId.hashCode();
            result = 31 * result + user.hashCode();
            return result;
        }
    }

    public class StoredToken_1_0 extends StoredToken {
        public StoredToken_1_0() {
            super();
        }

        public StoredToken_1_0(String serviceId, User user) {
            super(serviceId, user);
        }
    }

    public class StoredToken_2_0 extends StoredToken {
        public StoredToken_2_0() {
            super();
        }

        public StoredToken_2_0(String serviceId, User user) {
            super(serviceId, user);
        }
    }
}
