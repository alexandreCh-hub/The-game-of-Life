package com.uca;

import com.uca.dao._Initializer;
import com.uca.gui.*;

import com.uca.core.GridCore;
import com.uca.entity.CellEntity;

import java.util.List;

import com.google.gson.Gson;

import static spark.Spark.*;
import spark.*;

public class StartServer {

    public static void main(String[] args) {
        //Configuration de Spark
        staticFiles.location("/static/");
        port(8081);

        // Création de la base de données, si besoin
        _Initializer.Init();

        /**
         * Définition des routes
         */
        //affiche les requetes dans le terminal
         before((req, res) -> {
            System.out.println(req.requestMethod() + " " + req.uri() + " " + req.protocol());
        });

        // index de l'application
        get("/", (req, res) -> {
                return IndexGUI.getIndex();
            });

        // retourne l'état de la grille
        get("/grid", (req, res) -> {
                res.type("application/json");
                return new Gson().toJson(GridCore.getGrid(getSession(req)));
            });

        // inverse l'état d'une cellule 
        put("/grid/change", (req, res) -> {
                Gson gson = new Gson();
                CellEntity selectedCell = (CellEntity) gson.fromJson(req.body(), CellEntity.class);
                return GridCore.changeState(getSession(req),selectedCell);
            });

        // sauvegarde les modifications de la grille 
        post("/grid/save", (req, res) -> {
                GridCore.save(getSession(req));
                return 200;
            });

        // annule les modifications de la grille 
        post("/grid/cancel", (req, res) -> {
                GridCore.cancel(getSession(req));
                return 200;
            });

        // charge un fichier rle depuis un URL
        put("/grid/rle", (req, res) -> {
                String RLEUrl = req.body();
                List<CellEntity> grid = GridCore.decodeRLEUrl(RLEUrl);
                GridCore.loadGrid(getSession(req),grid);
                return 200;
            });

        // vide la grille
        post("/grid/empty", (req, res) -> {
                GridCore.resetGrid(getSession(req));
                return 200;
            });

        // met à jour la grille en la remplaçant par la génération suivante
        post("/grid/next", (req, res) -> {
                GridCore.next(getSession(req));
                return 200;
            });

    }

    /**
     * retourne le numéro de session
     * il y a un numéro de session différent pour chaque onglet de navigateur
     * ouvert sur l'application
     */
    public static int getSession(Request req) {
        return Integer.parseInt(req.queryParams("session"));
    }
}
