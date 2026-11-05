package com.sqa.oms.order.db.config;

import com.sqa.oms.order.db.model.ReadOnlyDataSourceProperties;
import com.sqa.oms.order.db.model.ReadWriteDataSourceProperties;
import com.sqa.oms.order.util.Check;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Objects;

/**
 * Wires the read-only/read-write DataSource pair and routes @Transactional calls between them
 * based on whether the current transaction is read-only.
 */
@Configuration
@EnableConfigurationProperties({ReadOnlyDataSourceProperties.class, ReadWriteDataSourceProperties.class})
@EnableJpaRepositories(
        basePackages = "com.sqa.oms.order",
        transactionManagerRef = "transactionManager",
        entityManagerFactoryRef = "entityManager")
@EnableTransactionManagement
public class DataSourceConfigService {

    private final ReadOnlyDataSourceProperties readOnlyDataSourceProperties;

    private final ReadWriteDataSourceProperties readWriteDataSourceProperties;

    /**
     * Constructor with dependency injection.
     *
     * @param readOnlyDataSourceProperties  properties of the read-only datasource
     * @param readWriteDataSourceProperties properties of the read-write datasource
     */
    public DataSourceConfigService(final ReadOnlyDataSourceProperties readOnlyDataSourceProperties,
                                   final ReadWriteDataSourceProperties readWriteDataSourceProperties) {
        Check.isNotNull(readOnlyDataSourceProperties, "readOnlyDataSourceProperties");
        Check.isNotNull(readWriteDataSourceProperties, "readWriteDataSourceProperties");

        this.readOnlyDataSourceProperties = readOnlyDataSourceProperties;
        this.readWriteDataSourceProperties = readWriteDataSourceProperties;
    }

    /**
     * Creates the Spring bean of DataSources used by the application.
     *
     * @return the routing DataSource
     */
    @Bean
    @Primary
    public DataSource dataSource() {
        DataSourceRouting routingDataSource = new DataSourceRouting();
        routingDataSource.initDataSource(readOnlyDataSource(), readWriteDataSource());

        return routingDataSource;
    }

    /**
     * Creates the read-write DataSource used by the application.
     *
     * @return DataSource
     */
    private DataSource readWriteDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(readWriteDataSourceProperties.getUrl());
        dataSource.setUsername(readWriteDataSourceProperties.getUsername());
        dataSource.setPassword(new String(readWriteDataSourceProperties.getPassword()));

        return dataSource;
    }

    /**
     * Creates the read-only DataSource used by the application.
     *
     * @return DataSource
     */
    private DataSource readOnlyDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(readOnlyDataSourceProperties.getUrl());
        dataSource.setUsername(readOnlyDataSourceProperties.getUsername());
        dataSource.setPassword(new String(readOnlyDataSourceProperties.getPassword()));

        return dataSource;
    }

    /**
     * Creates the Spring bean LocalContainerEntityManagerFactoryBean with the
     * packages containing the @Entity objects.
     *
     * @param builder the entity manager factory builder, pre-configured from spring.jpa.*
     * @return LocalContainerEntityManagerFactoryBean
     */
    @Bean(name = "entityManager")
    public LocalContainerEntityManagerFactoryBean entityManagerFactoryBean(
            final EntityManagerFactoryBuilder builder) {
        return builder.dataSource(dataSource())
                .packages("com.sqa.oms.order.entity")
                .jta(false)
                .persistenceUnit("PSQL").build();
    }

    /**
     * Creates the Spring bean JpaTransactionManager.
     *
     * @param entityManagerFactoryBean the entity manager factory bean
     * @return JpaTransactionManager
     */
    @Bean(name = "transactionManager")
    public JpaTransactionManager transactionManager(
            @Autowired
            @Qualifier("entityManager") final LocalContainerEntityManagerFactoryBean entityManagerFactoryBean) {
        return new JpaTransactionManager(Objects.requireNonNull(entityManagerFactoryBean.getObject()));
    }
}
