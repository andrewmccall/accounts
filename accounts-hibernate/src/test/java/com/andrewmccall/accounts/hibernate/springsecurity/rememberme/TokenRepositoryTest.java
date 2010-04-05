package com.andrewmccall.accounts.hibernate.springsecurity.rememberme;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.junit.runner.RunWith;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/com/andrewmccall/accounts/accounts-hibernate-test-config.xml"})
public class TokenRepositoryTest extends com.andrewmccall.accounts.core.springsecurity.rememberme.TokenRepositoryTest {
}
