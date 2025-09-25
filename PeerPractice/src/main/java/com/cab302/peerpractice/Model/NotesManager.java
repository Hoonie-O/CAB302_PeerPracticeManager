package com.cab302.peerpractice.Model;

import java.util.List;

public class NotesManager {

    INotesDAO notesDAO;
    IGroupDAO groupDAO;

    public NotesManager(INotesDAO notesDAO, IGroupDAO groupDAO){
        this.groupDAO = groupDAO;
        this.notesDAO = notesDAO;
    }

    public void addNote(String name, int groupID){
        Group group = groupDAO.searchByID(groupID);
        Note note = new Note(name,group);

    }



}
