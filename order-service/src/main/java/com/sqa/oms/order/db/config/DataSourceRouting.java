package com.sqa.oms.order.db.config;

import com.sqa.oms.order.db.constants.DataSourceType;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Routes each call to either the read-only or the read-write DataSource, based on whether the
 * currently running transaction is read-only.
 */
public class DataSourceRouting extends AbstractRoutingDataSource {

    /**
     * Sets the key of the DataSource to be used, based on
     * if the current transaction is read-only or not.
     */
    @Override
    protected Object determineCurrentLookupKey() {
        DataSourceType dataSourceToUse = DataSourceType.READONLY;

        if (TransactionDefinitionInterceptor.isCurrentTransactionReadWrite()) {
            dataSourceToUse = DataSourceType.READWRITE;
        }

        return dataSourceToUse;
    }

    /**
     * Initialization of the map with all the possible DataSources.
     *
     * @param readOnlyDataSource  the read-only DataSource
     * @param readWriteDataSource the read-write DataSource
     */
    public void initDataSource(final DataSource readOnlyDataSource, final DataSource readWriteDataSource) {
        Map<Object, Object> dataSourceMap = new HashMap<>();
        dataSourceMap.put(DataSourceType.READONLY, readOnlyDataSource);
        dataSourceMap.put(DataSourceType.READWRITE, readWriteDataSource);
        this.setTargetDataSources(dataSourceMap);
        this.setDefaultTargetDataSource(readOnlyDataSource);
    }
}
