module com.cab302.peerpractice {
    // --- JavaFX ---
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.graphics;

    // --- External UI libs ---
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;

    // --- Java platform ---
    requires java.prefs;
    requires java.desktop;
    requires java.sql;
    requires java.sql.rowset;
    requires java.management;

    // --- External dependencies ---
    requires jakarta.mail;
    requires jbcrypt;
    requires org.commonmark;
    requires org.slf4j;

    // --- Core application ---
    exports com.cab302.peerpractice;
    opens com.cab302.peerpractice to javafx.fxml;

    // --- Controllers ---
    exports com.cab302.peerpractice.Controllers;
    opens com.cab302.peerpractice.Controllers to javafx.fxml;

    // --- Exceptions ---
    exports com.cab302.peerpractice.Exceptions;

    // --- Utilities ---
    exports com.cab302.peerpractice.Model.Utils;
    opens com.cab302.peerpractice.Model.Utils to javafx.fxml;

    // --- DAOs ---
    exports com.cab302.peerpractice.Model.DAOs;
    opens com.cab302.peerpractice.Model.DAOs to javafx.fxml;

    // --- Managers ---
    exports com.cab302.peerpractice.Model.Managers;
    opens com.cab302.peerpractice.Model.Managers to javafx.fxml;

    // --- Entities ---
    exports com.cab302.peerpractice.Model.Entities;
    opens com.cab302.peerpractice.Model.Entities to javafx.fxml;
}
