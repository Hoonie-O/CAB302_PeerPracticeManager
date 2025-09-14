package com.cab302.peerpractice.Model;

import com.cab302.peerpractice.Exceptions.DuplicateUsernameException;
import com.cab302.peerpractice.Exceptions.DuplicateEmailException;
import javafx.collections.ObservableList;
import java.sql.SQLException;

public interface IUserDAO {
    User findUser(String column, String value) throws SQLException;
    ObservableList<User> findUsers() throws SQLException;
    boolean createUser(String username, String password, String firstName, String lastName, String email, String institution) throws SQLException, DuplicateUsernameException, DuplicateEmailException;
    boolean updateValue(String username, String column, String value) throws SQLException;
    boolean deleteUser(String userID) throws SQLException;
}
