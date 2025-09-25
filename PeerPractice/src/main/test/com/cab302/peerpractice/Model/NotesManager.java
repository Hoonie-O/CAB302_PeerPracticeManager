package com.cab302.peerpractice.Model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

public class NotesManager {

    private Group group;
    private User user;
    private NotesManager notesManager;
    private INoteDAO noteDAO;

    @BeforeEach
    public void setUp(){
        user = new User("Seiji","Sato","sati2030","email@email.com","12321424121","QUT");
        group = new Group("Name","Descripton",false,user.getUsername(), LocalDateTime.now());
        notesManager = new NotesManager();
        noteDAO = new MockNoteDAO();
    }

    @Test
    public void TestAddNote(){

    }


}
