package com.delaval.usertransactionlogserver.persistence;

import com.delaval.usertransactionlogserver.ServerProperties;
import com.delaval.usertransactionlogserver.persistence.entity.EventLog;
import com.delaval.usertransactionlogserver.util.UtlsLogUtil;
import com.delaval.usertransactionlogserver.websocket.MessTypes;
import com.delaval.usertransactionlogserver.websocket.WebSocketMessage;
import simpleorm.sessionjdbc.SSessionJdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Singleton.
 * Handles connection and sessions to db.
 */
public class ConnectionFactory {

    private static final String LOCK_COLUMN_NAME = "In_use";

    private enum LogTable{
        EVENT_LOG_TABLE(MessTypes.EVENT_LOG, EventLog.EVENT_LOG.getTableName());

        private String myTableName;
        private MessTypes myType;

        LogTable(MessTypes type, String tableName){
            myType = type;
            myTableName = tableName;
        }

        static String getTableName(MessTypes type){
            for(LogTable value: LogTable.values()){
                if(value.myType.equals(type)){
                    return value.myTableName;
                }
            }
            return null;
        }
    }


    private static ConnectionFactory _instance;
    public static final String LOG_SERVER_CONN_NAME = "LogServer";

    private Connection theConnection = null;

    private ConnectionFactory() throws SQLException {
        createConnection();
    }

    public static synchronized ConnectionFactory getInstance() throws SQLException {
        if (_instance == null || isConnectionClosed()) {
            _instance = new ConnectionFactory();
        }
        return _instance;
    }

    public static synchronized void checkIfConnectionIsOk() throws SQLException {
        ConnectionFactory.getInstance().getConnection();
        String lockedTable = ConnectionFactory.getInstance().getTablenameIfAnyLockedTable();
        if(lockedTable != null){
            throw new SQLException("table:" + lockedTable + " is locked");
        }
    }


    private static boolean isConnectionClosed() {
        try {
            return _instance.getConnection().isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
            return true;
        }
    }

    private String getTablenameIfAnyLockedTable() throws SQLException {
        for(LogTable logTable: LogTable.values()){
            if(isTableWithNameLocked(logTable.myTableName)){
                return logTable.myTableName;
            }
        }
        return null;
    }



    public boolean isTableLocked(WebSocketMessage messType) throws SQLException {
        String tablename = LogTable.getTableName(MessTypes.getType(messType.getMessType()));
        return tablename != null && isTableWithNameLocked(tablename);

    }

    private boolean isTableWithNameLocked(String tablename) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("SHOW OPEN TABLES LIKE '").append(tablename).append("'");
        UtlsLogUtil.debug(ConnectionFactory.class, "Checking if db-table is locked:", sql.toString());
        Connection connection = ConnectionFactory.getInstance().getConnection();
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        try {
            ps = connection.prepareStatement(sql.toString());
            resultSet = ps.executeQuery();
            if (resultSet.next()) {
                int in_use = resultSet.getInt(LOCK_COLUMN_NAME);
                return in_use == 1;
            }
        } catch (SQLException e) {
            UtlsLogUtil.error(ServerProperties.class, "Couldnt check if table was locked:", e.getMessage());
            throw e;
        }
        finally {
            if(ps != null){
                try {
                    ps.close();
                } catch (SQLException e) {
                    UtlsLogUtil.error(ServerProperties.class, "Couldnt close PreparedStatement:", e.getMessage());
                }
            }
            if(resultSet != null){
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    UtlsLogUtil.error(ServerProperties.class, "Couldnt close ResultSet:", e.getMessage());
                }
            }
        }
        return false;
    }

    public static void closeFactory() throws SQLException {
        UtlsLogUtil.info(getInstance().getClass(), "Closing factory");
        _instance.closeConnection();

        _instance = null;
    }

    public SSessionJdbc getSession(String connectionName) {
        return SSessionJdbc.open(getConnection(), connectionName);
    }

    public void closeConnection() {
        UtlsLogUtil.info(this.getClass(), "Closing connection");
        if (getConnection() != null) {
            try {
                if (!getConnection().isClosed()) {
                    getConnection().close();
                }
            } catch (SQLException e) {
                UtlsLogUtil.error(this.getClass(), "Error while closing connection:", e.getMessage());
            }
        }
    }

    public Connection getConnection() {
        try {
            if (theConnection.isClosed()) {
                createConnection();
            }
            return theConnection;
        } catch (SQLException e) {
            UtlsLogUtil.error(this.getClass(), "Error while fetching connection:", e.getMessage());
        }
        return theConnection;
    }

    private void createConnection() throws SQLException {
        try {
            String jdbcUrl = "jdbc:mysql://" + getProp(ServerProperties.PropKey.DB_SERVER_HOST) +
              ":" + getProp(ServerProperties.PropKey.DB_SERVER_PORT) + "/" +
              getProp(ServerProperties.PropKey.DB_NAME);
            UtlsLogUtil.debug(this.getClass(), "Creating db-connection:", jdbcUrl);
            theConnection = java.sql.DriverManager.getConnection(jdbcUrl, getProp(ServerProperties.PropKey.DB_USER), getProp(ServerProperties.PropKey.DB_PWD));

            theConnection.setAutoCommit(false);
        } catch (SQLException e) {
            UtlsLogUtil.error(this.getClass(), "Error while creating connection:", e.getMessage());
            throw e;
        }
    }


    private String getProp(ServerProperties.PropKey propKey) {
        return ServerProperties.getInstance().getProp(propKey);
    }

}
