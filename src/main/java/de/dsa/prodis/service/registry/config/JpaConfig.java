package de.dsa.prodis.service.registry.config;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.eclipse.persistence.config.BatchWriting;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.logging.SessionLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.orm.jpa.JpaBaseConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.persistenceunit.PersistenceManagedTypes;
import org.springframework.orm.jpa.vendor.AbstractJpaVendorAdapter;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.jta.JtaTransactionManager;

import de.dsa.prodis.service.registry.util.CustomIdSequence;
import jakarta.persistence.EntityManagerFactory;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories("de.dsa.prodis.service.registry.model.repository")
@Primary
public class JpaConfig extends JpaBaseConfiguration {

    
    protected JpaConfig(DataSource dataSource, JpaProperties properties, 
        ObjectProvider<JtaTransactionManager> jtaTransactionManager) {
        super(dataSource, properties, jtaTransactionManager);
    }

    @Override
    @Primary
    @Bean(name = "entityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder builder, PersistenceManagedTypes persistenceManagedTypes) {
        return builder.dataSource(this.getDataSource())
        .managedTypes(persistenceManagedTypes)
        .packages("de.dsa.prodis.service.registry.model.jpa")
        .persistenceUnit("registry-persistence")
        .properties(initJpaProperties()).jta(isJta())
        .build();
    }

    @Primary
    @Bean(name = "transactionManager")
    public PlatformTransactionManager transactionManagerPlanthub(
        @Qualifier("entityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    private final Map<String, ?> initJpaProperties() {
        final Map<String, Object> ret = new HashMap<>();
        // Add any JpaProperty you are interested in and is supported by your Database
        // -> here we should only refer to javax properties from the JPA standard

        /*
         * When configuring the entity manager factory by ourselves, Spring does not
         * automatically merge the vendor properties, so we have to do it here
         * explicitly.
         */
        ret.putAll(getVendorProperties());
        return ret;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.boot.autoconfigure.orm.jpa.JpaBaseConfiguration#
     * createJpaVendorAdapter()
     */
    @Override
    protected AbstractJpaVendorAdapter createJpaVendorAdapter() {
        return new EclipseLinkJpaVendorAdapter();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.boot.autoconfigure.orm.jpa.JpaBaseConfiguration#
     * getVendorProperties()
     *
     * Hier sollten implementations-spezifische, also keine JPA-API Properties
     * gesetzt werden.
     */
    @Override
    protected Map<String, Object> getVendorProperties() {
        Map<String, Object> properties = new HashMap<>();

        properties.put(PersistenceUnitProperties.BATCH_WRITING, BatchWriting.JDBC);
        // Set log level
        properties.put(PersistenceUnitProperties.LOGGING_LEVEL, mapLogLevel());

        // Turn off dynamic weaving to disable LTW lookup in static weaving mode
        properties.put(PersistenceUnitProperties.WEAVING, "false");
        // Turn off the cache
        properties.put(PersistenceUnitProperties.CACHE_SHARED_DEFAULT, "false");

        properties.put(PersistenceUnitProperties.SESSION_CUSTOMIZER, CustomIdSequence.class.getName());
        // Set customize logger
        properties.put(PersistenceUnitProperties.LOGGING_LOGGER, JpaCustomSessionLog.class.getName());

        // Turn off id validation
        //    	properties.put(PersistenceUnitProperties.ID_VALIDATION, IdValidation.NONE.toString());
        return properties;
    }

    private String mapLogLevel() {
        Logger logger = LoggerFactory.getLogger("org.eclipse.persistence");
        if (logger != null) {
            if (logger.isDebugEnabled()) {
                return SessionLog.ALL_LABEL;
            } else if (logger.isInfoEnabled()) {
                return SessionLog.INFO_LABEL;
            } else if (logger.isWarnEnabled()) {
                return SessionLog.WARNING_LABEL;
            } else if (logger.isErrorEnabled()) {
                return SessionLog.SEVERE_LABEL;
            }
        }

        // Previous default setting was WARN
        return SessionLog.WARNING_LABEL;

    }
}
