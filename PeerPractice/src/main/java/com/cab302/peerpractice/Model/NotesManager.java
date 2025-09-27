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
        if(groupDAO.searchByID(groupID) == null) throw new IllegalArgumentException("Group " + groupID + " Does not exist");
        Note note = new Note(name,groupID);
        return notesDAO.addNote(note);
    }

    public void changeName(String noteID, String name){
        name = ValidationUtils.validateAndCleanOthersName(name);
        if(notesDAO.getNote(noteID) == null) throw new IllegalArgumentException("Note " + noteID + " Does not exist");
        notesDAO.changeName(noteID,name);
    }

    public void deleteNote(String noteID){
        if(notesDAO.getNote(noteID) == null) throw new IllegalArgumentException("Note " + noteID + " Does not exist");
        notesDAO.deleteNote(noteID);
    }

    public List<Note> getNotes(int groupID){
        if(groupDAO.searchByID(groupID) == null) throw new IllegalArgumentException("Group " + groupID + " Doesn not exist");
        return notesDAO.getNotes(groupID);
    }

    public String addChapter(String noteID, String name){
        Note note = notesDAO.getNote(noteID);
        if(note == null) throw new IllegalArgumentException("Note " + noteID + " Does not exist");
        name = ValidationUtils.validateAndCleanOthersName(name);
        Chapter chapter = new Chapter(name,noteID);
        String chapterID = notesDAO.addChapter(noteID,chapter);
        note.addChapter(chapterID);
        return chapterID;
    }

    public void changeChapterName(String chapterID, String name){
        if(notesDAO.getChapter(chapterID) == null) throw new IllegalArgumentException("Chapter " + chapterID + " Does not exist");
        name = ValidationUtils.validateAndCleanOthersName(name);
        notesDAO.updateChapter(chapterID,"name",name);
    }

    public void changeContent(String chapterID, String content){
        Chapter chapter = notesDAO.getChapter(chapterID);
        if(chapter == null) throw new IllegalArgumentException("Chapter " + chapterID + " Could not be found");
        chapter.setContent(content);
        notesDAO.updateChapter(chapterID,"content",content);
    }

    public List<Chapter> getChapters(String noteID){
        if(notesDAO.getNote(noteID) == null) throw new IllegalArgumentException("Note " + noteID + " Cannot be null");
        return notesDAO.getChapters(noteID);
    }

    public void deleteChapter(String chapterID){
        if(notesDAO.getNote(chapterID) == null) throw new IllegalArgumentException("Note " + chapterID + " Does not exist");
        notesDAO.deleteNote(chapterID);
    }



}
