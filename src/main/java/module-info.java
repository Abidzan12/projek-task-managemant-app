module com.example.project {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires java.sql;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires org.kordamp.ikonli.fontawesome5;

    opens com.example.project.fxml to javafx.fxml;
    opens com.example.project.controller to javafx.fxml;
    opens com.example.project.model to javafx.base, javafx.fxml;
    opens com.example.project to javafx.fxml, javafx.graphics;

    exports com.example.project;

}