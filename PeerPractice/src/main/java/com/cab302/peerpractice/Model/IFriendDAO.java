package com.cab302.peerpractice.Model;

import java.sql.SQLException;
import java.util.List;

public interface IFriendDAO {
    List<Friend> getFriends(User user) throws SQLException;
    boolean addFriend(User user, User friend);
    boolean removeFriend(User user, User friend);
    boolean blockUser(User user, User friend);
    boolean acceptFriendRequest();
    boolean denyFriendRequest();
}
