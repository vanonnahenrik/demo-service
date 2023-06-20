package de.dsa.prodis.service.registry.config;

import javax.sql.DataSource;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Load specially configured datasource pool
 */
@Configuration
@EnableTransactionManagement
public class DefaultDsConfig {

    @Primary
    @Bean(name = "defaultDs")
    @ConfigurationProperties(prefix="spring.datasource.tomcat")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

}
