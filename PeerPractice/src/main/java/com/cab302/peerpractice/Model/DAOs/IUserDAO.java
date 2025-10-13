package com.cab302.peerpractice.Model.DAOs;

import com.cab302.peerpractice.Exceptions.DuplicateUsernameException;
import com.cab302.peerpractice.Exceptions.DuplicateEmailException;
import com.cab302.peerpractice.Model.Entities.Notification;
import com.cab302.peerpractice.Model.Entities.User;
import javafx.collections.ObservableList;
import java.sql.SQLException;

public interface IUserDAO {
    User findUser(String column, String value) throws SQLException;
    ObservableList<User> findUsers() throws SQLException;

    User findUserById(String userId) throws SQLException;
    boolean createUser(String username, String password, String firstName, String lastName, String email, String institution) throws SQLException, DuplicateUsernameException, DuplicateEmailException;
    boolean addNotification(User sentFrom, User receivedBy, String message) throws SQLException;
    boolean updateValue(String username, String column, String value) throws SQLException;
    boolean deleteUser(String userID) throws SQLException;
    
    // Additional methods for MockUserDAO compatibility
    boolean addUser(User user);
    boolean deleteUser(User user);
    boolean updateUser(User user);
    void update(User user);
    User searchByUsername(String username);
    java.util.List<User> searchByInstitution(String institution);
    java.util.Optional<User> getUserByEmail(String email);
    java.util.Optional<User> getUserByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    java.util.List<User> getAllUsers();
    boolean storePassword(User user, String hash);
    String getPassword(User user);
    boolean addNotification(User username, Notification notification);
    boolean removeNotification(User username, Notification notification);


}
