package com.cab302.peerpractice.Model.DAOs;

import com.cab302.peerpractice.Exceptions.DuplicateFriendException;
import com.cab302.peerpractice.Model.Entities.Friend;
import com.cab302.peerpractice.Model.Entities.User;

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
