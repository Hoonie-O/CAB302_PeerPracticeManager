package com.cab302.peerpractice.Model;

import java.util.List;

public interface IGroupDAO {
    boolean addGroup(Group group);
    boolean deleteGroup(Group group);
    boolean updateGroup(Group group);
    List<Group>searchByUser(User user);
    List<Group>searchByMembers(List<User> users);
    List<Group>searchByName(String name);
    boolean existsByName(String name);
    boolean existstByUser(User user);
    List<Group>getAllGroups();
}
