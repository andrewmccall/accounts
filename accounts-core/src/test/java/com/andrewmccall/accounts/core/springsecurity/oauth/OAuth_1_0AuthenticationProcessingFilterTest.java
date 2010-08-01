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

import com.andrewmccall.oauth.oauth_1_0.AccessToken;
import com.andrewmccall.oauth.oauth_1_0.OAuth;
import com.andrewmccall.oauth.oauth_1_0.RequestToken;
import com.andrewmccall.oauth.oauth_1_0.Service;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.junit.Before;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.security.authentication.AuthenticationManager;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/com/andrewmccall/accounts/accounts-core-config.xml", "classpath:/com/andrewmccall/accounts/accounts-services-config.xml"})
public class OAuth_1_0AuthenticationProcessingFilterTest {


    @Resource
    private OAuth_1_0AuthenticationProcessingFilter filter10;

    @Resource
    private AccessTokenStore accessTokenStore;

    @Resource
    private Service service;

    @Resource
    private String protectedResourceId;

    @Mock
    AuthenticationManager authenticationManager;
    @Mock
    HttpServletRequest request;
    @Mock
    HttpServletResponse response;
    @Mock
    HttpSession session;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        reset(service);
        reset(accessTokenStore);
        reset(request);
        reset(response);
        reset(session);
        filter10.setAuthenticationManager(authenticationManager);
        when(request.getSession(true)).thenReturn(session);


    }

    @Test
    public void testGetRequestToken() throws Exception {

        String callbackUrl = "http://testapp.com";


        when(session.getAttribute(OAuth_1_0AuthenticationProcessingFilter.OAUTH_REQUEST_TOKEN)).thenReturn(null);

        when(request.getCookies()).thenReturn(null);
        when(request.getHeaderNames()).thenReturn(Collections.enumeration(Collections.emptyList()));
        when(request.getLocales()).thenReturn(Collections.enumeration(Collections.emptyList()));
        when(request.getParameterMap()).thenReturn(Collections.emptyMap());
        when(request.getMethod()).thenReturn("GET");
        when(request.getPathInfo()).thenReturn(null);
        when(request.getQueryString()).thenReturn(null);
        when(request.getRequestURI()).thenReturn("");
        when(request.getServerPort()).thenReturn(80);
        when(request.getScheme()).thenReturn("http");
        when(request.getServerName()).thenReturn("testapp.com");
        when(request.getContextPath()).thenReturn("");
        when(request.getServletPath()).thenReturn("");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://testapp.com"));

        RequestToken requestToken = new RequestToken();
        requestToken.setService(service);
        requestToken.setSecret(UUID.randomUUID().toString());
        requestToken.setValue(UUID.randomUUID().toString());
        requestToken.setCallbackConfirmed(true);
        when(service.getUserAuthorizationUrl()).thenReturn("http://testapp.com/signin");
        when(service.getRequestToken(service, "http://testapp.com")).thenReturn(requestToken);


        session.setAttribute(OAuth_1_0AuthenticationProcessingFilter.OAUTH_REQUEST_TOKEN, requestToken);


        filter10.attemptAuthentication(request, response);

        //verify(request, response, session, service, accessTokenStore, authenticationManager);

    }

    @Test
    public void testGetAccessToken() throws Exception {


        RequestToken requestToken = new RequestToken();
        requestToken.setService(service);
        requestToken.setCallbackConfirmed(true);
        requestToken.setValue(UUID.randomUUID().toString());
        requestToken.setSecret(UUID.randomUUID().toString());

        when(session.getAttribute(OAuth_1_0AuthenticationProcessingFilter.OAUTH_REQUEST_TOKEN)).thenReturn(requestToken);

        String verifier = UUID.randomUUID().toString();
        when(request.getParameter(OAuth.OAUTH_VERIFIER)).thenReturn(verifier);

        when(session.getId()).thenReturn(UUID.randomUUID().toString());

        AccessToken accessToken = new AccessToken();
        accessToken.setService(service);
        accessToken.setValue(UUID.randomUUID().toString());
        accessToken.setSecret(UUID.randomUUID().toString());

        when(service.getAccessToken(requestToken)).thenReturn(accessToken);

        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getSession(false)).thenReturn(session);

        ArgumentCaptor<OAuthAuthentication> auth = ArgumentCaptor.forClass(OAuthAuthentication.class);
        when(authenticationManager.authenticate(auth.capture())).thenReturn(null);

        
        filter10.attemptAuthentication(request, response);
        assertEquals(accessToken.getValue(), auth.getValue().getToken().getValue());
        assertEquals(accessToken.getSecret(), auth.getValue().getToken().getSecret());

        //EasyMock.verify(request, response, session, service, accessTokenStore, authenticationManager);
    }

}
