package main;

import com.delaval.usertransactionlogserver.persistence.ConnectionFactory;
import com.delaval.usertransactionlogserver.websocket.WebSocketMessage;

import java.sql.*;

/**
 * Created by delaval on 12/2/2015.
 */
public class JdbcTest {


    public JdbcTest() throws SQLException {
//        doTheSelectCount();

        MyWebSocketMessage webSocketMessage = new MyWebSocketMessage();
        webSocketMessage.setClient("klienten");
        webSocketMessage.setUsername("LEIF USER");
        webSocketMessage.setTarget("Ipad");
        for(int i=0; i<10; i++) {
            doInsert(webSocketMessage);
        }
        ConnectionFactory.getInstance().closeConnection();
        for(int i=0; i<10; i++) {
            doInsert(webSocketMessage);
        }

    }

    private void doTheSelectCount() throws SQLException {
        Connection connection = ConnectionFactory.getInstance().getConnection();
        String sql = "SELECT COUNT(*) as rowcount FROM LogContent";
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(sql);
            resultSet = ps.executeQuery();

            while(resultSet.next()){
                int count = resultSet.getInt("rowcount");
                System.out.println("antal:" + count);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if(resultSet != null) {
                    resultSet.close();
                }
                if(ps != null){
                    ps.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }



    private void doInsert(WebSocketMessage webSocketMessage){
        Connection connection = null;
        try {
            connection = ConnectionFactory.getInstance().getConnection();
            JdbcWorker worker = new InsertWorker();
            worker.doTheWork(connection, webSocketMessage);
        } catch (Exception e) {
            System.out.println("something happened:" + e.getMessage());
            try {
                if(connection != null) {
                    connection.rollback();
                }
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }
        finally {
        }
    }

    private static class MyWebSocketMessage extends WebSocketMessage{
        private String jsonContent;
        private String client;
        private String username;
        private String messType;

        @Override
        public String getClient() {
            return client;
        }

        @Override
        public String getUsername() {
            return username;
        }

        @Override
        public String getTarget() {
            return target;
        }

        private String target;


        public void setJsonContent(String jsonContent) {
            this.jsonContent = jsonContent;
        }

        public void setClient(String client) {
            this.client = client;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public void setMessType(String messType) {
            this.messType = messType;
        }

        public void setTarget(String target) {
            this.target = target;
        }
    }

    interface JdbcWorker{
        String getSQL();
        void doTheWork(Connection connection, WebSocketMessage webSocketMessage) throws SQLException;
    }

    private static class InsertWorker implements JdbcWorker{

        private String sql = "INSERT INTO UserTransactionKey (id, username, target, client, timestamp) " +
                "VALUES (?, ?, ?, ?, ?)";


        @Override
        public String getSQL() {
            return sql;
        }

        @Override
        public void doTheWork(Connection connection, WebSocketMessage webSocketMessage) throws SQLException {
            PreparedStatement ps = null;
            try {
                ps = connection.prepareStatement(sql);

                java.util.Date now = new java.util.Date();
                ps.setString(1, now.getTime() + webSocketMessage.getUsername());
                ps.setString(2, webSocketMessage.getUsername());
                ps.setString(3, webSocketMessage.getTarget());
                ps.setString(4, webSocketMessage.getClient());
                ps.setTimestamp(5, new Timestamp(now.getTime()));

                ps.executeUpdate();
            }
            finally {
                if(ps != null) {
                    System.out.println("CLOSING ps");
                    ps.close();
                }
            }
            connection.commit();
        }
    }

    public static void main(String[] args) throws SQLException {
        new JdbcTest();
    }
}
