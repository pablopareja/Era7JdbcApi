package com.era7.lib.era7jdbcapi;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * Class that manages everything related to connections with the DB system
 * @author Pablo Pareja Tobes
 *
 */
public class DBConnection {

    private static DataSource DS = null;
    private static Connection LOGIN_CONNECTION = null;
    private static InitialContext INIT_CONTEXT = null;
    /**
     * Url for the connection with the DB
     */
    public static String DATASOURCE_CONTEXT = "";
    /**
     *  <b>IMPORTANT!!!</b> <br>This flag indicates whether the application will use a session guide connection
     * 	system for retrieving connections with the DB or not.
     */
    public static boolean SESSION_GUIDED_CONNECTIONS_FLAG = true;
    /**
     * Counter indicating the number of active connections with the DB
     */
    public static int ACTIVE_DB_CONNECTIONS_COUNTER = 0;
    /**
     * Maximum number of concurrent active connections with the DB
     */
    public static int MAXIMUM_ACTIVE_DB_CONNECTIONS = 10;
    /**
     * Hash code for the LOGIN_CONNECTION object so that it can be easily identifiable doing a simple comparation
     */
    public static int LOGIN_CONNECTION_HASH_CODE;


    public static int LOGIN_CONNECTION_VALIDATION_TIMEOUT = 1;

    /**
     *	Init
     *
     * @throws SQLException
     * @throws NamingException
     */
    private static synchronized void initDataSource() {

        if (DS == null) {
            try {
                INIT_CONTEXT = new InitialContext();
                DS = (DataSource) INIT_CONTEXT.lookup(DATASOURCE_CONTEXT);

                System.out.println("Datasource initialized!");
            } catch (NamingException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Gets the general connection with the Data Base initializing it in case it was not
     * initialized yet.
     *
     * @return Connection object
     * @throws DataBaseException
     * @throws DataBaseException In the case where there was a problem getting the connection.
     */
    public synchronized static Connection getNewConnection() throws DataBaseException {
        if (DS == null) {
            initDataSource();
        }

        Connection conn = null;

        if (SESSION_GUIDED_CONNECTIONS_FLAG) {
            if (ACTIVE_DB_CONNECTIONS_COUNTER == MAXIMUM_ACTIVE_DB_CONNECTIONS) {
                return getLoginConnection();
            } else {
                try {
                    conn = DS.getConnection();
                    ACTIVE_DB_CONNECTIONS_COUNTER++;
                } catch (SQLException e) {
                    e.printStackTrace();
                    throw new DataBaseException(DataBaseException.PROBLEMS_GETTING_CONNECTION_TO_DB, e.getMessage());
                }
            }
        } else {
            try {
                conn = DS.getConnection();
            } catch (SQLException e) {
                e.printStackTrace();
                throw new DataBaseException(DataBaseException.PROBLEMS_GETTING_CONNECTION_TO_DB, e.getMessage());
            }
        }

        System.out.println("Active connections number: " + ACTIVE_DB_CONNECTIONS_COUNTER);


        return conn;
    }

    /**
     * Gets the LOGIN connection with the Data Base initializing it in case it was not
     * initialized yet.
     *
     * @return Connection object
     * @throws DataBaseException
     * @throws DataBaseException In the case where there was a problem getting the connection.
     */
    public synchronized static Connection getLoginConnection() throws DataBaseException {



        if (SESSION_GUIDED_CONNECTIONS_FLAG) {
            if (LOGIN_CONNECTION == null) {
                initDataSource();
                try {
                    LOGIN_CONNECTION = DS.getConnection();
                    LOGIN_CONNECTION_HASH_CODE = LOGIN_CONNECTION.hashCode();
                    System.out.println("Login connection created!");

                    //Incrementing the counter of active connections
                    ACTIVE_DB_CONNECTIONS_COUNTER++;
                } catch (SQLException e) {
                    e.printStackTrace();
                    throw new DataBaseException(DataBaseException.PROBLEMS_GETTING_CONNECTION_TO_DB, e.getMessage());
                }
            }else{
                boolean getTheConnectionAgain = false;
                try {
                    if (!LOGIN_CONNECTION.isValid(LOGIN_CONNECTION_VALIDATION_TIMEOUT)) {
                        getTheConnectionAgain = true;
                    }else{
                        System.out.println("LOGIN_CONNECTION is valid!");
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
                    getTheConnectionAgain = true;
                }
                if(getTheConnectionAgain){
                    initDataSource();
                    try {
                        System.out.println("The login connection was lost, getting a new login connection...");
                        LOGIN_CONNECTION = DS.getConnection();
                        LOGIN_CONNECTION_HASH_CODE = LOGIN_CONNECTION.hashCode();
                        System.out.println("Login connection created!");

                        //Incrementing the counter of active connections
                        ACTIVE_DB_CONNECTIONS_COUNTER++;
                    } catch (SQLException e) {
                        e.printStackTrace();
                        throw new DataBaseException(DataBaseException.PROBLEMS_GETTING_CONNECTION_TO_DB, e.getMessage());
                    }
                }
            }

            return LOGIN_CONNECTION;
        } else {
            throw new DataBaseException(DataBaseException.SESSION_DRIVEN_CONNECTION_SYSTEM_NEEDED, "");
        }

    }

    /**
     * Closes the connection provided
     * @param conn Connection to be closed
     * @throws DataBaseException
     */
    public static void closeConnection(Connection conn) throws DataBaseException {
        //int hashCode = conn.hashCode();
        try {
            conn.close();
            if(SESSION_GUIDED_CONNECTIONS_FLAG){
                DBConnection.ACTIVE_DB_CONNECTIONS_COUNTER--;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            //System.out.println(e.toString());
            throw new DataBaseException(DataBaseException.CLOSE_CONNECTION_ERROR, e.toString());
        }
    }
}
