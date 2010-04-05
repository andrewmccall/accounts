package com.andrewmccall.accounts.hibernate;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.junit.runner.RunWith;

import java.util.Random;

/**
 * Just runs the basic AccountService tests on a the JPA implementation to make sure it's working properly.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/com/andrewmccall/accounts/accounts-hibernate-test-config.xml"})
public class AccountServiceTest extends com.andrewmccall.accounts.core.AccountServiceTest<Long> {

    Random r = new Random();

    @Override
    public Long getId() {
        return r.nextLong();
    }
}
