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

import com.andrewmccall.accounts.core.oauth.AccessTokenStore;

import com.andrewmccall.oauth.AccessToken;
import com.andrewmccall.oauth.OAuth;
import com.andrewmccall.oauth.OAuthConsumer;
import com.andrewmccall.oauth.RequestToken;
import com.andrewmccall.oauth.Service;
import org.easymock.Capture;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.junit.Before;

import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.security.authentication.AuthenticationManager;
import org.easymock.EasyMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.UUID;
import java.net.URLEncoder;

import com.andrewmccall.accounts.core.User;
import com.andrewmccall.accounts.core.RandomTestUtils;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/com/andrewmccall/accounts/accounts-core-config.xml", "classpath:/com/andrewmccall/accounts/accounts-services-config.xml"})
public class OAuthAuthenticationProcessingFilterTest {

    @Resource
    private OAuthAuthenticationProcessingFilter filter;

    private AuthenticationManager authenticationManager;

    @Resource
    private AccessTokenStore accessTokenStore;

    @Resource
    private OAuthConsumer oAuthConsumer;

    @Resource
    private Service service;

    @Resource
    private String protectedResourceId;

    @Before
    public void setup() {
        authenticationManager = EasyMock.createMock(AuthenticationManager.class);
        filter.setAuthenticationManager(authenticationManager);

        EasyMock.reset(oAuthConsumer, accessTokenStore);

    }

    @Test
    public void testGetRequestToken() throws Exception {

        String callbackUrl = "http://testapp.com";

        HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);
        HttpServletResponse response = EasyMock.createMock(HttpServletResponse.class);
        HttpSession session = EasyMock.createMock(HttpSession.class);

        expect(request.getSession(true)).andReturn(session);
        expect(session.getAttribute(OAuthAuthenticationProcessingFilter.OAUTH_REQUEST_TOKEN)).andReturn(null);

        expect(request.getCookies()).andReturn(null);
        expect(request.getHeaderNames()).andReturn(Collections.enumeration(Collections.emptyList()));
        expect(request.getLocales()).andReturn(Collections.enumeration(Collections.emptyList()));
        expect(request.getParameterMap()).andReturn(Collections.emptyMap());
        expect(request.getMethod()).andReturn("GET");
        expect(request.getPathInfo()).andReturn(null);
        expect(request.getQueryString()).andReturn(null);
        expect(request.getRequestURI()).andReturn("");
        expect(request.getServerPort()).andReturn(80);
        expect(request.getScheme()).andReturn("http").times(2);
        expect(request.getServerName()).andReturn("testapp.com");
        expect(request.getContextPath()).andReturn("");
        expect(request.getServletPath()).andReturn("");
        expect(request.getRequestURL()).andReturn(new StringBuffer("http://testapp.com"));

        RequestToken requestToken = new RequestToken();
        requestToken.setService(service);
        requestToken.setSecret(UUID.randomUUID().toString());
        requestToken.setValue(UUID.randomUUID().toString());
        requestToken.setCallbackConfirmed(true);

        expect(oAuthConsumer.getRequestToken(service, "http://testapp.com")).andReturn(requestToken);
        response.sendRedirect(filter.getUserAuthorizationRedirectURL(requestToken));

        session.setAttribute(OAuthAuthenticationProcessingFilter.OAUTH_REQUEST_TOKEN, requestToken);
        EasyMock.replay(request, response, session, oAuthConsumer, accessTokenStore, authenticationManager);

        filter.attemptAuthentication(request, response);
        
        EasyMock.verify(request, response, session, oAuthConsumer, accessTokenStore, authenticationManager);

    }

    @Test
    public void testGetAccessToken() throws Exception {

        HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);
        HttpServletResponse response = EasyMock.createMock(HttpServletResponse.class);
        HttpSession session = EasyMock.createMock(HttpSession.class);

        expect(request.getSession(true)).andReturn(session);

        RequestToken requestToken = new RequestToken();
        requestToken.setService(service);
        requestToken.setCallbackConfirmed(true);
        requestToken.setValue(UUID.randomUUID().toString());
        requestToken.setSecret(UUID.randomUUID().toString());        

        expect(session.getAttribute(OAuthAuthenticationProcessingFilter.OAUTH_REQUEST_TOKEN)).andReturn(requestToken);

        String verifier = UUID.randomUUID().toString();
        expect(request.getParameter(OAuth.OAUTH_VERIFIER)).andReturn(verifier);

        expect(session.getId()).andReturn(UUID.randomUUID().toString());

        AccessToken accessToken = new AccessToken();
        accessToken.setService(service);
        accessToken.setValue(UUID.randomUUID().toString());
        accessToken.setSecret(UUID.randomUUID().toString());
        
        expect(oAuthConsumer.getAccessToken(requestToken)).andReturn(accessToken);

        expect(request.getRemoteAddr()).andReturn("127.0.0.1");
        expect(request.getSession(false)).andReturn(session);

        Capture<OAuthAuthentication> auth = new Capture<OAuthAuthentication>();
        expect(authenticationManager.authenticate(EasyMock.capture(auth))).andReturn(null);

        replay(request, response, session, oAuthConsumer, accessTokenStore, authenticationManager);

        filter.attemptAuthentication(request, response);
        assertEquals(accessToken.getValue(), auth.getValue().getToken().getValue());
        assertEquals(accessToken.getSecret(), auth.getValue().getToken().getSecret());

        EasyMock.verify(request, response, session, oAuthConsumer, accessTokenStore, authenticationManager);
    }

}
