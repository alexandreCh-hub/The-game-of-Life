package com.uca.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.uca.entity.CellEntity;

public class GridDAO {

    public Connection connect;

    public GridDAO(int session) {
        this.connect = _Connector.getConnection(session);
    }

    public int changeState(CellEntity cell) {
        try {
            // recupere l'etat de la case
            PreparedStatement statement = this.connect
                    .prepareStatement("SELECT state FROM plateau WHERE x=? AND y=?");
            statement.setInt(1, cell.getX());
            statement.setInt(2, cell.getY());
            ResultSet result = statement.executeQuery();
            int state = -1;
            if (!result.next()) {
                throw new SQLException("La case n'est pas dans le tableau");
            }

            state = result.getInt("state");
            // change l'etat de la case
            statement = this.connect.prepareStatement("UPDATE plateau SET state = ? WHERE x=? AND y=?");
            // inverse l'etat
            if (state == 0) {
                statement.setInt(1, 1);
            } else {
                statement.setInt(1, 0);
            }
            statement.setInt(2, cell.getX());
            statement.setInt(3, cell.getY());
            statement.executeUpdate();

            return (state == 0) ? 1 : 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public void save(){
        try{
            this.connect.commit();
            this.connect.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    public void cancel(){
        try{
            this.connect.rollback();
            this.connect.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    public List<CellEntity> getCells() {
        List<CellEntity> grid = new ArrayList<>();
        try {
            PreparedStatement statement = this.connect.prepareStatement("SELECT x,y FROM plateau WHERE state=1");
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                CellEntity cell = new CellEntity(result.getInt("x"), result.getInt("y"));
                grid.add(cell);
            }
            return grid;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void resetCells() {
        List<CellEntity> grid = getCells();
        for (CellEntity cell : grid) {
            changeState(cell);
        }
    }

    public void loadGrid(List<CellEntity> grid){
        resetCells();
        try{
            for(CellEntity cell : grid){
                PreparedStatement statement = this.connect.prepareStatement("UPDATE plateau SET state=1 where x=? and y=?");
                System.out.println(cell.getX() +" "+ cell.getY());
                statement.setInt(1, cell.getX());
                statement.setInt(2, cell.getY());
                statement.executeUpdate();
            }
        } catch(SQLException e){
            e.printStackTrace();
        }
    }

    public void nextGrid() {
        try {
            // this.connect.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            // change l'état des cellules mortes qui ont 3 cellules voisines vivantes
            // change l'état des cellules vivantes qui ont moins de 2 ou plus de 3 cellules
            // voisines vivantes
            // conserve l'état des autres cellules
            PreparedStatement statement = this.connect.prepareStatement(
                    "UPDATE plateau SET state = " +
                            "CASE " +
                            "WHEN state = 0 AND " +
                            "(SELECT COUNT(*) FROM plateau alive " +
                            "WHERE alive.state = 1 AND " +
                            "(alive.x = plateau.x - 1 OR alive.x = plateau.x OR alive.x = plateau.x + 1) AND " +
                            "(alive.y = plateau.y - 1 OR alive.y = plateau.y OR alive.y = plateau.y + 1)) = 3 " +
                            "THEN 1 " +
                            "WHEN state = 1 AND " +
                            "(SELECT COUNT(*) FROM plateau alive " +
                            "WHERE alive.state = 1 AND " +
                            "(alive.x = plateau.x - 1 OR alive.x = plateau.x OR alive.x = plateau.x + 1) AND " +
                            "(alive.y = plateau.y - 1 OR alive.y = plateau.y OR alive.y = plateau.y + 1) AND " +
                            "NOT (alive.x = plateau.x AND alive.y = plateau.y)) NOT IN (2, 3) " +
                            "THEN 0 " +
                            "ELSE state " +
                            "END");
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
