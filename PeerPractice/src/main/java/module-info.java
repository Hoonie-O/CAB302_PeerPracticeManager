module com.cab302.peerpractice {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires java.prefs;
    requires java.desktop;
    requires javafx.graphics;
    requires java.sql;
    requires java.sql.rowset;
    requires jakarta.mail;
    requires jbcrypt;
    requires java.management;


    opens com.cab302.peerpractice to javafx.fxml;
    exports com.cab302.peerpractice;
    exports com.cab302.peerpractice.Controllers;
    exports com.cab302.peerpractice.Model;
    exports com.cab302.peerpractice.Exceptions;
    opens com.cab302.peerpractice.Controllers to javafx.fxml;
    opens com.cab302.peerpractice.Model to javafx.fxml;
    exports com.cab302.peerpractice.Utilities;
    opens com.cab302.peerpractice.Utilities to javafx.fxml;
}