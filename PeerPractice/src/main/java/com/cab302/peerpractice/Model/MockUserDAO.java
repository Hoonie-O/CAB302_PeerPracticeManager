package com.cab302.peerpractice.Model;

import com.cab302.peerpractice.Exceptions.DuplicateEmailException;
import com.cab302.peerpractice.Exceptions.DuplicateUsernameException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class MockUserDAO implements IUserDAO{

    private final ObservableList<User> users = FXCollections.observableArrayList();

    public MockUserDAO(){
        BcryptHasher hasher = new BcryptHasher();
        String hash = hasher.hasher("password");
        User user = new User("Seiji","Sato","username","email@email.com",hash,"qut");
    }

    @Override
    public User findUser(String column, String value) throws SQLException {
        for (User u : users) {
            switch (column.toLowerCase()) {
                case "firstname":
                case "first_name":
                    if (u.getFirstName().equalsIgnoreCase(value)) return u;
                    break;
                case "lastname":
                case "last_name":
                    if (u.getLastName().equalsIgnoreCase(value)) return u;
                    break;
                case "username":
                    if (u.getUsername().equalsIgnoreCase(value)) return u;
                    break;
                case "email":
                    if (u.getEmail().equalsIgnoreCase(value)) return u;
                    break;
                case "passwordhash":
                case "password_hash":
                    if (u.getPassword().equals(value)) return u; // probably case-sensitive
                    break;
                case "institution":
                    if (u.getInstitution().equalsIgnoreCase(value)) return u;
                    break;
                default:
                    throw new SQLException("Invalid column: " + column);
            }
        }
        return null; // no match found
    }

    @Override
    public ObservableList<User> findUsers() throws SQLException {
        return users;
    }

    @Override
    public boolean createUser(String username, String password, String firstName, String lastName, String email, String institution) throws SQLException, DuplicateUsernameException, DuplicateEmailException {
        User newUser = new User(firstName,lastName,username,email,password,institution);
        users.add(newUser);
        return true;
    }

    @Override
    public boolean addNotification(String sentFrom, String receivedBy, String message) throws SQLException {
        return true;
    }

    @Override
    public boolean updateValue(String username, String column, String value) throws SQLException {
        for (User u : users) {
            if (u.getUsername().equalsIgnoreCase(username)) {
                switch (column.toLowerCase()) {
                    case "firstname":
                        u.setFirstName(value);
                        return true;
                    case "lastname":
                        u.setLastName(value);
                        return true;
                    case "username":
                        u.setUsername(value);
                        return true;
                    case "email":
                        u.setEmail(value);
                        return true;
                    case "passwordhash":
                        u.setPassword(value);
                        return true;
                    case "institution":
                        u.setInstitution(value);
                        return true;
                    default:
                        throw new SQLException("Invalid column: " + column);
                }
            }
        }
        return false; // user not found
    }

    @Override
    public boolean deleteUser(String userID) throws SQLException {
        return users.removeIf(u -> u.getUsername().equalsIgnoreCase(userID) ||
                u.getEmail().equalsIgnoreCase(userID));
    }
}
