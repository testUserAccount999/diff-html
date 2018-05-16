package org.sample;

import java.io.InputStream;

import org.junit.Test;

public class HtmlTest {
    @Test
    public void executeTest() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("./test.txt");
    }
}
