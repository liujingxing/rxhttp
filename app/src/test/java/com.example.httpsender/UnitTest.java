package com.example.httpsender;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * User: ljx
 * Date: 2020/6/20
 * Time: 09:14
 */
public class UnitTest {

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void isEmpty() {
        Assert.assertEquals(true, "".isEmpty());
    }
}
