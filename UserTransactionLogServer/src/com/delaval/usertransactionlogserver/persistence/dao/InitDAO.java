package com.delaval.usertransactionlogserver.persistence.dao;

import com.delaval.usertransactionlogserver.ServerProperties;
import com.delaval.usertransactionlogserver.domain.InternalSystemProperty;
import com.delaval.usertransactionlogserver.persistence.ConnectionFactory;
import com.delaval.usertransactionlogserver.persistence.entity.EventLog;
import com.delaval.usertransactionlogserver.persistence.entity.SystemProperty;
import com.delaval.usertransactionlogserver.persistence.entity.UserTransactionKey;
import com.delaval.usertransactionlogserver.persistence.operation.CreateSystemPropertyOperation;
import com.delaval.usertransactionlogserver.persistence.operation.GetSystemPropertyWithNameOperation;
import com.delaval.usertransactionlogserver.persistence.operation.OperationFactory;
import com.delaval.usertransactionlogserver.persistence.operation.OperationResult;
import com.delaval.usertransactionlogserver.util.UtlsLogUtil;
import simpleorm.dataset.SFieldString;
import simpleorm.sessionjdbc.SSessionJdbc;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Singleton
 * Creates all the needed tables in DB. It also handles updates and altering to columns in tables.
 */
public class InitDAO {

    private static InitDAO _instance;
    public static final String DEFAULT_DELETE_INTERVAL_IN_DAYS = "60";

    private InitDAO() {
        // Empty by design (Singleton)
    }

    public static synchronized InitDAO getInstance() {
        if (_instance == null) {
            _instance = new InitDAO();
        }
        return _instance;
    }

