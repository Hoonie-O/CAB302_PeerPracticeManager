package com.cab302.peerpractice.Model.daos;

import com.cab302.peerpractice.Exceptions.DuplicateFriendException;
import com.cab302.peerpractice.Model.entities.Friend;
import com.cab302.peerpractice.Model.entities.User;

import java.sql.SQLException;
import java.util.List;

public interface IFriendDAO {
    List<Friend> getFriends(User user) throws SQLException;
    boolean addFriend(User user, User friend) throws SQLException, DuplicateFriendException;
    boolean removeFriend(User user, User friend) throws SQLException;
    boolean acceptFriendRequest(User user, User friend) throws SQLException;
    boolean denyFriendRequest(User user, User friend) throws SQLException;
    boolean blockUser(User user, User friend) throws SQLException;
}
