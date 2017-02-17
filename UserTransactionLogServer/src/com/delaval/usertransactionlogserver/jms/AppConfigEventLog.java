package com.delaval.usertransactionlogserver.jms;

import com.delaval.usertransactionlogserver.ServerProperties;
import com.delaval.usertransactionlogserver.jms.consumer.EventLogListener;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.SimpleMessageListenerContainer;
import org.springframework.jms.listener.adapter.MessageListenerAdapter;

import javax.jms.ConnectionFactory;

/**
 * Sets up the configuration for Spring-jms.
 * Configures which bean shall listen to the queue, the name of the queue and connectionfactory for ActiveMQ.
 */
@Configuration
public class AppConfigEventLog {

    @Bean
    ConnectionFactory connectionFactory() {
        PooledConnectionFactory pooledConnectionFactory = new PooledConnectionFactory(
                new ActiveMQConnectionFactory(ServerProperties.getInstance().getProp(ServerProperties.PropKey.JMS_CONNECTION)));
        pooledConnectionFactory.setReconnectOnException(true);
        return pooledConnectionFactory;
    }

    @Bean
    MessageListenerAdapter receiver() {
        return new MessageListenerAdapter(new EventLogListener()) {{
            setDefaultListenerMethod(EventLogListener.NAME_OF_PROCESS_METHOD);
        }};
    }

    @Bean
    JmsTemplate jmsTemplate(ConnectionFactory connectionFactory) {
        return new JmsTemplate(connectionFactory);
    }

    @Bean
    SimpleMessageListenerContainer container(final MessageListenerAdapter messageListener,
                                             final ConnectionFactory connectionFactory) {
        return new SimpleMessageListenerContainer() {{
            setMessageListener(messageListener);
            setConnectionFactory(connectionFactory);
            setDestinationName(ServerProperties.getInstance().getProp(ServerProperties.PropKey.JMS_QUEUE_DEST_EVENT));

        }};
    }


}