    /**
     * Checks the usertransaction-key-table exists in db.
     * @return true if it does
     * @throws SQLException
     */
    public boolean isCreateTables() throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM ").append(UserTransactionKey.USER_TRANSACTION_KEY.getTableName());
        UtlsLogUtil.info(InitDAO.class, "Checking if db-tables exist:", sql.toString());
        Connection connection = ConnectionFactory.getInstance().getConnection();
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        try {
            ps = connection.prepareStatement(sql.toString());
            resultSet = ps.executeQuery();
            if (resultSet.next()) {
                return false;
            }
        }
        catch (SQLException e) {
            return true;
        }
        finally {
            if (ps != null) {
                try {
                    ps.close();
                }
                catch (SQLException e) {
                    UtlsLogUtil.error(InitDAO.class, "Couldnt close PreparedStatement:", e.getMessage());
                }
            }
            if (resultSet != null) {
                try {
                    resultSet.close();
                }
                catch (SQLException e) {
                    UtlsLogUtil.error(InitDAO.class, "Couldnt close ResultSet:", e.getMessage());
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

    /**
     * Alter EventLog- and SystemProperty-tables. Adding/altering varchar-columns and timestamp-columns
     * @throws SQLException
     */
    public void alterTables() throws SQLException {
        UtlsLogUtil.info(this.getClass(), EventLog.EVENT_LOG.getTableName(), " alter table...");
        ResultSet eventLogTable = getTableWithName(EventLog.EVENT_LOG.getTableName());
        alterTables(eventLogTable, EventLog.EVENT_LOG.getTableName(), EventLog.getVarcharColumns());

        UtlsLogUtil.info(this.getClass(), SystemProperty.SYSTEM_PROPERTY.getTableName(), " alter table...");
        ResultSet systemPropTable = getTableWithName(SystemProperty.SYSTEM_PROPERTY.getTableName());
        alterTables(systemPropTable, SystemProperty.SYSTEM_PROPERTY.getTableName(), SystemProperty.getVarcharColumns());

        UtlsLogUtil.info(this.getClass(), EventLog.EVENT_LOG.getTableName(), " alter timestamps...");
        alterTimestampColumns(EventLog.EVENT_LOG.getTableName());
        UtlsLogUtil.info(this.getClass(), SystemProperty.SYSTEM_PROPERTY.getTableName(), " alter timestamps...");
        alterTimestampColumns(SystemProperty.SYSTEM_PROPERTY.getTableName());

        updateUserTransactionKeyIdToLowerIfExist();
    }

    /**
     * Alter a table. If a varcharcolumn is missing, it adds it and if a size is changed in the column it changes that.
     * @param table
     * @param tablename
     * @param varcharColumns
     * @throws SQLException
     */
    private void alterTables(ResultSet table, String tablename, List<SFieldString> varcharColumns) throws SQLException {
        List<SFieldString> missingColumns = getMissingVarcharColumns(table, varcharColumns, tablename);
        for (SFieldString missingColumn : missingColumns) {
            addMissingVarcharColumn(tablename, missingColumn);
        }
        List<SFieldString> varcharColumnsWithBiggerColumnSize = getVarcharColumnsWithBiggerColumnSize(table, varcharColumns, tablename);
        for (SFieldString columnWithBiggerSize : varcharColumnsWithBiggerColumnSize) {
            alterSizeOfColumn(tablename, columnWithBiggerSize);
        }
    }

    /**
     * Fetches all varchar-columns thats missing in the table according to the entity
     * @param table
     * @param varcharColumns
     * @param tablename
     * @return a list with the names of the columns thats missing
     */
    private List<SFieldString> getMissingVarcharColumns(ResultSet table, List<SFieldString> varcharColumns, String tablename) {
        List<SFieldString> missingColumnnames = new ArrayList<>();
        UtlsLogUtil.info(this.getClass(), "checking missing columns...");
        try {
            ResultSetMetaData metaData = table.getMetaData();
            for (SFieldString column : varcharColumns) {
                boolean exist = false;
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    UtlsLogUtil.info(this.getClass(),
                      "checking if column exist table.columnname:", metaData.getColumnName(i),
                      " varcharcolumns:", column.getColumnName());
                    exist = column.getColumnName().equals(metaData.getColumnName(i));
                    if (exist) {
                        break;
                    }
                }
                if (!exist) {
                    UtlsLogUtil.info(this.getClass(), "found missing column:", column.getColumnName());
                    missingColumnnames.add(column);

                }
            }
        }
        catch (SQLException e) {
            UtlsLogUtil.error(this.getClass(),
              "FATAL ERROR, something went wrong while checking columns in table:", tablename,
              " ", e.getMessage());
        }
        return missingColumnnames;
    }

    /**
     * Fetches the columns that had got a bigger size in entity than in database
     * @param table
     * @param varcharColumns
     * @param tablename
     * @return list with the columns that need to be updated
     */
    private List<SFieldString> getVarcharColumnsWithBiggerColumnSize(ResultSet table, List<SFieldString> varcharColumns, String tablename) {
        List<SFieldString> changedColumnSize = new ArrayList<>();
        UtlsLogUtil.info(this.getClass(), "checking size of columns...");
        try {
            ResultSetMetaData metaData = table.getMetaData();
            for (SFieldString column : varcharColumns) {
                boolean changedSize = false;
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    UtlsLogUtil.info(this.getClass(),
                      "checking if column size is changed, columnname:", metaData.getColumnName(i),
                      " varcharcolumns:", column.getColumnName());
                    changedSize = column.getMaxSize() > metaData.getColumnDisplaySize(i);
                    if (!changedSize) {
                        break;
                    }
                }
                if (changedSize) {
                    UtlsLogUtil.info(this.getClass(), "found column with changed size:", column.getColumnName());
                    changedColumnSize.add(column);

                }
            }
        }
        catch (SQLException e) {
            UtlsLogUtil.error(this.getClass(),
              "FATAL ERROR, something went wrong while checking columnsize in table:", tablename,
              " ", e.getMessage());
        }
        return changedColumnSize;
    }


    private void addMissingVarcharColumn(String tablename, SFieldString missingColumn) throws SQLException {
        UtlsLogUtil.info(this.getClass(),
          "adding missing column:", missingColumn.toString(),
          " to table:", tablename);
        StringBuilder sqlEvent = new StringBuilder();
        sqlEvent.append("ALTER TABLE ").append(tablename).append(" ADD ").append(missingColumn.getColumnName())
          .append(" VARCHAR(").append(missingColumn.getMaxSize()).append(")");
        String errorMess = "Something went wrong when adding missing column, " + missingColumn.getColumnName() + " to table:" + tablename + " with maxsize:" + missingColumn.getMaxSize();
        runSqlCommand(sqlEvent, errorMess);
    }

    private void alterSizeOfColumn(String tablename, SFieldString column) throws SQLException {
        UtlsLogUtil.info(this.getClass(),
          " changing size of column:", column.toString(),
          " to table:", tablename, " to size:", Integer.toString(column.getMaxSize()));
        StringBuilder sqlEvent = new StringBuilder();
        sqlEvent.append("ALTER TABLE ").append(tablename).append(" MODIFY ").append(column.getColumnName())
          .append(" VARCHAR(").append(column.getMaxSize()).append(")");
        String errorMess = "Something went wrong when adding missing column, " + column.getColumnName() + " to table:" + tablename + " with maxsize:" + column.getMaxSize();
        runSqlCommand(sqlEvent, errorMess);
    }


    private void alterTimestampColumns(String tablename) throws SQLException {
        UtlsLogUtil.info(this.getClass(), "altering timestamp in table:", tablename);
        StringBuilder sqlEvent = new StringBuilder();
        sqlEvent.append("ALTER TABLE ").append(tablename)
          .append(" MODIFY timestamp TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)");
        String errorMess = "Something went wrong when altering timestamp in table:" + tablename;
        runSqlCommand(sqlEvent, errorMess);

    }

    private void updateUserTransactionKeyIdToLowerIfExist() throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("select id from ").append(UserTransactionKey.USER_TRANSACTION_KEY.getTableName())
          .append(" where id REGEXP BINARY '[A-Z]'");
        UtlsLogUtil.info(InitDAO.class, "check if it exist any userTransactionKeyId with capital letters:", sql.toString());
        Connection connection = ConnectionFactory.getInstance().getConnection();
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        try {
            ps = connection.prepareStatement(sql.toString());
            resultSet = ps.executeQuery();
            if (resultSet.next()) {
                StringBuilder sqlCommand = new StringBuilder();
                sqlCommand.append("update ").append(UserTransactionKey.USER_TRANSACTION_KEY.getTableName()).append(" set id = LOWER(id) where id REGEXP BINARY '[A-Z]'");
                StringBuilder errorBuilder = new StringBuilder();
                errorBuilder.append("Something went wrong when updating id-colum in ").
                  append(UserTransactionKey.USER_TRANSACTION_KEY.getTableName()).
                  append(" to lower case");
                runSqlCommand(sqlCommand, errorBuilder.toString());

                sqlCommand = new StringBuilder();
                sqlCommand.append("update ").append(EventLog.EVENT_LOG.getTableName()).append(" set userTransactionKeyId = LOWER(userTransactionKeyId) where userTransactionKeyId REGEXP BINARY '[A-Z]'");
                errorBuilder = new StringBuilder();
                errorBuilder.append("Something went wrong when updating userTransacetionKeyId-colum in ").
                  append(EventLog.EVENT_LOG.getTableName()).
                  append(" to lower case");
                runSqlCommand(sqlCommand, errorBuilder.toString());

            }
        }
        catch (SQLException e) {
            UtlsLogUtil.error(InitDAO.class, "Something went wrong while altering userTransactionKeyId:", e.getMessage());
        }
        finally {
            if (ps != null) {
                try {
                    ps.close();
                }
                catch (SQLException e) {
                    UtlsLogUtil.error(InitDAO.class, "Couldnt close PreparedStatement:", e.getMessage());
                }
            }
            if (resultSet != null) {
                try {
                    resultSet.close();
                }
                catch (SQLException e) {
                    UtlsLogUtil.error(InitDAO.class, "Couldnt close ResultSet:", e.getMessage());
                }
            }
        }
    }

