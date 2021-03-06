package com.epam.esm.configuration;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Class {@code PersistenceConfiguration} contains spring configuration for persistence subproject.
 *
 * @author Oleg Borikov
 * @version 1.0
 */
@Configuration
@Profile("prod")
@PropertySource("classpath:property/database.properties")
@ComponentScan("com.epam.esm")
public class PersistenceConfiguration {

    @Value("${database.driverClassName}")
    private String driverClassName;
    @Value("${database.url}")
    private String url;
    @Value("${database.username}")
    private String username;
    @Value("${database.password}")
    private String password;

    /**
     * Create bean {@link HikariDataSource} which will be used as data source.
     *
     * @return the hikari data source
     */
    @Bean
    public HikariDataSource hikariDataSource() {
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setDriverClassName(driverClassName);
        hikariDataSource.setJdbcUrl(url);
        hikariDataSource.setUsername(username);
        hikariDataSource.setPassword(password);
        return hikariDataSource;
    }

    /**
     * Create bean {@link JdbcTemplate} which will be used for queries to database.
     *
     * @param hikariDataSource the hikari data source
     * @return the jdbc template
     */
    @Bean
    public JdbcTemplate jdbcTemplate(HikariDataSource hikariDataSource) {
        return new JdbcTemplate(hikariDataSource);
    }
}
