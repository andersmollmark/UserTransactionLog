package com.delaval.usertransactionlogserver.persistence.dao;

import com.delaval.usertransactionlogserver.ServerProperties;
import com.delaval.usertransactionlogserver.domain.InternalSystemProperty;
import com.delaval.usertransactionlogserver.persistence.ConnectionFactory;
import com.delaval.usertransactionlogserver.persistence.entity.ClickLog;
import com.delaval.usertransactionlogserver.persistence.entity.EventLog;
import com.delaval.usertransactionlogserver.persistence.entity.SystemProperty;
import com.delaval.usertransactionlogserver.persistence.entity.UserTransactionKey;
import com.delaval.usertransactionlogserver.persistence.operation.CreateSystemPropertyOperation;
import com.delaval.usertransactionlogserver.persistence.operation.OperationFactory;
import com.delaval.usertransactionlogserver.persistence.operation.OperationParam;
import com.delaval.usertransactionlogserver.util.UtlsLogUtil;
import simpleorm.dataset.SFieldString;
import simpleorm.sessionjdbc.SSessionJdbc;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Creates all the needed tables in DB.
 */
public class InitDAO {

    private static InitDAO _instance;
    public static final String DEFAULT_DELETE_INTERVAL_IN_DAYS = "60";

    private InitDAO() {
        // Empty by design
    }

    public static synchronized InitDAO getInstance() {
        if (_instance == null) {
            _instance = new InitDAO();
        }
        return _instance;
    }

