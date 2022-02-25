package com.tstr.poc.config;

import java.util.HashMap;

import javax.sql.DataSource;
import javax.sql.XADataSource;
import javax.transaction.TransactionManager;

import org.apache.tomcat.dbcp.dbcp2.PoolableConnection;
import org.apache.tomcat.dbcp.dbcp2.PoolableConnectionFactory;
import org.apache.tomcat.dbcp.dbcp2.managed.DataSourceXAConnectionFactory;
import org.apache.tomcat.dbcp.dbcp2.managed.ManagedDataSource;
import org.apache.tomcat.dbcp.pool2.impl.GenericObjectPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.XADataSourceWrapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "com.tstr.poc.repository", entityManagerFactoryRef = "myEntityManager")
@EnableTransactionManagement
@ComponentScan(basePackages = "me.snowdrop.*")
public class DatabaseConfiguration {

	@Autowired
	private TransactionManager transactionManager;

	@Autowired
	private XADataSourceWrapper xaDataSourceWrapper;

	@Bean
	@Primary
	@ConfigurationProperties("app.datasource.primary")
	public DataSourceProperties myDataSourceProperties() throws Exception {
		return new DataSourceProperties();
	}

	@Bean
	@Primary
	@ConfigurationProperties("app.datasource.primary.configuration")
	public DataSource myDataSource() throws Exception {

		DataSource dataSource = myDataSourceProperties().initializeDataSourceBuilder().build();
		this.xaDataSourceWrapper.wrapDataSource((XADataSource) dataSource);
		DataSourceXAConnectionFactory dataSourceXAConnectionFactory = new DataSourceXAConnectionFactory(
				transactionManager, (XADataSource) dataSource);
		PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(
				dataSourceXAConnectionFactory, null);
		poolableConnectionFactory.setValidationQuery("select 1 from dual");
		GenericObjectPool<PoolableConnection> connectionPool = new GenericObjectPool<>(poolableConnectionFactory);
		connectionPool.setMaxTotal(10);
		poolableConnectionFactory.setPool(connectionPool);
		return new ManagedDataSource<>(connectionPool, dataSourceXAConnectionFactory.getTransactionRegistry());
	}

	@Bean(name = "myEntityManager")
	@Primary
	public LocalContainerEntityManagerFactoryBean myEntityManager() throws Throwable {

		HashMap<String, Object> properties = new HashMap<>();
		properties.put("javax.persistence.transactionType", "JTA");
		properties.put("hibernate.transaction.jta.platform",
				"org.hibernate.service.jta.platform.internal.JBossStandAloneJtaPlatform");
		properties.put("hibernate.connection.release_mode", "after_statement");
		properties.put("hibernate.connection.isolation", "2");
		properties.put("hibernate.dialect", "org.hibernate.dialect.Oracle10gDialect");
		properties.put(null, properties);

		LocalContainerEntityManagerFactoryBean entiryManager = new LocalContainerEntityManagerFactoryBean();
		entiryManager.setJtaDataSource(this.myDataSource());
		entiryManager.setJpaVendorAdapter(jpavendorAdapter());
		entiryManager.setPackagesToScan("com.tstr.poc.model");
		entiryManager.setPersistenceUnitName("myPersistenceUnit");
		entiryManager.setJpaPropertyMap(properties);
		return entiryManager;
	}

	@Bean
	JdbcTemplate jdbcTemplate(DataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}

	@Bean
	public JpaVendorAdapter jpavendorAdapter() {
		HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		vendorAdapter.setShowSql(false);
		vendorAdapter.setGenerateDdl(false);
		vendorAdapter.setDatabase(Database.ORACLE);
		return vendorAdapter;
	}

}
