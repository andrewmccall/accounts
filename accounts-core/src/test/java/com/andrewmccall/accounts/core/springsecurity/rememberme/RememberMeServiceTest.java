/*
 * Copyright (c) 2010. Andrew McCall [andrew@andrewmccall.com] - All Rights Reserved.
 *
 * Unless explicitly stated otherwise, all rights are owned by or controlled by Andrew McCall.
 *
 * Except as otherwise expressly permitted under copyright law the content not be copied, reproduced,
 * republished, downloaded, posted, broadcast or transmitted in any way without first obtaining Andrew
 * McCall's written permission or that of the copyright owner.
 */

package com.andrewmccall.accounts.core.springsecurity.rememberme;

import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.web.authentication.rememberme.InvalidCookieException;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationException;
import org.springframework.security.web.authentication.rememberme.CookieTheftException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.easymock.EasyMock;
import org.easymock.Capture;
import static org.easymock.EasyMock.*;

import javax.annotation.Resource;
import java.util.Random;
import java.util.Date;

import com.andrewmccall.accounts.core.User;
import com.andrewmccall.accounts.core.RandomTestUtils;
import com.andrewmccall.accounts.core.AccountService;
import com.andrewmccall.accounts.core.AccountsException;
import static junit.framework.Assert.*;

