package ru.nesterov.repository;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(entityManagerFactoryRef = "clientAnalyzerEntityManagerFactory",
        transactionManagerRef = "clientAnalyzerTransactionManager", basePackages = "ru.nesterov.repository")
public class ClientAnalyzerDataSourceConfig {
    @Bean(name = "clientAnalyzerEntityManagerFactory")
    @Primary
    @ConfigurationProperties("spring.datasource.client-analyzer")
    public LocalContainerEntityManagerFactoryBean clientAnalyzerEntityManagerFactory(EntityManagerFactoryBuilder builder, @Value("${spring.datasource.client-analyzer.url}") String url,
            @Value("${spring.datasource.client-analyzer.username}") String username, @Value("${spring.datasource.client-analyzer.password}") String password, @Value("${spring.datasource.client-analyzer.driver-class-name}") String driverClassName) {

        DataSource dataSource = DataSourceBuilder.create()
                .url(url)
                .username(username)
                .password(password)
                .driverClassName(driverClassName)
                .build();
        return builder
                .dataSource(dataSource)
                .packages("ru.nesterov.entity")
                .persistenceUnit("client-analyzer")
                .build();
    }

    @Bean(name = "clientAnalyzerTransactionManager")
    public PlatformTransactionManager clientAnalyzerTransationManager(@Qualifier("clientAnalyzerEntityManagerFactory") EntityManagerFactory clientAnalyzerEntityManagerFactory) {
        return new JpaTransactionManager(clientAnalyzerEntityManagerFactory);
    }
}
