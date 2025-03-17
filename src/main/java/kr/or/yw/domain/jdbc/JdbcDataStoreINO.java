package kr.or.yw.domain.jdbc;

import lombok.Getter;

import java.util.Map;

@Getter
public abstract class JdbcDataStoreINO {

    private String host;

    private String port;

    private String database;

    private String schema;

    private String user;

    private String password;

    public void shp2db() {

    }

    public void json2db() {

    }

    public void a2db() {

    }

    public void db2shp() {

    }

    public void db2json() {

    }

    abstract Map<String, Object> getJdbcProperties();
}
