package pl.aticode.civilworkoffers.config;

import java.net.URISyntaxException;
import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class HibernateConfig {
	
	@Value(value = "${enable.test}")
	private boolean enableTest;
	
	@Autowired
	private DataSource dataSource;
	
	@Bean
	public LocalSessionFactoryBean sessionFactory() throws URISyntaxException {
		LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
		sessionFactory.setDataSource(dataSource);
		sessionFactory.setPackagesToScan("pl.aticode.civilworkoffers");		
        Properties hibernateProperties = new Properties();
		if(!enableTest) {
			hibernateProperties.setProperty("hibernate.show_sql", "false");
			hibernateProperties.setProperty("hibernate.format_sql", "false");
			hibernateProperties.setProperty("hibernate.hbm2ddl.auto", "validate");
		} else {
			hibernateProperties.setProperty("hibernate.show_sql", "true");
			hibernateProperties.setProperty("hibernate.format_sql", "false");
			hibernateProperties.setProperty("hibernate.hbm2ddl.auto", "create");
		}	
		sessionFactory.setHibernateProperties(hibernateProperties);
		
		return sessionFactory;
	}
	
    @Bean
    public PlatformTransactionManager hibernateTransactionManager() throws URISyntaxException {
        HibernateTransactionManager transactionManager = new HibernateTransactionManager();
        transactionManager.setSessionFactory(sessionFactory().getObject());
        return transactionManager;
    }
}
