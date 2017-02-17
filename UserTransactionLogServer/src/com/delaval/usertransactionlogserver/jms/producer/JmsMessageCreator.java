package com.delaval.usertransactionlogserver.jms.producer;

import com.delaval.usertransactionlogserver.util.UtlsLogUtil;
import org.springframework.jms.core.MessageCreator;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

/**
 * This is a datacarrier/wrapper when posting jms-message to queue
 */
public class JmsMessageCreator implements MessageCreator {

    private String message;

    public JmsMessageCreator(String message){
        this.message = message;
    }

    @Override
    public Message createMessage(Session session) throws JMSException {
        UtlsLogUtil.debug(this.getClass(), "creating message with content:" + message);
        return session.createTextMessage(message);
    }
}
