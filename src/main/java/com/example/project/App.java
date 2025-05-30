package com.example.project;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        Parent root = loadFXML("login");
        scene = new Scene(root);

        String cssPath = "/com/example/project/css/style.css";
        URL cssUrl = App.class.getResource(cssPath);

        if (cssUrl == null) {
            System.err.println("Peringatan: File CSS tidak ditemukan di: " + cssPath);
        } else {
            scene.getStylesheets().add(cssUrl.toExternalForm());
            System.out.println("CSS (" + cssPath + ") berhasil ditambahkan ke scene awal.");
        }

        stage.setScene(scene);
        stage.setTitle("Task Manager");
        stage.show();
    }

    public static void setRoot(String fxml) throws IOException {
        Parent newRoot = loadFXML(fxml);
        if (scene != null && newRoot != null) {
            scene.setRoot(newRoot);
        } else {
            System.err.println("Gagal mengubah root: scene atau FXML root baru adalah null.");
        }
    }

    private static Parent loadFXML(String fxml) throws IOException {
        String path = "/com/example/project/fxml/" + fxml + ".fxml";
        URL fxmlLocation = App.class.getResource(path);
        System.out.println("Mencoba memuat FXML: " + path);
        System.out.println("URL FXML yang ditemukan: " + fxmlLocation);

        if (fxmlLocation == null) {
            throw new IOException("File FXML tidak ditemukan pada path: " + path + ". Pastikan path benar dan file ada di direktori resources yang ter-build.");
        }

        FXMLLoader loader = new FXMLLoader(fxmlLocation);
        return loader.load();
    }

    public static void main(String[] args) {
        launch(args);
    }
}