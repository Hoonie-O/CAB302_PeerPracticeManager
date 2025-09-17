package com.cab302.peerpractice.Model;

import com.cab302.peerpractice.Exceptions.DuplicateFriendException;

import java.sql.SQLException;
import java.util.List;

public interface IFriendDAO {
    List<Friend> getFriends(User user) throws SQLException;
    boolean addFriend(User user, User friend) throws SQLException, DuplicateFriendException;
    boolean checkFriendExists(User user, User friend) throws SQLException;
    void removeFriend(User user, User friend) throws SQLException;
    boolean blockUser(User user, User friend);
    boolean acceptFriendRequest(User user, User friend) throws SQLException;
    boolean denyFriendRequest(User user, User friend) throws SQLException;
}
