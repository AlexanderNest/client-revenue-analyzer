package ru.nesterov.bot;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(entityManagerFactoryRef = "botEntityManagerFactory", transactionManagerRef = "botTransactionManager", basePackages = "ru.nesterov.user-repository")
public class BotDataSourceConfig {
    @Bean(name = "botEntityManagerFactory")
    @ConfigurationProperties("spring.datasource.bot")
    public LocalContainerEntityManagerFactoryBean botEntityManagerFactory(EntityManagerFactoryBuilder builder, @Value(("${spring.datasource.bot.url}")) String url,
                                                                          @Value("${spring.datasource.bot.username}") String username, @Value("${spring.datasource.bot.password}") String password, @Value("${spring.datasource.bot.driver-class-name}") String driverClassName) {
        DataSource dataSource = DataSourceBuilder.create()
                .url(url)
                .driverClassName(driverClassName)
                .password(password)
                .username(username)
                .build();
        return builder
                .dataSource(dataSource)
                .packages("ru.nesterov.entity")
                .persistenceUnit("bot")
                .build();
    }

    @Bean(name = "botTransactionManager")
    public PlatformTransactionManager botTransactionManager(
            @Qualifier("botEntityManagerFactory") EntityManagerFactory botEntityManagerFactory) {
        return new JpaTransactionManager(botEntityManagerFactory);
    }
}
