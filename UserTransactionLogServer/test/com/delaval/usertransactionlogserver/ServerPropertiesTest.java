package com.delaval.usertransactionlogserver;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by delaval on 12/9/2015.
 */
public class ServerPropertiesTest {

    @Test
    public void testGetProp() throws Exception {
        Assert.assertEquals("8085", ServerProperties.getInstance().getProp(ServerProperties.PropKey.WEBSOCKET_PORT));

    }

    @Test
    public void testGetNewProp() throws Exception {
        ServerProperties instance = ServerProperties.getInstance();
        String expectedProp = "dummy";
        instance.set(ServerProperties.PropKey.JMS_CONNECTION, expectedProp);
        Assert.assertEquals(expectedProp, ServerProperties.getInstance().getProp(ServerProperties.PropKey.JMS_CONNECTION));

    }

    @Test
    public void testGetAllPropsShallReturnRightAmountOfProps(){
        for(ServerProperties.PropKey aKey : ServerProperties.PropKey.values()){
            String prop = ServerProperties.getInstance().getProp(aKey);
            Assert.assertNotNull("property was null for key:" + aKey, prop);
        }
    }
}