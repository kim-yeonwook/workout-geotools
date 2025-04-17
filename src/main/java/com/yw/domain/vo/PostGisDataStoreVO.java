package com.yw.domain.vo;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.geotools.data.postgis.PostgisNGDataStoreFactory;

import java.util.HashMap;
import java.util.Map;

@EqualsAndHashCode
@Builder
@Getter
public class PostGisDataStoreVO implements IDataStoreVO {

    private static final String type = "postgis";

    private String host;

    private Integer port;

    private String database;

    private String schema;

    private String user;

    private String password;

    private final boolean preparedStatements = true;

    private final boolean encodeFunctions = true;

    @Override
    public Map<String, Object> toMap() {
        return new HashMap<>() {
            {
                put(PostgisNGDataStoreFactory.DBTYPE.key, type);
                put(PostgisNGDataStoreFactory.HOST.key, host);
                put(PostgisNGDataStoreFactory.PORT.key, port);
                put(PostgisNGDataStoreFactory.DATABASE.key, database);
                put(PostgisNGDataStoreFactory.SCHEMA.key, schema);
                put(PostgisNGDataStoreFactory.USER.key, user);
                put(PostgisNGDataStoreFactory.PASSWD.key, password);
                put(PostgisNGDataStoreFactory.PREPARED_STATEMENTS.key, preparedStatements);
                put(PostgisNGDataStoreFactory.ENCODE_FUNCTIONS.key, encodeFunctions);
            }
        };
    }
}