    public boolean isCreateTables() throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM ").append(UserTransactionKey.USER_TRANSACTION_KEY.getTableName());
        UtlsLogUtil.debug(InitDAO.class, "Checking if db-tables exist:" + sql.toString());
        Connection connection = ConnectionFactory.getInstance().getConnection();
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        try {
            ps = connection.prepareStatement(sql.toString());
            resultSet = ps.executeQuery();
            if (resultSet.next()) {
                return false;
            }
        } catch (SQLException e) {
            return true;
        }
        finally {
            if(ps != null){
                try {
                    ps.close();
                } catch (SQLException e) {
                    UtlsLogUtil.error(ServerProperties.class, "Couldnt close PreparedStatement:" + e.getMessage());
                }
            }
            if(resultSet != null){
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    UtlsLogUtil.error(ServerProperties.class, "Couldnt close ResultSet:" + e.getMessage());
                }
            }
        }
        return true;

    }

    /**
     * Fetches the UserTransactionKey-table
     *
     * @return the resultset with the table
     */
    public ResultSet getUserTransactionKeyTable() throws SQLException {
        return getTableWithName(UserTransactionKey.USER_TRANSACTION_KEY.getTableName());
    }

    public void alterTables() throws SQLException {
        ResultSet clickLogTable = getTableWithName(ClickLog.CLICK_LOG.getTableName());
        UtlsLogUtil.debug(this.getClass(), ClickLog.CLICK_LOG.getTableName() + " alter table...");
        alterTables(clickLogTable, ClickLog.CLICK_LOG.getTableName(), ClickLog.getVarcharColumns());

        ResultSet eventLogTable = getTableWithName(EventLog.EVENT_LOG.getTableName());
        UtlsLogUtil.debug(this.getClass(), EventLog.EVENT_LOG.getTableName() + " alter table...");
        alterTables(eventLogTable, EventLog.EVENT_LOG.getTableName(), EventLog.getVarcharColumns());

        UtlsLogUtil.debug(this.getClass(), EventLog.EVENT_LOG.getTableName() + " alter timestamps...");
        alterTimestampColumns(EventLog.EVENT_LOG.getTableName());
        UtlsLogUtil.debug(this.getClass(), ClickLog.CLICK_LOG.getTableName() + " alter timestamps...");
        alterTimestampColumns(ClickLog.CLICK_LOG.getTableName());

    }

    private void alterTables(ResultSet table, String tablename, List<SFieldString> varcharColumns) throws SQLException {
        List<SFieldString> missingColumns = getMissingVarcharColumns(table, varcharColumns, tablename);
        for (SFieldString missingColumn : missingColumns) {
            addMissingVarcharColumn(tablename, missingColumn);
        }
    }

    private List<SFieldString> getMissingVarcharColumns(ResultSet table, List<SFieldString> varcharColumns, String tablename) {
        List<SFieldString> missingColumnnames = new ArrayList<>();
        UtlsLogUtil.debug(this.getClass(), "checking missing columns...");
        try {
            ResultSetMetaData metaData = table.getMetaData();
            for (SFieldString column : varcharColumns) {
                boolean exist = false;
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    UtlsLogUtil.debug(this.getClass(), "checking if column exist table.columnname:" + metaData.getColumnName(i) + " varcharcolumns:" + column.getColumnName());
                    exist = column.getColumnName().equals(metaData.getColumnName(i));
                    if (exist) {
                        break;
                    }
                }
                if (!exist) {
                    UtlsLogUtil.debug(this.getClass(), "found missing column:" + column.getColumnName());
                    missingColumnnames.add(column);

                }
            }
        } catch (SQLException e) {
            UtlsLogUtil.error(this.getClass(), "FATAL ERROR, something went wrong while checking columns in table:" + tablename + " " + e.getMessage());
        }
        return missingColumnnames;
    }

    private void addMissingVarcharColumn(String tablename, SFieldString missingColumn) throws SQLException {
        UtlsLogUtil.debug(this.getClass(), "adding missing column:" + missingColumn + " to table:" + tablename);
        StringBuilder sqlEvent = new StringBuilder();
        sqlEvent.append("ALTER TABLE ").append(tablename).append(" ADD ").append(missingColumn.getColumnName())
          .append(" VARCHAR(").append(missingColumn.getMaxSize()).append(")");
        String errorMess = "Something went wrong when adding missing column, " + missingColumn.getColumnName() + " to table:" + tablename + " with maxsize:" + missingColumn.getMaxSize();
        runSqlCommand(sqlEvent, errorMess);
    }

    private void alterTimestampColumns(String tablename) throws SQLException {
        UtlsLogUtil.debug(this.getClass(), "altering timestamp in table:" + tablename);
        StringBuilder sqlEvent = new StringBuilder();
        sqlEvent.append("ALTER TABLE ").append(tablename)
          .append(" MODIFY timestamp TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)");
        String errorMess = "Something went wrong when altering timestamp in table:" + tablename;
        runSqlCommand(sqlEvent, errorMess);

    }

    private ResultSet getTableWithName(String tablename) throws SQLException {
        UtlsLogUtil.debug(this.getClass(), tablename + " getTableWithName...");
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM ").append(tablename);
        return runSqlCommandAndGetResult(sql, "Something went wrong when 'select * from':" + tablename);
    }

    /**
     * Creates the database-tables needed.
     */
    public void createTables() throws Exception {
        UtlsLogUtil.debug(this.getClass(), "going to create tables");
        SSessionJdbc session = null;
        try {
            session = ConnectionFactory.getInstance().getSession(ConnectionFactory.LOG_SERVER_CONN_NAME);
            UtlsLogUtil.debug(this.getClass(), "dropping tables");
            dropAllTables(session);
            UtlsLogUtil.debug(this.getClass(), "creating tables");
            createTables(session);
        } finally {
            if (session != null) {
                session.close();
                ConnectionFactory.getInstance().closeConnection();
            }
        }
    }

    private void dropAllTables(SSessionJdbc ses) {
        ses.begin();
        dropTableNoError(ses, UserTransactionKey.USER_TRANSACTION_KEY.getTableName());
        dropTableNoError(ses, ClickLog.CLICK_LOG.getTableName());
        dropTableNoError(ses, EventLog.EVENT_LOG.getTableName());
        dropTableNoError(ses, SystemProperty.SYSTEM_PROPERTY.getTableName());
        ses.commit();
    }

    private void dropTableNoError(SSessionJdbc ses, String table) {
        ses.flush();
        ses.getDriver().dropTableNoError(table);
        ses.commit();
        ses.begin();
    }

    private void createTables(SSessionJdbc ses) {
        ses.begin();
        ses.rawUpdateDB(ses.getDriver().createTableSQL(UserTransactionKey.USER_TRANSACTION_KEY));
        ses.rawUpdateDB(ses.getDriver().createTableSQL(ClickLog.CLICK_LOG));
        ses.rawUpdateDB(ses.getDriver().createTableSQL(EventLog.EVENT_LOG));
        ses.rawUpdateDB(ses.getDriver().createTableSQL(SystemProperty.SYSTEM_PROPERTY));
        ses.commit();
    }


    public void createDeleteLogEvent() throws SQLException {
        StringBuilder sql = new StringBuilder("SET GLOBAL event_scheduler = ON");
        String errorMessSchedule = "Something went wrong when creating delete_log-event:";
        runSqlCommand(sql, errorMessSchedule);
        createDeleteClickLogEvent();
        createDeleteEventLogEvent();
    }

    public void createDeleteClickLogEvent() throws SQLException {

        String deleteClickLogsIntervalName = ServerProperties.getInstance().getProp(ServerProperties.PropKey.SYSTEM_PROPERTY_NAME_DELETE_CLICK_LOGS_INTERVAL);
        String deleteInterval = getDeleteInterval(deleteClickLogsIntervalName);

        StringBuilder dropSqlEvent = new StringBuilder();
        dropSqlEvent.append("DROP EVENT IF EXISTS delete_click_log");
        String dropErrorMess = "Something went wrong when dropping delete_click_log-event:";
        runSqlCommand(dropSqlEvent, dropErrorMess);

        StringBuilder sqlEvent = new StringBuilder();
        sqlEvent.append("CREATE EVENT delete_click_log ")
          .append("ON SCHEDULE EVERY 1 DAY STARTS ")
          .append(ServerProperties.getInstance().getProp(ServerProperties.PropKey.DELETE_LOGS_EVENT_START))
          .append(" ON COMPLETION PRESERVE ")
          .append("DO DELETE FROM ClickLog WHERE timestamp < DATE_SUB(NOW(), INTERVAL ")
          .append(deleteInterval)
          .append(" DAY)");
        String errorMess = "Something went wrong when creating delete_click_log-event:";
        runSqlCommand(sqlEvent, errorMess);
    }

    public void createDeleteEventLogEvent() throws SQLException {

        String deleteEventLogsIntervalName = ServerProperties.getInstance().getProp(ServerProperties.PropKey.SYSTEM_PROPERTY_NAME_DELETE_EVENT_LOGS_INTERVAL);
        String deleteInterval = getDeleteInterval(deleteEventLogsIntervalName);

        StringBuilder dropSqlEvent = new StringBuilder();
        dropSqlEvent.append("DROP EVENT IF EXISTS delete_event_logs");
        String dropErrorMess = "Something went wrong when dropping delete_event_logs-event:";
        runSqlCommand(dropSqlEvent, dropErrorMess);


        StringBuilder sqlEvent = new StringBuilder();
        sqlEvent.append("CREATE EVENT delete_event_logs ")
          .append("ON SCHEDULE EVERY 1 DAY STARTS ")
          .append(ServerProperties.getInstance().getProp(ServerProperties.PropKey.DELETE_LOGS_EVENT_START))
          .append(" ON COMPLETION PRESERVE ")
          .append("DO DELETE FROM EventLog WHERE timestamp < DATE_SUB(NOW(), INTERVAL ")
          .append(deleteInterval)
          .append(" DAY)");
        String errorMess = "Something went wrong when creating delete_event_log-event:";
        runSqlCommandAndGetResult(sqlEvent, errorMess);
    }

    public List<String> getAllUserTransactionKeysThatLacksLogs() {

        ResultSet resultSet = null;
        List<String> ids = new ArrayList<>();
        try {
            StringBuilder sqlEvent = new StringBuilder();
            sqlEvent.append("select * from UserTransactionKey u ")
              .append("where not exists (select * from EventLog el where el.userTransactionKeyId = u.id) ")
              .append("and not exists (select * from ClickLog cl where cl.userTransactionKeyId = u.id)");
            String errorMess = "Something went wrong when trying to fetch all usertransactionKeyIds that lacks logs:";
            resultSet = runSqlCommandAndGetResult(sqlEvent, errorMess);

            while (resultSet.next()) {
                ids.add(resultSet.getString("id"));
            }
        } catch (SQLException e) {
            UtlsLogUtil.error(this.getClass(), "Something went wrong when iterating resultset:" + e.getMessage());
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (SQLException e) {
                UtlsLogUtil.error(this.getClass(), "Something went wrong when closing resultset:" + e.getMessage());
            }
        }
        return ids;
    }

    private String getDeleteInterval(String name) throws SQLException {
        String sql = "SELECT * FROM SystemProperty WHERE NAME='" + name + "'";
        Connection connection = ConnectionFactory.getInstance().getConnection();
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        String interval = ServerProperties.getInstance().getProp(ServerProperties.PropKey.DELETE_LOGS_INTERVAL_DEFAULT_IN_DAYS);
        try {
            ps = connection.prepareStatement(sql);
            resultSet = ps.executeQuery();
            if (resultSet.next()) {
                String valueFromDB = resultSet.getString(SystemProperty.VALUE_COLUMN);
                if(!valueFromDB.equals(interval)){
                    interval = createDeleteInterval(name, resultSet, interval);
                }
            } else {
                interval = createDeleteInterval(name, resultSet, interval);
            }

        } catch (SQLException e) {
            UtlsLogUtil.error(this.getClass(), "Something went wrong while fetching deleteinterval with name:" + name + ", exception:" + e.getMessage());
        } finally {
            try {
                if(ps != null){
                    ps.close();
                }
            } catch (SQLException e) {
                UtlsLogUtil.error(this.getClass(), "Something went wrong when closing prepared statement:" + e.getMessage());
            }
            try {
                tryClose(resultSet);
            } catch (SQLException e) {
                UtlsLogUtil.error(this.getClass(), "Something went wrong when closing resultset:" + e.getMessage());
            }

        }
        return interval;
    }

    private String createDeleteInterval(String name, ResultSet resultSet, String interval) {
        String result = InitDAO.DEFAULT_DELETE_INTERVAL_IN_DAYS;
        try {
            tryClose(resultSet);
            result = createDeleteIntervalProperty(name, interval);
        } catch (SQLException e) {
            UtlsLogUtil.error(this.getClass(), "Something went wrong when closing resultset:" + e.getMessage());
        }
        return result;
    }

    private void tryClose(ResultSet resultSet) throws SQLException {
        if (resultSet != null && !resultSet.isClosed()) {
            resultSet.close();
        }
    }

    private String createDeleteIntervalProperty(String name, String interval) {
        if (interval == null) {
            UtlsLogUtil.error(this.getClass(), "Missing serverproperty: " + name + ", setting " + DEFAULT_DELETE_INTERVAL_IN_DAYS + " as default");
            interval = DEFAULT_DELETE_INTERVAL_IN_DAYS;
        }
        InternalSystemProperty newProperty = new InternalSystemProperty();
        newProperty.setName(name);
        newProperty.setValue(interval);
        newProperty.setTimestamp(new Date());
        OperationParam<CreateSystemPropertyOperation> createSystemPropertyParamForSystem = OperationFactory.getCreateSystemPropertyParamForSystem(newProperty);
        OperationDAO.getInstance().executeOperation(createSystemPropertyParamForSystem);
        return interval;
    }


    private void runSqlCommand(StringBuilder sql, String errorMess) throws SQLException {
        UtlsLogUtil.debug(InitDAO.class, "running sql-command:" + sql.toString());
        Connection connection = ConnectionFactory.getInstance().getConnection();
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(sql.toString());
            resultSet = ps.executeQuery();

        } catch (SQLException e) {
            UtlsLogUtil.error(this.getClass(), errorMess + e.getMessage());
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (SQLException e) {
                UtlsLogUtil.error(this.getClass(), "Something went wrong when closing resultset:" + e.getMessage());
            }
            try {
                if(ps != null){
                    ps.close();
                }
            } catch (SQLException e) {
                UtlsLogUtil.error(this.getClass(), "Something went wrong when closing prepared statement:" + e.getMessage());
            }
        }
    }

    private ResultSet runSqlCommandAndGetResult(StringBuilder sql, String errorMess) throws SQLException {
        UtlsLogUtil.debug(InitDAO.class, "running sql-command and returning result:" + sql.toString());
        Connection connection = ConnectionFactory.getInstance().getConnection();
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(sql.toString());
            resultSet = ps.executeQuery();

        } catch (SQLException e) {
            UtlsLogUtil.error(this.getClass(), errorMess + e.getMessage());
        }
        finally {
            if(ps != null){
                try {
                    ps.close();
                } catch (SQLException e) {
                    UtlsLogUtil.error(this.getClass(), "Something went wrong when closing prepared statement:" + e.getMessage());
                }
            }
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    UtlsLogUtil.error(this.getClass(), "Something went wrong when closing resultset:" + e.getMessage());
                }
            }
        }
        return resultSet;
    }

}
