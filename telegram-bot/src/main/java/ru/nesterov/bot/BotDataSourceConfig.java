package ru.nesterov.bot;

import jakarta.activation.DataSource;
import jakarta.persistence.EntityManagerFactory;
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

@Configuration
@EnableJpaRepositories(entityManagerFactoryRef = "botEntityManagerFactory", transactionManagerRef = "botTransactionManager")
public class BotDataSourceConfig {

    @Bean(name = "botEntityManagerFactory")
    @ConfigurationProperties("spring.datasource.bot")
    public LocalContainerEntityManagerFactoryBean botEntityManagerFactory(EntityManagerFactoryBuilder builder) {
        DataSourceBuilder.create().url(@Value("{spring.datasource.bot.url}"))
        return builder
                .dataSource()
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
}
