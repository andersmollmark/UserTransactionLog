package com.delaval.usertransactionlogserver.jms;

import com.delaval.usertransactionlogserver.util.UtlsLogUtil;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.jms.core.JmsTemplate;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Singleton.
 * Makes it possible to get jms-templates in an easy way.
 */
public class JmsResourceFactory {

    AnnotationConfigApplicationContext ctx;

    private static JmsResourceFactory _eventLogInstance;
    private static AtomicReference<Boolean> isStopped = new AtomicReference<>(false);

    private JmsResourceFactory() {
        // empty by default
    }

    public static synchronized JmsResourceFactory getEventLogInstance() {
        UtlsLogUtil.debug(JmsResourceFactory.class, "inside getEventLogInstance");
        if (_eventLogInstance == null) {
            try {
                _eventLogInstance = new JmsResourceFactory();
                _eventLogInstance.ctx = new AnnotationConfigApplicationContext(AppConfigEventLog.class);
            }
            catch(Throwable t) {
                _eventLogInstance = null;
                UtlsLogUtil.error(JmsResourceFactory.class, "getEventLogInstance caused an exception, maybe activemq is down:", t.getMessage());
            }
        }
        return _eventLogInstance;
    }

    public static void initApplicationContext() {
        getEventLogInstance();
    }

    public static synchronized void stopSendingJms(){
        if(!isStopped.get()){
            UtlsLogUtil.info(JmsResourceFactory.class, "stop sending to jms-queue");
            isStopped.set(true);
        }
    }

    public static synchronized void startSendingJms(){
        if(isStopped.get()){
            UtlsLogUtil.info(JmsResourceFactory.class, "start sending to jms-queue again");
            isStopped.set(false);
        }
    }

    public static boolean isJmsStopped(){
        return isStopped.get();
    }

    public AnnotationConfigApplicationContext getCtx() {
        return ctx;
    }

    public JmsTemplate getJmsTemplate() {
        return ctx.getBean(JmsTemplate.class);
    }

    public void closeContext() {
        ctx.close();
    }

}
