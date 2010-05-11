/*
 * Copyright (c) 2010. Andrew McCall [andrew@andrewmccall.com] - All Rights Reserved.
 *
 * Unless explicitly stated otherwise, all rights are owned by or controlled by Andrew McCall.
 *
 * Except as otherwise expressly permitted under copyright law the content not be copied, reproduced,
 * republished, downloaded, posted, broadcast or transmitted in any way without first obtaining Andrew
 * McCall's written permission or that of the copyright owner.
 */

package com.andrewmccall.accounts.core.springsecurity;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import com.andrewmccall.accounts.core.SecurityService;
import com.andrewmccall.accounts.core.AccountService;
import com.andrewmccall.accounts.core.AccountsException;
import com.andrewmccall.accounts.core.User;

/**
 * Exposes the the accounts services to the spring-security framework
 */
@Service
public class SpringSecurityService implements SecurityService, UserDetailsService {

    protected Log log;

    @Resource
    protected AccountService accountService;

    private static final List<GrantedAuthority> AUTHORITIES_TEMP = new ArrayList<GrantedAuthority>(1);
    static {
        AUTHORITIES_TEMP.add(new GrantedAuthorityImpl("ROLE_USER"));
    }
    public static final List<GrantedAuthority> AUTHORITIES = Collections.unmodifiableList(AUTHORITIES_TEMP);


    public SpringSecurityService() {
        log = LogFactory.getLog(this.getClass());
        if (log.isDebugEnabled())
            log.debug("Created new instance of " + this.getClass());
    }

    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException, DataAccessException {

        if (log.isDebugEnabled())
            log.debug("Loading UserDetails for id: " + id);


        User user = null;
        try {
            user = accountService.getUser(id);
        } catch (AccountsException e) {
            if (log.isWarnEnabled())
                log.warn("Cannot load user for id: " + id, e);
            throw new DataAccessResourceFailureException ("Cannot load user for id: " + id, e);
        }
        if (user == null) {
            if (log.isInfoEnabled())
                log.info("No user found for id: " + id);
            throw new UsernameNotFoundException("No such user could be found.");
        }

        UserDetails userDetails = new UserDetailsImpl(user, AUTHORITIES);
        if (log.isInfoEnabled())
            log.info("found user and created UserDetails: " + userDetails);
        return userDetails;
    }

    /**
     * Gets the User from the current SecurityContext.
     *
     * @return the User attached to the current securityContext.
     */
    public User getUser() {

        Authentication auth;
        if ((auth = SecurityContextHolder.getContext().getAuthentication()) == null) {
            if (log.isWarnEnabled())
                log.warn("The Authentication was null");
            return null;
        } else if (log.isTraceEnabled())
            log.trace("Returned Authentication: " + auth);

        if (!auth.isAuthenticated()) {
            if (log.isTraceEnabled())
                log.trace("The Authentication object has not been fully Authenticated, no user may be returned.");
            return null;
        }

        if (!(auth instanceof com.andrewmccall.accounts.core.springsecurity.Authentication)) {
            if (log.isTraceEnabled())
                log.trace("Authentication was not an com.andrewmccall.accounts.core.springsecurity.Authentication. " + auth.toString());
            return null;
        }
        return ((com.andrewmccall.accounts.core.springsecurity.Authentication)auth).getUser();
    }
}