package com.cab302.peerpractice.Model.Managers;

import com.cab302.peerpractice.Model.Entities.Chapter;
import com.cab302.peerpractice.Model.Entities.Note;
import com.cab302.peerpractice.Model.DAOs.IGroupDAO;
import com.cab302.peerpractice.Model.DAOs.INotesDAO;
import com.cab302.peerpractice.Model.Utils.ValidationUtils;

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

    public void deleteNote(String noteID) {
        if (noteID == null || noteID.trim().isEmpty()) {
            throw new IllegalArgumentException("Note ID cannot be null or empty");
        }

        if (notesDAO.getNote(noteID) == null) {
            throw new IllegalArgumentException("Note with ID " + noteID + " does not exist");
        }

        notesDAO.deleteNote(noteID);
    }

    public List<Note> getNotes(int groupID){
        if(groupDAO.searchByID(groupID) == null) throw new IllegalArgumentException("Group " + groupID + " Doesn not exist");
        return notesDAO.getNotes(groupID);
    }

    public String addChapter(String noteID, String name) {
        if (noteID == null || noteID.trim().isEmpty()) {
            throw new IllegalArgumentException("Note ID cannot be null or empty");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Chapter name cannot be null or empty");
        }

        Note note = notesDAO.getNote(noteID);
        if (note == null) {
            throw new IllegalArgumentException("Note with ID " + noteID + " does not exist");
        }

        // Clean/validate chapter name
        name = ValidationUtils.validateAndCleanOthersName(name);

        Chapter chapter = new Chapter(name, noteID);
        String chapterID = notesDAO.addChapter(noteID, chapter);
        note.addChapter(chapterID);

        return chapterID;
    }

    public void changeChapterName(String chapterID, String name) {
        if (chapterID == null || chapterID.trim().isEmpty()) {
            throw new IllegalArgumentException("Chapter ID cannot be null or empty");
        }

        Chapter chapter = notesDAO.getChapter(chapterID);
        if (chapter == null) {
            throw new IllegalArgumentException("Chapter with ID " + chapterID + " does not exist");
        }

        name = ValidationUtils.validateAndCleanOthersName(name);
        notesDAO.updateChapter(chapterID, "name", name);
    }

    public void changeContent(String chapterID, String content){
        if(content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Content cannot be null or empty");
        }
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
        if(notesDAO.getChapter(chapterID) == null) throw new IllegalArgumentException("Chapter " + chapterID + " Does not exist");
        notesDAO.deleteChapter(chapterID);
    }



}
