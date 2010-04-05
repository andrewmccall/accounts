package com.andrewmccall.accounts.hibernate.oauth;

import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/com/andrewmccall/accounts/accounts-hibernate-test-config.xml"})
@Transactional(readOnly=false)
public class AccessTokenStoreTest extends com.andrewmccall.accounts.core.oauth.AccessTokenStoreTest {
}
