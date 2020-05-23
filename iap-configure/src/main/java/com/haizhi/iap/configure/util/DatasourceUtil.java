package com.haizhi.iap.configure.util;

import org.apache.commons.dbcp.BasicDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by chenbo on 2017/10/10.
 */
public class DatasourceUtil {

    public static BasicDataSource getMysqlDataSource(String host, Integer port, String database,
                                                     String username, String password){

        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName("com.mysql.jdbc.Driver");
        ds.setUrl("jdbc:mysql://" + host + ":" + port + "/" + database
                + "?Unicode=true&characterEncoding=utf8");
        ds.setUsername(username);
        ds.setPassword(password);
        return ds;
    }

    public static void closeMysqlConn(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
