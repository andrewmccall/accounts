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

import com.andrewmccall.accounts.core.oauth.AccessToken;
import com.andrewmccall.accounts.core.oauth.AccessTokenStore;
import com.andrewmccall.accounts.core.springsecurity.oauth.OAuthAuthentication;
import com.andrewmccall.oauth.OAuthConsumer;
import com.andrewmccall.oauth.UrlStringRequestAdapter;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.easymock.Capture;
import org.junit.runner.RunWith;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.security.core.Authentication;
import org.easymock.EasyMock;

import static junit.framework.Assert.assertTrue;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;

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
import java.net.URL;
import java.net.MalformedURLException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/com/andrewmccall/accounts/accounts-core-config.xml", "classpath:/com/andrewmccall/accounts/accounts-services-config.xml"})
public class TwitterAuthenticationProviderTest {

    @Resource
    private TwitterAuthenticationProvider authenticationProvider;

    @Resource
    private OAuthConsumer oAuthConsumer;

    @Resource
    private AccountService accountService;

    @Resource
    private HttpClient httpClient;

    @Resource
    private AccessTokenStore accessTokenStore;

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private User user;
    private AccessToken token;
    private OAuthAuthentication auth;

    private String url;

    public TwitterAuthenticationProviderTest() throws MalformedURLException {
        this.url = "http://twitter.com/account/verify_credentials.json";
    }

    @Before
    public void resetObjects() throws AccountsException {

        user = new User();
        RandomTestUtils.generateUser(user);
        token = new AccessToken();
        auth = new OAuthAuthentication(token, null);

    }

    private InputStream userAsStream (final User user) throws JSONException, UnsupportedEncodingException {

        if (log.isDebugEnabled())
            log.debug("Creating new inputStream for user:" + user);

        JSONObject obj = new JSONObject();
        obj.put("id", user.getTwitterId());
        obj.put("name", user.getName());
        obj.put("location", user.getLocation());
        obj.put("screen_name", user.getUsername());
        obj.put("description", user.getBio());
        obj.put("url", user.getWebsite());
        obj.put("time_zone", user.getTimeZone().getID()) ;
        obj.put("followers_count", user.getFollowers());
        obj.put("friends_count", user.getFriends());

        if (log.isDebugEnabled())
            log.debug("JSON Object: " + obj.toString());

        return new ByteArrayInputStream(obj.toString().getBytes("UTF-8"));

    }

    @Test
    public void testAuthenticateNewUser () throws Exception {

        EasyMock.reset(oAuthConsumer, accountService, httpClient, accessTokenStore);

        Capture<HttpUriRequest> cap = new Capture<HttpUriRequest>();

        expect(oAuthConsumer.prepare(EasyMock.capture(cap), EasyMock.eq(token))).andReturn(new UrlStringRequestAdapter(url));
        expect(accountService.twitterIdExists(user.getTwitterId())).andReturn(false).once();

        HttpResponse response = EasyMock.createMock(HttpResponse.class);
        StatusLine statusLine = EasyMock.createMock(StatusLine.class);
        expect(httpClient.execute(EasyMock.capture(cap))).andReturn(response);
        expect(response.getStatusLine()).andReturn(statusLine).anyTimes();
        expect(statusLine.getStatusCode()).andReturn(200).anyTimes();
        expect(statusLine.getReasonPhrase()).andReturn("OK").anyTimes();

        HttpEntity entity = EasyMock.createMock(HttpEntity.class);
        expect(response.getEntity()).andReturn(entity).times(2);
        expect(entity.getContent()).andReturn(userAsStream(user));
        entity.consumeContent();

        accountService.createUser(EasyMock.eq(user));

        EasyMock.replay(accountService, oAuthConsumer, accessTokenStore, httpClient, response, statusLine, entity);
        authenticationProvider.authenticate(auth);

        assertEquals("The url should be the one we capture!", url, cap.getValue().getURI().toString());

        EasyMock.verify(oAuthConsumer, accountService, accessTokenStore, httpClient, response, statusLine, entity);
        EasyMock.reset(oAuthConsumer, accountService, accessTokenStore, httpClient, response, statusLine, entity);
    }

