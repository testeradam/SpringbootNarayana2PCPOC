package com.tstr.poc.config;

import javax.jms.ConnectionFactory;
import javax.transaction.TransactionManager;

import org.apache.activemq.ActiveMQXAConnectionFactory;
import org.apache.activemq.pool.XaPooledConnectionFactory;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.component.jms.JmsConfiguration;
import org.apache.camel.spring.spi.SpringTransactionPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jms.XAConnectionFactoryWrapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
@EnableJms
@EnableTransactionManagement
public class MessageBrokerConfiguration {

	@Autowired
	private JtaTransactionManager jtaTransactionManager;
	
	@Autowired
	private TransactionManager transactionManager;
	
	@Autowired
	private XAConnectionFactoryWrapper xaConnectionFactoryWrapper;

	@Bean(name = "transactionPolicy")
	public SpringTransactionPolicy springTransactionPolicy() throws Throwable {
		TransactionTemplate txTemplate = new TransactionTemplate(jtaTransactionManager,
				new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRED));
		return new SpringTransactionPolicy(txTemplate);
	}
	
	
	//First component configuration
	@Bean(name="BrokerConsumerMsgComponent")
	public JmsComponent getMessageConsumerComponent() throws Exception {
		JmsComponent jmsComponent = new JmsComponent(receiverMsgConfig());
		jmsComponent.setMaxConcurrentConsumers(1);
		jmsComponent.setConcurrentConsumers(1);
		jmsComponent.setIdleTaskExecutionLimit(1);
		jmsComponent.setMaxMessagesPerTask(1);
		return jmsComponent;
	}
	
	@Bean
	public JmsConfiguration receiverMsgConfig() throws Exception {
		JmsConfiguration receiverMsgConfig = new JmsConfiguration();
		receiverMsgConfig.setConnectionFactory(consumerXAPooledConnectionFactory());
		receiverMsgConfig.setMaxConcurrentConsumers(1);
		receiverMsgConfig.setTransacted(false);
		receiverMsgConfig.setTransactionManager(jtaTransactionManager);
		receiverMsgConfig.setCacheLevelName("CACHE_NONE");
		return receiverMsgConfig;		
	}
	
	@Bean(name="consumerXAPooledConnectionFactory")
	public XaPooledConnectionFactory consumerXAPooledConnectionFactory() throws Exception {
		XaPooledConnectionFactory consumerXAPooledConnectionFactory = new XaPooledConnectionFactory();
		consumerXAPooledConnectionFactory.setConnectionFactory(consumerConnectionFactory());
		consumerXAPooledConnectionFactory.setTransactionManager(transactionManager);
		consumerXAPooledConnectionFactory.setMaxConnections(30);
		consumerXAPooledConnectionFactory.setIdleTimeout(7200000);
		return consumerXAPooledConnectionFactory;
	}
	
	@Bean(name="consumerConnectionFactory")
	@Primary
	public ConnectionFactory consumerConnectionFactory() throws Exception {
		String brokerUrl = "failover:(tcp://localhost:61616)?jms.prefetchPolicy.queuePrefetch=1&timeout=5000";
		ActiveMQXAConnectionFactory consumerXAConnectionFactory = new ActiveMQXAConnectionFactory(brokerUrl);
		this.xaConnectionFactoryWrapper.wrapConnectionFactory(consumerXAConnectionFactory);
		return consumerXAConnectionFactory;
	}
	
	//Second Component Configuration
	@Bean(name="BrokerProducerMsgComponent")
	public JmsComponent getMessageProducerComponent() throws Exception {
		JmsComponent jmsComponent = new JmsComponent(producerMsgConfig());
		jmsComponent.setMaxConcurrentConsumers(1);
		jmsComponent.setConcurrentConsumers(1);
		jmsComponent.setIdleTaskExecutionLimit(1);
		jmsComponent.setMaxMessagesPerTask(1);
		return jmsComponent;
	}
	
	@Bean
	public JmsConfiguration producerMsgConfig() throws Exception {
		JmsConfiguration producerMsgConfig = new JmsConfiguration();
		producerMsgConfig.setConnectionFactory(producerXAPooledConnectionFactory());
		producerMsgConfig.setMaxConcurrentConsumers(1);
		producerMsgConfig.setTransacted(false);
		producerMsgConfig.setTransactionManager(jtaTransactionManager);
		producerMsgConfig.setCacheLevelName("CACHE_NONE");
		return producerMsgConfig;		
	}
	
	@Bean(name="producerXAPooledConnectionFactory")
	public XaPooledConnectionFactory producerXAPooledConnectionFactory() throws Exception {
		XaPooledConnectionFactory producerXAPooledConnectionFactory = new XaPooledConnectionFactory();
		producerXAPooledConnectionFactory.setConnectionFactory(producerConnectionFactory());
		producerXAPooledConnectionFactory.setTransactionManager(transactionManager);
		producerXAPooledConnectionFactory.setMaxConnections(30);
		producerXAPooledConnectionFactory.setIdleTimeout(7200000);
		return producerXAPooledConnectionFactory;
	}
	
	@Bean(name="producerConnectionFactory")
	public ConnectionFactory producerConnectionFactory() throws Exception {
		String brokerUrl = "failover:(tcp://localhost:61616)?jms.prefetchPolicy.queuePrefetch=1&timeout=5000";
		ActiveMQXAConnectionFactory producerXAConnectionFactory = new ActiveMQXAConnectionFactory(brokerUrl);
		this.xaConnectionFactoryWrapper.wrapConnectionFactory(producerXAConnectionFactory);
		return producerXAConnectionFactory;
	}
}
