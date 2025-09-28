package com.cab302.peerpractice.Model.daos;

import com.cab302.peerpractice.Exceptions.DuplicateFriendException;
import com.cab302.peerpractice.Model.entities.Friend;
import com.cab302.peerpractice.Model.entities.FriendStatus;
import com.cab302.peerpractice.Model.entities.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mock (in-memory) implementation of IFriendDAO for unit testing without DB.
 */
public class MockFriendDAO implements IFriendDAO {

    // Key = "user:friend", Value = FriendStatus
    private final Map<String, FriendStatus> friendRelations = new ConcurrentHashMap<>();
    private final IUserDAO userDAO;

    public MockFriendDAO(IUserDAO userDAO) {
        this.userDAO = userDAO;
    }

    private String key(User user, User friend) {
        return user.getUsername() + ":" + friend.getUsername();
    }

    // -------------------- READ --------------------
    @Override
    public ObservableList<Friend> getFriends(User user) {
        ObservableList<Friend> list = FXCollections.observableArrayList();

        friendRelations.forEach((k, status) -> {
            String[] parts = k.split(":");
            if (parts[0].equals(user.getUsername())) {
                User u1 = null;
                try {
                    u1 = userDAO.findUser("username", parts[0]);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                User u2 = null;
                try {
                    u2 = userDAO.findUser("username", parts[1]);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                if (u1 != null && u2 != null) {
                    list.add(new Friend(u1, u2, status));
                }
            }
        });

        return list;
    }

    // -------------------- CREATE --------------------
    @Override
    public boolean addFriend(User user, User friend) throws SQLException, DuplicateFriendException {
        if (Objects.equals(user.getUsername(), friend.getUsername())) return false;

        if (friendExists(user, friend)) {
            if (acceptFriendRequest(user, friend)) return true;
            throw new DuplicateFriendException("Friend already exists");
        }

        friendRelations.put(key(user, friend), FriendStatus.PENDING);
        return true;
    }

    // -------------------- UPDATE --------------------
    @Override
    public boolean acceptFriendRequest(User user, User friend) {
        return updateFriendStatus(user, friend, FriendStatus.ACCEPTED);
    }

    @Override
    public boolean denyFriendRequest(User user, User friend) {
        return updateFriendStatus(user, friend, FriendStatus.DENIED);
    }

    @Override
    public boolean blockUser(User user, User friend) {
        return updateFriendStatus(user, friend, FriendStatus.BLOCKED);
    }

    private boolean updateFriendStatus(User user, User friend, FriendStatus status) {
        String k = key(user, friend);
        if (friendRelations.containsKey(k)) {
            friendRelations.put(k, status);
            return true;
        }
        return false;
    }

    // -------------------- DELETE --------------------
    @Override
    public boolean removeFriend(User user, User friend) {
        return friendRelations.remove(key(user, friend)) != null;
    }

    // -------------------- HELPERS --------------------
    private boolean friendExists(User user, User friend) {
        return friendRelations.containsKey(key(user, friend));
    }
}
