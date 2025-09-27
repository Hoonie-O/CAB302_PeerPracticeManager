package com.cab302.peerpractice.Model;

import com.cab302.peerpractice.Exceptions.DuplicateUsernameException;
import com.cab302.peerpractice.Exceptions.DuplicateEmailException;
import javafx.collections.ObservableList;
import java.sql.SQLException;

public interface IUserDAO {
    User findUser(String column, String value) throws SQLException;
    ObservableList<User> findUsers() throws SQLException;

    User findUserById(String userId) throws SQLException;
    boolean createUser(String username, String password, String firstName, String lastName, String email, String institution) throws SQLException, DuplicateUsernameException, DuplicateEmailException;
    boolean addNotification(String sentFrom, String receivedBy, String message) throws SQLException;
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
    boolean addNotification(String username, Notification notification);
    boolean removeNotification(String username, Notification notification);


}
