package com.cab302.peerpractice.Model;

import java.util.List;

public class NotesManager {

    INotesDAO notesDAO;
    IGroupDAO groupDAO;

    public NotesManager(INotesDAO notesDAO, IGroupDAO groupDAO){
        this.notesDAO = notesDAO;
        this.groupDAO = groupDAO;
    }

    public String addNote(String name, int groupID){
        name = ValidationUtils.validateAndCleanOthersName(name);

        Note note = new Note(name,groupID);
        return notesDAO.addNote(note);
    }

    public void changeName(String noteID, String name){
        name = ValidationUtils.validateAndCleanOthersName(name);
        notesDAO.changeName(noteID,name);
    }




}
