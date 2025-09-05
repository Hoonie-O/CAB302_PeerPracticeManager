package com.cab302.peerpractice.Model;

import javafx.beans.property.*;

public class User {
    private final IntegerProperty userID;
    private final StringProperty username;
    private final StringProperty password;
    private final StringProperty firstName;
    private final StringProperty lastName;
    private final StringProperty email;
    private final StringProperty institution;
    private final StringProperty biography;
    private final SimpleObjectProperty<Event> events;

    public User() {
        this.userID = new SimpleIntegerProperty();
        this.username = new SimpleStringProperty();
        this.password = new SimpleStringProperty();
        this.firstName = new SimpleStringProperty();
        this.lastName = new SimpleStringProperty();
        this.email = new SimpleStringProperty();
        this.institution = new SimpleStringProperty();
        this.biography = new SimpleStringProperty();
        this.events = new SimpleObjectProperty<>();
    }

    public int getUserID() {
        return userID.get();
    }
    public void setUserID(int userID) {
        this.userID.set(userID);
    }
    public IntegerProperty userIDProperty() {
        return userID;
    }

    public String getUsername() {
        return username.get();
    }
    public void setUsername(String username) {
        this.username.set(username);
    }
    public StringProperty usernameProperty() {
        return username;
    }

    public String getPassword() {
        return password.get();
    }
    public void setPassword(String password) {
        this.password.set(password);
    }
    public StringProperty passwordProperty() {
        return password;
    }

    public String getFirstName() {
        return firstName.get();
    }
    public void setFirstName(String firstName) {
        this.firstName.set(firstName);
    }
    public StringProperty firstNameProperty() {
        return firstName;
    }

    public String getLastName() {
        return lastName.get();
    }
    public void setLastName(String lastName) {
        this.lastName.set(lastName);
    }
    public StringProperty lastNameProperty() {
        return lastName;
    }

    public String getEmail() {
        return email.get();
    }
    public void setEmail(String email) {
        this.email.set(email);
    }
    public StringProperty emailProperty() {
        return email;
    }

    public String getInstitution() {
        return institution.get();
    }
    public void setInstitution(String institution) {
        this.institution.set(institution);
    }
    public StringProperty institutionProperty() {
        return institution;
    }

    public String getBiography() {
        return biography.get();
    }
    public void setBiography(String biography) {
        this.biography.set(biography);
    }
    public StringProperty biographyProperty() {
        return biography;
    }

    public Object getEvents() {
        return events.get();
    }
    public void setEvents(Event events) {
        this.events.set(events);
    }
    public SimpleObjectProperty<Event> eventsProperty() {
        return events;
    }
}