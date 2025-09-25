package com.cab302.peerpractice.Model;

import java.awt.image.AreaAveragingScaleFilter;
import java.util.ArrayList;
import java.util.List;

public class MockNotesDao implements INotesDAO {

    private List<Note> notes;
    private List<Chapter> chapters;
    private IGroupDAO groupDAO;

    public MockNotesDao(){
        this.groupDAO = new MockGroupDAO();
        this.notes = new ArrayList<Note>();
        this.chapters = new ArrayList<Chapter>();
    }


    @Override
    public int addNote(Note note) {

    }

    @Override
    public void updateNote(String noteID, String column, String value) {

    }

    @Override
    public void deleteNote(String noteID) {

    }

    @Override
    public List<Note> getNotes(String groupID) {
        return List.of();
    }

    @Override
    public Note getNote(String noteID) {
        return null;
    }

    @Override
    public int addChapter(String noteID, Chapter chapter) {

    }

    @Override
    public void updateChapter(String chapterID, String column, String value) {

    }

    @Override
    public void deleteChapter(String chapterID) {

    }

    @Override
    public List<Chapter> getChapters(String noteID) {
        return List.of();
    }

    @Override
    public Chapter getChapter(String chapterID) {
        return null;
    }

    @Override
    public int addAttachment(String chapterID, Attachment attachment) {

    }

    @Override
    public void updateAttachment(String attachmentID) {

    }

    @Override
    public void deleteAttachment(String attachmentID) {

    }

    @Override
    public void getAttachments(String chapterID) {

    }

    @Override
    public void getAttachment(String attachmentID) {

    }
}
