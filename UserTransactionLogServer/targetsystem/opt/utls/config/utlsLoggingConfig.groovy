import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.FileAppender

import static ch.qos.logback.classic.Level.DEBUG

appender("UTL_SERVER", FileAppender) {
    file = "/var/log/amsserver/utls.log"
    append = true
    encoder(PatternLayoutEncoder) {
        pattern = "%d{yyyy-MM-dd HH:mm:ss.SSS Z} | %level | %msg%n"
    }
}
logger("utlserver",INFO,["UTL_SERVER"])
//logger("utlserver", DEBUG, ["UTL_SERVER"])