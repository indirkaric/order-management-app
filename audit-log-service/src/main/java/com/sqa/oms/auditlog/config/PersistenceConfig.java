package com.sqa.oms.auditlog.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

/**
 * Enables Elasticsearch repository scanning for the audit log store.
 */
@Configuration
@EnableElasticsearchRepositories(basePackages = "com.sqa.oms.auditlog.repository.elasticsearch")
public class PersistenceConfig {
}
