package com.cab302.peerpractice.Model;

import eu.hansolo.toolbox.time.Times;
import javafx.beans.property.*;

import java.sql.Time;
import java.sql.Timestamp;

public class Event {
    private final StringProperty title;
    private final StringProperty description;
    private final StringProperty colourLabel;
    private final SimpleObjectProperty<Timestamp> startTime;
    private final SimpleObjectProperty<Timestamp> endTime;

    public Event() {
        this.title = new SimpleStringProperty();
        this.description = new SimpleStringProperty();
        this.colourLabel = new SimpleStringProperty();
        this.startTime = new SimpleObjectProperty<>();
        this.endTime = new SimpleObjectProperty<>();
    }

    public String getTitle() {
        return title.get();
    }
    public void setTitle (String title) {
        this.title.set(title);
    }
    public StringProperty titleProperty() {
        return title;
    }

    public String getDescription() {
        return description.get();
    }
    public void setDescription (String description) {
        this.description.set(description);
    }
    public StringProperty descriptionProperty() {
        return description;
    }

    public String getColourLabel() {
        return colourLabel.get();
    }
    public void setColourLabel (String colourLabel) {
        this.colourLabel.set(colourLabel);
    }
    public StringProperty colourLabelProperty() {
        return colourLabel;
    }

    public Timestamp getStartTime() {
        return startTime.get();
    }
    public void setStartTime(Timestamp startTime) {
        this.startTime.set(startTime);
    }
    public SimpleObjectProperty<Timestamp> startTimeProperty() {
        return startTime;
    }

    public Timestamp getEndTime() {
        return endTime.get();
    }
    public void setEndTime(Timestamp endTime) {
        this.endTime.set(endTime);
    }
    public SimpleObjectProperty<Timestamp> endTimeProperty() {
        return endTime;
    }
}