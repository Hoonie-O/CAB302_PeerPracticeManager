package com.cab302.peerpractice.Model;

import javafx.collections.ObservableList;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface IUserDAO {
    User findUser(String userID) throws ClassNotFoundException, SQLException;
    ObservableList<User> findUsers() throws ClassNotFoundException, SQLException;
    void createUser(String username, String password, String firstName, String email, String institution) throws ClassNotFoundException, SQLException;
    void updateUsername(String userID, String username) throws ClassNotFoundException, SQLException;
    void deleteUser(String userID) throws ClassNotFoundException, SQLException;
}
