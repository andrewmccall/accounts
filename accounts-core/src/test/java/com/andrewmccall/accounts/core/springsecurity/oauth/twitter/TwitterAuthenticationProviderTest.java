/*
 * Copyright (c) 2010. Andrew McCall [andrew@andrewmccall.com] - All Rights Reserved.
 *
 * Unless explicitly stated otherwise, all rights are owned by or controlled by Andrew McCall.
 *
 * Except as otherwise expressly permitted under copyright law the content not be copied, reproduced,
 * republished, downloaded, posted, broadcast or transmitted in any way without first obtaining Andrew
 * McCall's written permission or that of the copyright owner.
 */

package com.andrewmccall.accounts.core.springsecurity.oauth.twitter;

import com.andrewmccall.accounts.core.oauth.AccessTokenStore;
import com.andrewmccall.accounts.core.springsecurity.oauth.OAuthAuthentication;
import com.andrewmccall.oauth.AccessToken;
import com.andrewmccall.oauth.Service;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.junit.runner.RunWith;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.security.core.Authentication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import org.json.JSONObject;
import org.json.JSONException;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import javax.annotation.Resource;

import com.andrewmccall.accounts.core.AccountService;
import com.andrewmccall.accounts.core.User;
import com.andrewmccall.accounts.core.AccountsException;
import com.andrewmccall.accounts.core.RandomTestUtils;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/com/andrewmccall/accounts/accounts-core-config.xml", "classpath:/com/andrewmccall/accounts/accounts-services-config.xml"})
public class TwitterAuthenticationProviderTest {

    @Resource
    private TwitterAuthenticationProvider authenticationProvider;

    @Resource
    private Service service;

    @Resource
    private AccountService accountService;

    @Resource
    private HttpClient httpClient;

    @Resource
    private AccessTokenStore accessTokenStore;

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Mock
    HttpResponse response;
    @Mock
    StatusLine statusLine;
    @Mock
    HttpEntity entity;

    private ArgumentCaptor<HttpGet> cap; 


    private User user;
    private AccessToken token;
    private OAuthAuthentication auth;

    private String url;

    public TwitterAuthenticationProviderTest() throws MalformedURLException {
        this.url = "http://twitter.com/account/verify_credentials.json";
    }

    @Before
    public void resetObjects() throws AccountsException {
        MockitoAnnotations.initMocks(this);
        user = new User();
        RandomTestUtils.generateUser(user);
        token = new AccessToken(){};
        token.setService(service);
        auth = new OAuthAuthentication(token, null);
        reset(service, accountService, accessTokenStore, httpClient, response, statusLine, entity);
        when(response.getEntity()).thenReturn(entity);

        cap = ArgumentCaptor.forClass(HttpGet.class);
    }

    @Test
    public void testAuthenticateNewUser() throws Exception {

        reset(service, accountService, httpClient, accessTokenStore);


        HttpGet request = new HttpGet(url);
        service.prepare(cap.capture(), eq(token));

        when(accountService.twitterIdExists(user.getTwitterId())).thenReturn(false);

        when(httpClient.execute(cap.capture())).thenReturn(response);
        when(response.getStatusLine()).thenReturn(statusLine);
        when(statusLine.getStatusCode()).thenReturn(200);
        when(statusLine.getReasonPhrase()).thenReturn("OK");

        when(response.getEntity()).thenReturn(entity);
        when(entity.getContent()).thenReturn(userAsStream(user));
        entity.consumeContent();

        doNothing().when(accountService).createUser(eq(user));


        authenticationProvider.authenticate(auth);

        assertEquals(request.getURI(), cap.getValue().getURI());

    }

    @Test
    public void testAuthenticatedExistingUser() throws Exception {

        RandomTestUtils.setId(user);

        HttpGet request = new HttpGet(url);
        when(accountService.twitterIdExists(user.getTwitterId())).thenReturn(true);

        when(httpClient.execute(cap.capture())).thenReturn(response);
        when(response.getStatusLine()).thenReturn(statusLine);
        when(statusLine.getStatusCode()).thenReturn(200);
        when(statusLine.getReasonPhrase()).thenReturn("OK");

        when(entity.getContent()).thenReturn(userAsStream(user));
        entity.consumeContent();

        when(accountService.getUserForTwitterId(user.getTwitterId())).thenReturn(user);
        ArgumentCaptor<AccessToken> tc = ArgumentCaptor.forClass(AccessToken.class);

        authenticationProvider.authenticate((Authentication) auth);
        
        verify(service).prepare(cap.capture(), eq(token));
        verify(accessTokenStore).storeToken(tc.capture(), eq(user));

        assertTrue(tc.getValue() != null);

    }

    @Test
    public void testAuthenticatedExistingUserAndUpdate() throws Exception {

        RandomTestUtils.setId(user);
        reset(service, accountService, accessTokenStore, httpClient);


        HttpGet request = new HttpGet(url);
        service.prepare(cap.capture(), eq(token));

        when(accountService.twitterIdExists(user.getTwitterId())).thenReturn(true);

        when(httpClient.execute(cap.capture())).thenReturn(response);
        when(response.getStatusLine()).thenReturn(statusLine);
        when(statusLine.getStatusCode()).thenReturn(200);
        when(statusLine.getReasonPhrase()).thenReturn("OK");

        User nuser = new User();
        RandomTestUtils.generateUser(nuser);
        nuser.setId(user.getId());
        nuser.setTwitterId(user.getTwitterId());

        when(entity.getContent()).thenReturn(userAsStream(nuser));

        entity.consumeContent();

        when(accountService.getUserForTwitterId(user.getTwitterId())).thenReturn(user);

        accountService.update(nuser);

        authenticationProvider.authenticate(auth);

    }

    private InputStream userAsStream(final User user) throws JSONException, UnsupportedEncodingException {

        if (log.isDebugEnabled())
            log.debug("Creating new inputStream for user:" + user);

        JSONObject obj = new JSONObject();
        obj.put("id", user.getTwitterId());
        obj.put("name", user.getName());
        obj.put("location", user.getLocation());
        obj.put("screen_name", user.getUsername());
        obj.put("description", user.getBio());
        obj.put("url", user.getWebsite());
        obj.put("time_zone", user.getTimeZone().getID());
        obj.put("followers_count", user.getFollowers());
        obj.put("friends_count", user.getFriends());

        if (log.isDebugEnabled())
            log.debug("JSON Object: " + obj.toString());

        return new ByteArrayInputStream(obj.toString().getBytes("UTF-8"));

    }
}
