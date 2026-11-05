package com.sqa.oms.order.db.model;

import com.sqa.oms.order.util.Check;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Object to store the properties value of the read-only DataSource.
 */
@ConfigurationProperties(prefix = "database.readonly")
@Getter
public final class ReadOnlyDataSourceProperties {

    private final String url;

    private final char[] password;

    private final String username;

    /**
     * Constructor in which the parameters are passed directly from the .yaml file.
     *
     * @param url      jdbc url of the read-only datasource
     * @param password password of the read-only datasource
     * @param username username of the read-only datasource
     */
    public ReadOnlyDataSourceProperties(final String url, final char[] password, final String username) {
        Check.isNotEmpty(url, "ReadOnlyDataSourceProperties.url");
        Check.isNotEmpty(password, "ReadOnlyDataSourceProperties.password");
        Check.isNotEmpty(username, "ReadOnlyDataSourceProperties.username");

        this.url = url;
        this.password = password;
        this.username = username;
    }
}