    @Test
    public void testAuthenticatedExistingUser() throws Exception {

        RandomTestUtils.setId(user);
        EasyMock.reset(oAuthConsumer, accountService, httpClient, accessTokenStore);

        Capture<HttpUriRequest> cap = new Capture<HttpUriRequest>();

        expect(oAuthConsumer.prepare(EasyMock.capture(cap), EasyMock.eq(token))).andReturn(new UrlStringRequestAdapter(url));
        expect(accountService.twitterIdExists(user.getTwitterId())).andReturn(true).once();

        HttpResponse response = EasyMock.createMock(HttpResponse.class);
        StatusLine statusLine = EasyMock.createMock(StatusLine.class);
        expect(httpClient.execute(EasyMock.capture(cap))).andReturn(response);
        expect(response.getStatusLine()).andReturn(statusLine).anyTimes();
        expect(statusLine.getStatusCode()).andReturn(200).anyTimes();
        expect(statusLine.getReasonPhrase()).andReturn("OK").anyTimes();

        HttpEntity entity = EasyMock.createMock(HttpEntity.class);
        expect(response.getEntity()).andReturn(entity).times(2);
        expect(entity.getContent()).andReturn(userAsStream(user));
        entity.consumeContent();

        expect(accountService.getUserForTwitterId(user.getTwitterId())).andReturn(user);
        Capture<AccessToken> tc = new Capture<AccessToken>();
        accessTokenStore.storeToken(EasyMock.capture(tc));

        EasyMock.replay(accountService, oAuthConsumer, accessTokenStore, httpClient, response, statusLine, entity);

        authenticationProvider.authenticate((Authentication)auth);
        assertTrue(tc.hasCaptured());

        EasyMock.verify(oAuthConsumer, accountService, accessTokenStore, httpClient, response, statusLine, entity);
        EasyMock.reset(oAuthConsumer, accountService, accessTokenStore, httpClient, response, statusLine, entity);
    }

    @Test
    public void testAuthenticatedExistingUserAndUpdate() throws Exception {

        RandomTestUtils.setId(user);
        EasyMock.reset(oAuthConsumer, accountService, accessTokenStore, httpClient);

        Capture<HttpUriRequest> cap = new Capture<HttpUriRequest>();

        expect(oAuthConsumer.prepare(EasyMock.capture(cap), EasyMock.eq(token))).andReturn(new UrlStringRequestAdapter(url));
        expect(accountService.twitterIdExists(user.getTwitterId())).andReturn(true).once();

        HttpResponse response = EasyMock.createMock(HttpResponse.class);
        StatusLine statusLine = EasyMock.createMock(StatusLine.class);
        expect(httpClient.execute(EasyMock.capture(cap))).andReturn(response);
        expect(response.getStatusLine()).andReturn(statusLine).anyTimes();
        expect(statusLine.getStatusCode()).andReturn(200).anyTimes();
        expect(statusLine.getReasonPhrase()).andReturn("OK").anyTimes();

        HttpEntity entity = EasyMock.createMock(HttpEntity.class);
        expect(response.getEntity()).andReturn(entity).times(2);

        User nuser = new User();
        RandomTestUtils.generateUser(nuser);
        nuser.setId(user.getId());
        nuser.setTwitterId(user.getTwitterId());
        
        expect(entity.getContent()).andReturn(userAsStream(nuser));

        entity.consumeContent();

        expect(accountService.getUserForTwitterId(user.getTwitterId())).andReturn(user);
        
        accountService.update(nuser);

        EasyMock.replay(accountService, oAuthConsumer, accessTokenStore, httpClient, response, statusLine, entity);
        authenticationProvider.authenticate(auth);

        EasyMock.verify(oAuthConsumer, accountService, accessTokenStore, httpClient, response, statusLine, entity);
        EasyMock.reset(oAuthConsumer, accountService, accessTokenStore, httpClient, response, statusLine, entity);
    }

}
