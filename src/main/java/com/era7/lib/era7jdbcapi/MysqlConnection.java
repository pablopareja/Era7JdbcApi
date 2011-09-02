package com.era7.lib.era7jdbcapi;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;

/**
 * Class that manages everything related to connections with the Mysql DB system
 * @author Pablo Pareja Tobes
 *
 */
public class MysqlConnection {

    private static Driver DRIVER = null;
    private static Connection LOGIN_CONNECTION = null;
    /**
     * Url for the connection with the DB
     */
    public static String URL = "";
    /**
     *  Username for the connection with the DB
     */
    public static String USERNAME = "";
    /**
     *  Password for the connection with the DB
     */
    public static String PASSWORD = "";
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
    private static synchronized void initDriver() {

        if (DRIVER == null) {
            try {
                Driver d = (Driver) Class.forName("com.mysql.jdbc.Driver").newInstance();
                System.out.println("Mysql driver initialized!");
                
            } catch (InstantiationException ex) {
                Logger.getLogger(MysqlConnection.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(MysqlConnection.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(MysqlConnection.class.getName()).log(Level.SEVERE, null, ex);
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
        if (DRIVER == null) {
            initDriver();
        }

        System.out.println("MysqlConnection: getNewConnection()");

        Connection conn = null;

        if (SESSION_GUIDED_CONNECTIONS_FLAG) {
            System.out.println("SESSION_GUIDED_CONNECTIONS_FLAG es true");

            if (ACTIVE_DB_CONNECTIONS_COUNTER == MAXIMUM_ACTIVE_DB_CONNECTIONS) {
                return getLoginConnection();
            } else {
                try {
                    conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                    ACTIVE_DB_CONNECTIONS_COUNTER++;
                } catch (SQLException e) {
                    e.printStackTrace();
                    throw new DataBaseException(DataBaseException.PROBLEMS_GETTING_CONNECTION_TO_DB, e.getMessage());
                }
            }
        } else {
            System.out.println("SESSION_GUIDED_CONNECTIONS_FLAG es false");
            try {
                conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
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

//        if (SESSION_GUIDED_CONNECTIONS_FLAG) {
//            if (LOGIN_CONNECTION == null) {
//
//                try {
//                    LOGIN_CONNECTION =
//                    LOGIN_CONNECTION_HASH_CODE = LOGIN_CONNECTION.hashCode();
//                    System.out.println("Login connection created!");
//
//                    //Incrementing the counter of active connections
//                    ACTIVE_DB_CONNECTIONS_COUNTER++;
//
//                    System.out.println("Active connections number: " + ACTIVE_DB_CONNECTIONS_COUNTER);
//
//                } catch (SQLException e) {
//                    e.printStackTrace();
//                    throw new DataBaseException(DataBaseException.PROBLEMS_GETTING_CONNECTION_TO_DB, e.getMessage());
//                }
//            }
//
//            return LOGIN_CONNECTION;
//        } else {
//            throw new DataBaseException(DataBaseException.SESSION_DRIVEN_CONNECTION_SYSTEM_NEEDED, "");
//        }

        if (SESSION_GUIDED_CONNECTIONS_FLAG) {
            if (LOGIN_CONNECTION == null) {
                initDriver();
                try {
                    LOGIN_CONNECTION = DriverManager.getConnection(URL, USERNAME, PASSWORD);
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
                    initDriver();
                    try {
                        System.out.println("The login connection was lost, getting a new login connection...");
                        LOGIN_CONNECTION = DriverManager.getConnection(URL, USERNAME, PASSWORD);
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
                ACTIVE_DB_CONNECTIONS_COUNTER--;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            //System.out.println(e.toString());
            throw new DataBaseException(DataBaseException.CLOSE_CONNECTION_ERROR, e.toString());
        }
    }
}