    private ResultSet getTableWithName(String tablename) throws SQLException {
        UtlsLogUtil.info(this.getClass(), tablename, " getTableWithName...");
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM ").append(tablename);
        return runSqlCommandAndGetResult(sql, "Something went wrong when 'select * from':" + tablename);
    }

    /**
     * Creates the database-tables needed.
     */
    public void createTables() throws Exception {
        UtlsLogUtil.info(this.getClass(), "going to create tables");
        SSessionJdbc session = null;
        try {
            session = ConnectionFactory.getInstance().getSession(ConnectionFactory.LOG_SERVER_CONN_NAME);
            UtlsLogUtil.info(this.getClass(), "dropping tables");
            dropAllTables(session);
            UtlsLogUtil.info(this.getClass(), "creating tables");
            createTables(session);
        }
        finally {
            if (session != null) {
                session.close();
                ConnectionFactory.getInstance().closeConnection();
            }
        }
    }

    private void dropAllTables(SSessionJdbc ses) {
        ses.begin();
        dropTableNoError(ses, UserTransactionKey.USER_TRANSACTION_KEY.getTableName());
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
        ses.rawUpdateDB(ses.getDriver().createTableSQL(EventLog.EVENT_LOG));
        ses.rawUpdateDB(ses.getDriver().createTableSQL(SystemProperty.SYSTEM_PROPERTY));
        ses.commit();
    }

