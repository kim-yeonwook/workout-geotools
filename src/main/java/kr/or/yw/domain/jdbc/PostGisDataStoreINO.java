package kr.or.yw.domain.jdbc;

import org.geotools.data.postgis.PostgisNGDataStoreFactory;

import java.util.HashMap;
import java.util.Map;

public class PostGisDataStoreINO extends JdbcDataStoreINO {

    private final String type = "postgis";

    @Override
    Map<String, Object> getJdbcProperties() {
        return new HashMap<>(){{
            put(PostgisNGDataStoreFactory.DBTYPE.key, type);
            put(PostgisNGDataStoreFactory.HOST.key, getHost());
            put(PostgisNGDataStoreFactory.PORT.key, getPort());
            put(PostgisNGDataStoreFactory.DATABASE.key, getDatabase());
            put(PostgisNGDataStoreFactory.SCHEMA.key, getSchema());
            put(PostgisNGDataStoreFactory.USER.key, getUser());
            put(PostgisNGDataStoreFactory.PASSWD.key, getPassword());
            put(PostgisNGDataStoreFactory.PREPARED_STATEMENTS.key, true);
            put(PostgisNGDataStoreFactory.ENCODE_FUNCTIONS.key, true);
        }};
    }
}
