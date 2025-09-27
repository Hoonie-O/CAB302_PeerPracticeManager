package com.cab302.peerpractice.Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class MockGroupDAO implements IGroupDAO {
    private final List<Group> groups = new ArrayList<>();
    private int idCounter = 1;

    @Override
    public int addGroup(Group group) {
        if (groupExists(group)) {
            return -1; // duplicate
        }
        group.setID(idCounter++);
        groups.add(group);
        return group.getID();
    }

    @Override
    public boolean deleteGroup(Group group) {
        return groups.removeIf(g -> g.getID() == group.getID());
    }

    @Override
    public boolean updateGroup(Group group) {
        for (int i = 0; i < groups.size(); i++) {
            if (groups.get(i).getID() == group.getID()) {
                groups.set(i, group);
                return true;
            }
        }
        return false;
    }

    @Override
    public List<Group> searchByUser(User user) {
        return groups.stream()
                .filter(g -> g.getMembers() != null && g.getMembers().contains(user))
                .collect(Collectors.toList());
    }

    @Override
    public List<Group> searchByMembers(List<User> users) {
        return groups.stream()
                .filter(g -> g.getMembers() != null && new HashSet<>(g.getMembers()).containsAll(users))
                .collect(Collectors.toList());
    }

    @Override
    public List<Group> searchByName(String name) {
        return groups.stream()
                .filter(g -> g.getName().equalsIgnoreCase(name))
                .collect(Collectors.toList());
    }

    @Override
    public Group searchByID(int id) {
        for(Group g : groups){
            if(g.getID() == id){
                return g;
            }
        }
        return null;
    }

    @Override
    public boolean existsByName(String name) {
        return groups.stream()
                .anyMatch(g -> g.getName().equalsIgnoreCase(name));
    }

    @Override
    public boolean existstByUser(User user) {
        return groups.stream()
                .anyMatch(g -> g.getMembers() != null && g.getMembers().contains(user));
    }

    @Override
    public List<Group> getAllGroups() {
        return new ArrayList<>(groups);
    }

    @Override
    public boolean setRequireApproval(int id, boolean require_approval) {
        for (Group g : groups) {
            if (g.getID() == id) {
                g.setRequire_approval(require_approval);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean addToGroup(int id, User user) {
        for (Group g : groups) {
            if (g.getID() == id) {
                if (g.getMembers() == null) {
                    g.setMembers(new ArrayList<>());
                }
                if (!g.getMembers().contains(user)) {
                    g.addMember(user);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean groupExists(Group group) {
        return groups.stream()
                .anyMatch(g -> g.getName().equalsIgnoreCase(group.getName()));
    }
}
