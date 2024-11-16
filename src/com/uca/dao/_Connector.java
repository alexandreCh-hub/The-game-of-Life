package com.uca.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

public class _Connector {

    private static String url = "jdbc:postgresql://localhost/life";
    private static String user = "yoboyer";
    private static String passwd = "testWeb43";

    private static Connection connect;
    private static Map<Integer,Connection> connection_list = new HashMap<>();

    public static Connection getMainConnection(){
        if(connect == null){
            connect = getNewConnection();
        }
        return connect;
    }

    public static Connection getConnection(int session){
        if(!connection_list.containsKey(session)){
            Connection c = getNewConnection();
            connection_list.put(session, c);
            return c;
        }

        return connection_list.get(session);
    }

    private static Connection getNewConnection() {
        Connection c;
        try {
            c = DriverManager.getConnection(url, user, passwd);
            c.setAutoCommit(false);
            c.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        } catch (SQLException e) {
            System.err.println("Erreur en ouvrant une nouvelle connection.");
            throw new RuntimeException(e);
        }
        return c;
    }
}