/**
 * Tests our RememberMeService implementation
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/com/andrewmccall/accounts/accounts-core-config.xml", "classpath:/com/andrewmccall/accounts/accounts-services-config.xml"})
public class RememberMeServiceTest {

    @Resource
    AccountService accountService;

    @Resource
    RememberMeService rememberMeService;

    @Resource
    TokenRepository tokenRepository;

    private Log log = LogFactory.getLog(this.getClass());

    Random random = new Random();


    private User user;
    Capture<RememberMeToken> capturedToken = new Capture<RememberMeToken>();
    Capture<String> capturedSeries = new Capture<String>();


    @Before
    public void resetObjects() throws AccountsException {
        user = new User();
        RandomTestUtils.generateUser(user);
        user = RandomTestUtils.setId(user);
        EasyMock.reset(tokenRepository, accountService);
        expect(accountService.getUser(user.getId().toString())).andReturn(user).anyTimes();
        EasyMock.replay(accountService);
    }

    @Test
    public void testSuccess() throws AccountsException {

        rememberMeService.setSeriesLength(random.nextInt(100));
        rememberMeService.setTokenLength(random.nextInt(100));

        expect(tokenRepository.exists(capture(capturedSeries), isA(User.class))).andReturn(true).andReturn(false);
        tokenRepository.create(capture(capturedToken));


        EasyMock.replay(tokenRepository);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        Authentication auth = new TestingAuthenticationToken(user.getId().toString(), "");
        rememberMeService.onLoginSuccess(request, response, auth);

        // make sure we've set a token cookie for the user

        RememberMeToken token = capturedToken.getValue();
        String series = capturedSeries.getValue();

        EasyMock.verify(tokenRepository);
        EasyMock.reset(tokenRepository);

        expect(tokenRepository.getToken(series, user)).andReturn(token).anyTimes();
        tokenRepository.update(capture(capturedToken));

        EasyMock.replay(tokenRepository);


        assertNotNull("The cookieToken should ahve been set!", token);
        // make sure we've stored the token

        RememberMeToken rt = tokenRepository.getToken(token.getSeries(), user);
        assertNotNull("The repository should have a token", rt);
        assertEquals("The token should be the same as the one we set as a cookie!", token, rt);

        assertEquals("The series lenght is not right!", rememberMeService.getSeriesLength(), Base64.decodeBase64(rt.getSeries().getBytes()).length);
        assertEquals("The token lenght is not right!", rememberMeService.getTokenLength(), Base64.decodeBase64(rt.getValue().getBytes()).length);

        String[] cookieData = new String[]{
                user.getId().toString(),
                token.getSeries(),
                token.getValue()
        };

        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        // now we should be able to use the token to getUserForLogin.
        rememberMeService.processAutoLoginCookie(cookieData, request, response);

        EasyMock.reset(tokenRepository);
        expect(tokenRepository.getToken(series, user)).andReturn(token).anyTimes();
        EasyMock.replay(tokenRepository);

        token = capturedToken.getValue();

        assertNotNull("The cookieToken should have been set!", token);
        // make sure we've stored the token

        rt = tokenRepository.getToken(token.getSeries(), user);
        assertNotNull("The repository should have a token", rt);
        assertEquals("The token should be the same as the one we set as a cookie!", token, rt);

        assertEquals("The series lenght is not right!", rememberMeService.getSeriesLength(), Base64.decodeBase64(rt.getSeries().getBytes()).length);
        assertEquals("The token lenght is not right!", rememberMeService.getTokenLength(), Base64.decodeBase64(rt.getValue().getBytes()).length);

        EasyMock.verify(tokenRepository);
    }

    @Test(expected = InvalidCookieException.class)
    public void testParameters2() throws AccountsException {
        // should only accept 3 cookie parameters
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        rememberMeService.processAutoLoginCookie(new String[2], request, response);
    }

    @Test(expected = InvalidCookieException.class)
    public void testParameters1() throws AccountsException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // should only accept 3 cookie parameters
        rememberMeService.processAutoLoginCookie(new String[1], request, response);
    }

    @Test(expected = InvalidCookieException.class)
    public void testParameters4() throws AccountsException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // should only accept 3 cookie parameters
        rememberMeService.processAutoLoginCookie(new String[4], request, response);
    }

    @Test(expected = InvalidCookieException.class)
    public void testParametersRnd() throws AccountsException {
        // should only accept 3 cookie parameters
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        int i;
        while ((i = Math.abs(random.nextInt(20))) == 3) ;
        rememberMeService.processAutoLoginCookie(new String[i], request, response);
    }

    @Test(expected = RememberMeAuthenticationException.class)
    public void testInvalidUser() throws AccountsException {
        testSuccess();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();


        String id = "test";
        EasyMock.reset(accountService);
        expect(accountService.getUser(id)).andReturn(null);

        rememberMeService.processAutoLoginCookie(new String[]{id, "", ""}, request, response);
    }

    @Test(expected = RememberMeAuthenticationException.class)
    public void testNoToken() throws AccountsException {
        testSuccess();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        String series = RandomStringUtils.randomAlphabetic(20);
        EasyMock.reset(tokenRepository);
        expect(tokenRepository.getToken(series, user)).andReturn(null);
        EasyMock.replay(tokenRepository);

        rememberMeService.processAutoLoginCookie(
                new String[]{
                        user.getId().toString(),
                        series,
                        null
                }, request, response
        );

        EasyMock.verify(tokenRepository);
    }

    @Test(expected = CookieTheftException.class)
    public void testWrongValue() throws AccountsException {
        testSuccess();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        RememberMeToken token = capturedToken.getValue();
        String series = capturedSeries.getValue();

        EasyMock.reset(tokenRepository);
        expect(tokenRepository.getToken(series, user)).andReturn(token);
        tokenRepository.removeUserTokens(user);
        EasyMock.replay(tokenRepository);

        rememberMeService.processAutoLoginCookie(
                new String[]{
                        user.getId().toString(),
                        token.getSeries(),
                        token.getValue() + "error"
                }, request, response
        );
    }

    @Test(expected = RememberMeAuthenticationException.class)
    public void testExpired() throws AccountsException {
        testSuccess();
        RememberMeToken token = capturedToken.getValue();

        Date date = new Date(System.currentTimeMillis() - ((((long) rememberMeService.getTokenValiditySeconds()) + ((long) 86400)) * ((long) 2000)));

        if (log.isInfoEnabled())
            log.info("Setting token to a date in the past beyond: " + (new Date(System.currentTimeMillis() - (rememberMeService.getTokenValiditySeconds() * 1000))) + " new token date: " + date);

        token.setDate(date);

        EasyMock.reset(tokenRepository);
        tokenRepository.update(token);
        expect(tokenRepository.getToken(token.getSeries(), user)).andReturn(token);
        EasyMock.replay(tokenRepository);

        tokenRepository.update(token);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        rememberMeService.processAutoLoginCookie(
                new String[]{
                        user.getId().toString(),
                        token.getSeries(),
                        token.getValue()
                }, request, response
        );
    }

}
