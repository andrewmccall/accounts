/*
 * Copyright (c) 2010. Andrew McCall [andrew@andrewmccall.com] - All Rights Reserved.
 *
 * Unless explicitly stated otherwise, all rights are owned by or controlled by Andrew McCall.
 *
 * Except as otherwise expressly permitted under copyright law the content not be copied, reproduced,
 * republished, downloaded, posted, broadcast or transmitted in any way without first obtaining Andrew
 * McCall's written permission or that of the copyright owner.
 */

package com.andrewmccall.accounts.core.springsecurity.oauth;


import com.andrewmccall.accounts.core.oauth.AccessToken;
import com.andrewmccall.oauth.OAuth;
import com.andrewmccall.oauth.OAuthConsumer;
import com.andrewmccall.oauth.RequestToken;
import com.andrewmccall.oauth.Service;
import com.andrewmccall.oauth.exception.OAuthException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.security.web.PortResolver;
import org.springframework.security.web.PortResolverImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Authenticates a user via oauth.
 */
public class OAuthAuthenticationProcessingFilter extends AbstractAuthenticationProcessingFilter {

    public static final String OAUTH_REQUEST_TOKEN = "OAUTH_REQUEST_TOKEN";

    @Resource
    private OAuthConsumer oAuthConsumer;

    @Resource
    private Service service;

    private PortResolver portResolver = new PortResolverImpl();

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public OAuthAuthenticationProcessingFilter() {
        this("/");
    }

    protected OAuthAuthenticationProcessingFilter(String defaultFilterProcessesUrl) {
        super(defaultFilterProcessesUrl);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws AuthenticationException, IOException, ServletException {

        if (log.isDebugEnabled())
            log.debug("Attempting authentication");


        HttpSession session = httpServletRequest.getSession(true);
        RequestToken requestToken = (RequestToken) session.getAttribute(OAUTH_REQUEST_TOKEN);
        if (requestToken == null) {
            // If the token is null, then we need to start the Authorization process.
            try {
                requestToken = getRequestToken(httpServletRequest, httpServletResponse);
            } catch (OAuthException e) {
                if (log.isInfoEnabled())
                    log.info("Failed to get a RequestToken", e);
                throw new OAuthAuthenticationException("Failed to get a new RequestToken!", e);
            }
            session.setAttribute(OAUTH_REQUEST_TOKEN, requestToken);
            return null;
        }

        AccessToken token = null;
        try {
            token = authenticate(requestToken, httpServletRequest, httpServletResponse);
        } catch (OAuthException e) {
            throw new OAuthAuthenticationException("Failed to get AccessToken!", e);
        }

        // delegate to the authentication provider
        Authentication authentication = new OAuthAuthentication(token, authenticationDetailsSource.buildDetails(httpServletRequest));
        authentication = this.getAuthenticationManager().authenticate(authentication);
        if (log.isDebugEnabled())
            log.debug("AuthenticationManager returned: " + authentication);

        return authentication;

    }

    public AccessToken authenticate(RequestToken token, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, OAuthException {

        if (log.isDebugEnabled()) {
            log.debug("Obtaining access token");
        }

        token.setVerifier(httpServletRequest.getParameter(OAuth.OAUTH_VERIFIER));

        //authorize the request token and store it.
        AccessToken accessToken = new AccessToken(oAuthConsumer.getAccessToken(token));

        if (log.isDebugEnabled()) {
            log.debug("Access token " + token + " obtained");
        }

        return accessToken;
    }

    protected RequestToken getRequestToken(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, OAuthException {
        if (log.isDebugEnabled()) {
            log.debug("Obtaining request token");
        }

        //obtain authorization.
        String callbackURL = getCallbackURL(httpServletRequest);
        RequestToken token = oAuthConsumer.getRequestToken(service, callbackURL);

        if (log.isDebugEnabled()) {
            log.debug("Request token obtained, callbackURL " + callbackURL + ": " + token);
        }
        String redirect = getUserAuthorizationRedirectURL(token);

        if (log.isDebugEnabled()) {
            log.debug("Redirecting request to " + redirect + " for user authorization of the request token.");
        }
        httpServletResponse.sendRedirect(redirect);
        return token;
    }

    /**
     * Get the callback URL for the specified request.
     *
     * @param request The request.
     * @return The callback URL.
     */
    protected String getCallbackURL(HttpServletRequest request) {
        return new DefaultSavedRequest(request, portResolver).getRequestURL();
    }

    /**
     * Get the URL to which to redirect the user for authorization of protected resources.
     *
     * @param requestToken The request token.
     * @return The URL.
     */
    protected String getUserAuthorizationRedirectURL(RequestToken requestToken) throws AuthenticationException {
        try {
            String baseURL = service.getUserAuthorizationUrl();
            StringBuilder builder = new StringBuilder(baseURL);
            char appendChar = baseURL.indexOf('?') < 0 ? '?' : '&';
            builder.append(appendChar).append("oauth_token=");
            builder.append(URLEncoder.encode(requestToken.getValue(), "UTF-8"));
            return builder.toString();
        }
        catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

}
