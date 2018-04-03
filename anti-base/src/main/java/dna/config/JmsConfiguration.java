package dna.config;

import dna.origins.commons.JmsConstants;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.jms.Queue;
import javax.jms.Topic;


//@EnableJms
@Configuration
public class JmsConfiguration {
    public final static String TOPIC = "topic.base";

    @Bean
    public Queue securityQueryRecordQueue() {
        return new ActiveMQQueue(JmsConstants.QUEUE_RECORD_SECURITY_QUERY);
    }

    @Bean
    public Queue billUploadRecordQueue() {
        return new ActiveMQQueue(JmsConstants.QUEUE_RECORD_BILL_UPLOAD);
    }

    @Bean
    public Queue redPackRecordQueue() {
        return new ActiveMQQueue(JmsConstants.QUEUE_RECORD_RED_PACK);
    }

    @Bean
    public Topic topic() {
        return new ActiveMQTopic(TOPIC);
    }

}
