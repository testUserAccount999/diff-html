package org.sample;

import org.junit.Ignore;
import org.junit.Test;

public class DiffMainTest {
    @Ignore
    @Test
    public void executeTest() throws Exception {
        String[] args = { ".\\src\\test\\resources\\old", ".\\src\\test\\resources\\new" };
        DiffMain.main(args);
    }
}
