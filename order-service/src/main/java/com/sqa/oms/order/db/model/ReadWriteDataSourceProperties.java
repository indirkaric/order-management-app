package com.sqa.oms.order.db.model;

import com.sqa.oms.order.util.Check;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Object to store the properties value of the read-write DataSource.
 */
@ConfigurationProperties(prefix = "database.readwrite")
@Getter
public final class ReadWriteDataSourceProperties {

    private final String url;

    private final char[] password;

    private final String username;

    /**
     * Constructor in which the parameters are passed directly from the .yaml file.
     *
     * @param url      jdbc url of the read-write datasource
     * @param password password of the read-write datasource
     * @param username username of the read-write datasource
     */
    public ReadWriteDataSourceProperties(final String url, final char[] password, final String username) {
        Check.isNotEmpty(url, "ReadWriteDataSourceProperties.url");
        Check.isNotEmpty(password, "ReadWriteDataSourceProperties.password");
        Check.isNotEmpty(username, "ReadWriteDataSourceProperties.username");

        this.url = url;
        this.password = password;
        this.username = username;
    }
}
