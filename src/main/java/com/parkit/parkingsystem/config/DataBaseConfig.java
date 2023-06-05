package com.parkit.parkingsystem.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class DataBaseConfig {

    private static final Logger logger = LogManager.getLogger("DataBaseConfig");
    protected static Properties DbConfig = new Properties();

    /**
     * Loads the {@code DBConfig.properties} file at the root of project's directory into {@code DbConfig}
     * (a {@code java.util.Properties} object)
     */
    protected void loadConfig() {
        InputStream DbConfigFile = null;
        try {
            DbConfigFile = new FileInputStream("DBConfig.properties");
            DbConfig.load(DbConfigFile);
        } catch (IOException e) {
            logger.error("Error while loading database configuration", e);
        } finally {
            if (DbConfigFile != null) {
                try { DbConfigFile.close(); }
                catch (IOException e) { logger.error("Error while closing database configuration file", e); }
            }
        }
    }

    public Connection getConnection() throws ClassNotFoundException, SQLException {
        logger.info("Create DB connection");
        Class.forName("com.mysql.cj.jdbc.Driver");
        if (DbConfig.isEmpty()) loadConfig();
        return DriverManager.getConnection(
            String.format("jdbc:mysql://%s/%s", DbConfig.getProperty("hostname"), DbConfig.getProperty("database")),
            DbConfig.getProperty("username"), DbConfig.getProperty("password"));
    }

    public void closeConnection(Connection con) {
        if(con!=null) {
            try {
                con.close();
                logger.info("Closing DB connection");
            } catch (SQLException e) {
                logger.error("Error while closing connection",e);
            }
        }
    }

    public void closePreparedStatement(PreparedStatement ps) {
        if(ps!=null) {
            try {
                ps.close();
                logger.info("Closing Prepared Statement");
            } catch (SQLException e) {
                logger.error("Error while closing prepared statement",e);
            }
        }
    }

    public void closeResultSet(ResultSet rs) {
        if(rs!=null) {
            try {
                rs.close();
                logger.info("Closing Result Set");
            } catch (SQLException e) {
                logger.error("Error while closing result set",e);
            }
        }
    }
}
