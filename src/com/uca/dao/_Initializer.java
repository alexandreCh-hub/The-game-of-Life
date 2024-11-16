package com.uca.dao;

import java.sql.*;

public class _Initializer {
    // nom de la table contenant la grille
    final static String TABLE = "plateau";
    // taille de grille
    final static int SIZE = 1000;

    /**
     * cette méthode permet d'initialiser en créant une table pour la grille si elle n'existe pas
     */
    public static void Init(){

        Connection connection = _Connector.getMainConnection();
        PreparedStatement statement;
        try{
            if(!tableExists(connection, TABLE)){
                //créé la table
                statement = connection.prepareStatement("CREATE TABLE plateau (x INT, y INT, state INT, PRIMARY KEY (x,y))");
                statement.executeUpdate();

                //rempli la table
                for(int i=0;i<100;++i){
                    for(int j=0;j<100;++j){
                        statement = connection.prepareStatement("INSERT INTO plateau VALUES (?,?,0)");
                        statement.setInt(1, i);
                        statement.setInt(2, j);
                        statement.executeUpdate();
                    }
                }
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    /**
     * teste si une table existe dans la base de données 
     */
    private static boolean tableExists(Connection connection, String tableName) throws SQLException {
        DatabaseMetaData meta = connection.getMetaData();
        ResultSet resultSet = meta.getTables(null, null, tableName, new String[]{"TABLE"});
        return resultSet.next();
    }

    // suppression de la table pour les tests
    private static void drop() {
        try {
            Connection connection = _Connector.getMainConnection();
            PreparedStatement statement;

            statement = connection.prepareStatement("DROP TABLE plateau");
            statement.executeUpdate();
        } catch(Exception e) {
            throw new RuntimeException("could not drop database");
        }
    }
}