    public void updateSystemUser() throws SQLException {
        UtlsLogUtil.info(this.getClass(), "updateSystemUser");
        StringBuilder sql = new StringBuilder("update SystemProperty set userTransactionKeyId = 'systemsystemsystem' where userTransactionKeyId like('SystemSystemSystem')");
        String errorMessSchedule = "Something went wrong when updating SystemProperty and systemuser:";
        runSqlCommand(sql, errorMessSchedule);

        StringBuilder sql_2 = new StringBuilder("update UserTransactionKey set id = 'systemsystemsystem' where id like('SystemSystemSystem')");
        String errorMessSchedule_2 = "Something went wrong when updating UserTransactionKey and systemuser-id:";
        runSqlCommand(sql_2, errorMessSchedule_2);

    }

    public void createDeleteLogEvent() throws SQLException {
        UtlsLogUtil.info(this.getClass(), "createDeleteLogEvent");
        StringBuilder sql = new StringBuilder("SET GLOBAL event_scheduler = ON");
        String errorMessSchedule = "Something went wrong when creating delete_log-event:";
        runSqlCommand(sql, errorMessSchedule);
        createDeleteEventLogEvent();
    }

    public void createFetchLogUsers() {
        UtlsLogUtil.info(this.getClass(), " checking if we have to create fetch-log-users");
        String delproUser = ServerProperties.getInstance().getProp(ServerProperties.PropKey.FETCH_LOG_USER_DELPRO);
        String toolUser = ServerProperties.getInstance().getProp(ServerProperties.PropKey.FETCH_LOG_USER_TOOL);
        InternalSystemProperty delproUserProperty = getSystemPropertyWithName(delproUser);
        InternalSystemProperty toolUserProperty = getSystemPropertyWithName(toolUser);
        if (delproUserProperty == null || !delproUserProperty.getValue().equals(delproUser)) {
            UtlsLogUtil.info(this.getClass(), " creating delpro fetch-log-user as a systemproperty");
            createSystemProperty(delproUser, delproUser);
        }
        if (toolUserProperty == null || !toolUserProperty.getValue().equals(toolUser)) {
            UtlsLogUtil.info(this.getClass(), " creating utls-tool fetch-log-user as a systemproperty");
            createSystemProperty(toolUser, toolUser);
        }

    }

