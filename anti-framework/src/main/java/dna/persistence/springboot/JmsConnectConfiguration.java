package dna.persistence.springboot;

import com.atomikos.icatch.config.UserTransactionService;
import com.atomikos.icatch.jta.J2eeTransactionManager;
import com.atomikos.jms.AtomikosConnectionFactoryBean;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQPrefetchPolicy;
import org.apache.activemq.ActiveMQXAConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import javax.jms.ConnectionFactory;


@ConditionalOnProperty(name = "spring.activemq.enable", havingValue = "true")
@EnableJms
@Configuration
public class JmsConnectConfiguration {

    @Primary
    @Bean(initMethod = "checkSetup")
    @ConditionalOnProperty(value = "spring.jta.enabled", havingValue = "true", matchIfMissing = true)
    public J2eeTransactionManager j2eeTransactionManager(UserTransactionService userTransactionService) throws Exception {
        J2eeTransactionManager manager = new J2eeTransactionManager();
        return manager;
    }

    @Bean
    public JmsTemplate jmsTemplate(ConnectionFactory jmsConnectionFactory) {
        RedeliveryPolicy redeliveryPolicy = new RedeliveryPolicy();
        redeliveryPolicy.setMaximumRedeliveries(2);//最多重试2次
        ActiveMQPrefetchPolicy prefetchPolicy = new ActiveMQPrefetchPolicy();
        prefetchPolicy.setAll(1);//预取数量1
        if (jmsConnectionFactory instanceof AtomikosConnectionFactoryBean) {
            AtomikosConnectionFactoryBean connectionFactoryBean = (AtomikosConnectionFactoryBean) jmsConnectionFactory;
            connectionFactoryBean.setMinPoolSize(5);
            connectionFactoryBean.setMaxPoolSize(50);
            ActiveMQXAConnectionFactory xaConnectionFactory = (ActiveMQXAConnectionFactory) connectionFactoryBean.getXaConnectionFactory();
            xaConnectionFactory.setRedeliveryPolicy(redeliveryPolicy);
            xaConnectionFactory.setPrefetchPolicy(prefetchPolicy);
        } else {
            ActiveMQConnectionFactory mqConnectionFactory = (ActiveMQConnectionFactory) jmsConnectionFactory;
            mqConnectionFactory.setRedeliveryPolicy(redeliveryPolicy);
            mqConnectionFactory.setPrefetchPolicy(prefetchPolicy);
        }
        JmsTemplate jmsTemplate = new JmsTemplate(jmsConnectionFactory);
        return jmsTemplate;
    }

    @Bean
    public JmsMessagingTemplate jmsMessagingTemplate(JmsTemplate jmsTemplate) {
        return new JmsMessagingTemplate(jmsTemplate);
    }

    // topic模式的ListenerContainer
    @Bean
    public JmsListenerContainerFactory<?> jmsListenerContainerTopic(ConnectionFactory connectionFactory, PlatformTransactionManager transactionManager) {
        DefaultJmsListenerContainerFactory bean = new DefaultJmsListenerContainerFactory();
        bean.setPubSubDomain(true);
        bean.setConnectionFactory(connectionFactory);
        bean.setTransactionManager(transactionManager);
        bean.setSessionTransacted(true);
        return bean;
    }

    // queue模式的ListenerContainer
    @Bean
    public JmsListenerContainerFactory<?> jmsListenerContainerQueue(ConnectionFactory connectionFactory, PlatformTransactionManager transactionManager) {
        DefaultJmsListenerContainerFactory bean = new DefaultJmsListenerContainerFactory();
        bean.setConnectionFactory(connectionFactory);
        bean.setTransactionManager(transactionManager);
        bean.setSessionTransacted(true);
        return bean;
    }
}
