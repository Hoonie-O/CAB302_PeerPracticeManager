package com.cab302.peerpractice.Model.DAOs;

import com.cab302.peerpractice.Exceptions.DuplicateFriendException;
import com.cab302.peerpractice.Model.Entities.Friend;
import com.cab302.peerpractice.Model.Entities.User;

import java.sql.SQLException;
import java.util.List;

/**
 * <hr>
 * Data Access Object interface for managing friend relationships and social connections.
 *
 * <p>This interface defines the contract for friend relationship data operations,
 * providing methods to manage friendships, friend requests, and user blocking.
 *
 * <p> Key features include:
 * <ul>
 *   <li>Friend request management (send, accept, deny)</li>
 *   <li>User blocking and unblocking functionality</li>
 *   <li>Friendship status tracking and retrieval</li>
 *   <li>Observable collections for UI integration</li>
 * </ul>
 *
 * @see Friend
 * @see User
 * @see FriendDAO
 * @see DuplicateFriendException
 */
public interface IFriendDAO {
    /**
     * <hr>
     * Retrieves all friend relationships for a specific user.
     *
     * <p>Fetches the complete list of friends, friend requests, and blocked users
     * associated with the specified user, ordered by relationship status.
     *
     * @param user the user whose friends are being retrieved
     * @return an ObservableList of Friend objects representing all relationships
     * @throws SQLException if a database access error occurs
     */
    javafx.collections.ObservableList<Friend> getFriends(User user) throws SQLException;

    /**
     * <hr>
     * Sends a friend request from one user to another.
     *
     * <p>Creates a new friend relationship with 'pending' status. Checks for
     * duplicate requests and prevents self-friending. Also verifies that
     * neither user has blocked the other before allowing the request.
     *
     * @param user the user sending the friend request
     * @param friend the user receiving the friend request
     * @return true if the friend request was created successfully, false otherwise
     * @throws SQLException if a database access error occurs
     * @throws DuplicateFriendException if a friend relationship already exists
     */
    boolean addFriend(User user, User friend) throws SQLException, DuplicateFriendException;

    /**
     * <hr>
     * Removes a friend relationship between two users.
     *
     * <p>Deletes the friendship record from persistent storage, effectively
     * unfriending the users. This operation is bidirectional and affects
     * both users' friend lists.
     *
     * @param user the user initiating the unfriend action
     * @param friend the user being unfriended
     * @return true if the friendship was successfully removed, false otherwise
     * @throws SQLException if a database access error occurs
     */
    boolean removeFriend(User user, User friend) throws SQLException;

    /**
     * <hr>
     * Accepts a pending friend request.
     *
     * <p>Updates the friend request status to 'accepted' and creates a
     * reciprocal friendship record to ensure both users see each other
     * as friends in their respective friend lists.
     *
     * @param user the user accepting the friend request
     * @param friend the user whose request is being accepted
     * @return true if the friend request was successfully accepted, false otherwise
     * @throws SQLException if a database access error occurs
     */
    boolean acceptFriendRequest(User user, User friend) throws SQLException;

    /**
     * <hr>
     * Denies a pending friend request.
     *
     * <p>Updates the friend request status to 'denied', effectively
     * rejecting the friend request without creating a friendship.
     *
     * @param user the user denying the friend request
     * @param friend the user whose request is being denied
     * @return true if the friend request was successfully denied, false otherwise
     * @throws SQLException if a database access error occurs
     */
    boolean denyFriendRequest(User user, User friend) throws SQLException;

    /**
     * <hr>
     * Blocks a user, preventing future friend requests and interactions.
     *
     * <p>Updates the friend relationship status to 'blocked'. Blocked users
     * cannot send friend requests or messages to the blocking user.
     *
     * @param user the user initiating the block action
     * @param friend the user being blocked
     * @return true if the user was successfully blocked, false otherwise
     * @throws SQLException if a database access error occurs
     */
    boolean blockUser(User user, User friend) throws SQLException;

    /**
     * <hr>
     * Unblocks a previously blocked user.
     *
     * <p>Removes the blocked status by deleting the friend relationship record,
     * allowing the previously blocked user to send friend requests again.
     *
     * @param user the user initiating the unblock action
     * @param friend the user being unblocked
     * @return true if the user was successfully unblocked, false otherwise
     * @throws SQLException if a database access error occurs
     */
    boolean unblockUser(User user, User friend) throws SQLException;

    /**
     * <hr>
     * Retrieves all users blocked by a specific user.
     *
     * <p>Fetches the list of users that the specified user has blocked,
     * ordered by the blocked user's username.
     *
     * @param user the user whose blocked list is being retrieved
     * @return an ObservableList of Friend objects with 'blocked' status
     * @throws SQLException if a database access error occurs
     */
    javafx.collections.ObservableList<Friend> getBlockedUsers(User user) throws SQLException;

    /**
     * <hr>
     * Checks if one user has blocked another user.
     *
     * <p>Verifies whether a blocking relationship exists between the two users
     * in the specified direction (user has blocked friend).
     *
     * @param user the potential blocking user
     * @param friend the potentially blocked user
     * @return true if user has blocked friend, false otherwise
     * @throws SQLException if a database access error occurs
     */
    boolean isBlocked(User user, User friend) throws SQLException;
}