    private void createDeleteEventLogEvent() throws SQLException {
        UtlsLogUtil.info(this.getClass(), " create deleteEventLogEvent");
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
        UtlsLogUtil.info(this.getClass(), "getAllUserTransactionKeysThatLacksLogs");
        ResultSet resultSet = null;
        List<String> ids = new ArrayList<>();
        try {
            StringBuilder sqlEvent = new StringBuilder();
            sqlEvent.append("select * from UserTransactionKey u ")
              .append("where not exists (select * from EventLog el where el.userTransactionKeyId = u.id)");
            String errorMess = "Something went wrong when trying to fetch all usertransactionKeyIds that lacks logs:";
            resultSet = runSqlCommandAndGetResult(sqlEvent, errorMess);

            while (resultSet.next()) {
                ids.add(resultSet.getString("id"));
            }
        }
        catch (SQLException e) {
            UtlsLogUtil.error(this.getClass(), "Something went wrong when iterating resultset:", e.getMessage());
        }
        finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
            }
            catch (SQLException e) {
                UtlsLogUtil.error(this.getClass(), "Something went wrong when closing resultset:", e.getMessage());
            }
        }
        return ids;
    }

    private String getDeleteInterval(String name) throws SQLException {
        UtlsLogUtil.info(this.getClass(), " getDeleteInterval with name:" + name);
        InternalSystemProperty systemPropertyWithName = getSystemPropertyWithName(name);
        String interval = ServerProperties.getInstance().getProp(ServerProperties.PropKey.DELETE_LOGS_INTERVAL_DEFAULT_IN_DAYS) != null ?
          ServerProperties.getInstance().getProp(ServerProperties.PropKey.DELETE_LOGS_INTERVAL_DEFAULT_IN_DAYS) : DEFAULT_DELETE_INTERVAL_IN_DAYS;

        if (systemPropertyWithName == null || !systemPropertyWithName.getValue().equals(interval)) {
            interval = createSystemProperty(name, interval);
        }
        return interval;
    }

    private InternalSystemProperty getSystemPropertyWithName(String name) {
        UtlsLogUtil.info(this.getClass(), " fetching systemproperty with name:" + name);
        GetSystemPropertyWithNameOperation operation = OperationFactory.getSystemPropertyWithName(name);
        OperationResult<InternalSystemProperty> operationResult = OperationDAO.getInstance().doRead(operation);
        List<InternalSystemProperty> result = operationResult.getResult();
        if (result.size() > 0) {
            return result.get(0);
        }
        return null;
    }

    private String createSystemProperty(String name, String value) {
        InternalSystemProperty newProperty = new InternalSystemProperty();
        newProperty.setName(name);
        newProperty.setValue(value);
        newProperty.setTimestamp(new Date());
        CreateSystemPropertyOperation operation = OperationFactory.getCreateSystemPropertyForSystem(newProperty);
        OperationDAO.getInstance().doCreateUpdate(operation);
        return value;
    }

    private void runSqlCommand(StringBuilder sql, String errorMess) throws SQLException {
        UtlsLogUtil.info(InitDAO.class, "running sql-command:", sql.toString());
        Connection connection = ConnectionFactory.getInstance().getConnection();

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            ps.executeQuery();
            connection.commit();
        }
        catch (SQLException e) {
            UtlsLogUtil.error(this.getClass(), errorMess, e.getMessage());
            connection.rollback();

        }
    }

    private ResultSet runSqlCommandAndGetResult(StringBuilder sql, String errorMess) throws SQLException {
        UtlsLogUtil.info(InitDAO.class, "running sql-command and returning result:", sql.toString());
        Connection connection = ConnectionFactory.getInstance().getConnection();
        ResultSet resultSet = null;
        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            resultSet = ps.executeQuery();

        }
        catch (SQLException e) {
            UtlsLogUtil.error(this.getClass(), errorMess, e.getMessage());
        }
        finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                }
                catch (SQLException e) {
                    UtlsLogUtil.error(this.getClass(), "Something went wrong when closing resultset:", e.getMessage());
                }
            }
        }
        return resultSet;
    }

}
