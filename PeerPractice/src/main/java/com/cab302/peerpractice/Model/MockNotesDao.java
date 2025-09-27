package com.cab302.peerpractice.Model;

import java.awt.image.AreaAveragingScaleFilter;
import java.util.*;

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
    public String addNote(Note note) {
        String noteID = UUID.randomUUID().toString();
        note.setID(noteID);
        notes.add(note);
        return noteID;
    }

    @Override
    public void changeName(String noteID, String name){
        for(Note n : notes){
            if(n.getID().equals(noteID)){n.setName(name);}
        }
    }

    @Override
    public void deleteNote(String noteID) {
        notes.removeIf(n -> n.getID().equals(noteID));
    }

    @Override
    public List<Note> getNotes(int groupID) {
        List<Note> nts = notes.stream()
                .filter(n -> n.getGroup() == groupID )
                .toList();
        return Collections.unmodifiableList(nts);
    }

    @Override
    public Note getNote(String noteID) {
        return notes.stream().filter(n -> n.getID().equals(noteID)).findFirst().orElse(null);
    }



    @Override
    public List<Note> getAllNotes() {
        return Collections.unmodifiableList(notes);
    }

    @Override
    public String addChapter(String noteID, Chapter chapter) {
        String id = UUID.randomUUID().toString();
        Note note = notes.stream()
                .filter(n -> n.getID().equals(noteID))
                .findFirst()
                .orElse(null);
        note.addChapter(id);
        chapter.setID(id);
        chapters.add(chapter);
        return id;
    }

    @Override
    public void updateChapter(String chapterID, String column, String value) {
        Chapter chapter = chapters.stream()
                .filter(c -> c.getID().equals(chapterID))
                .findFirst()
                .orElse(null);
        if(column.equals("name")){chapter.setName(value);}
        else if(column.equals("content")){chapter.setContent(value);}
    }

    @Override
    public void deleteChapter(String chapterID) {
        chapters.removeIf(c -> c.getID().equals(chapterID));
    }

    @Override
    public List<Chapter> getChapters(String noteID) {
        List<Chapter> ch = chapters.stream()
                .filter(c -> c.getNote().equals(noteID))
                .toList();
        return Collections.unmodifiableList(ch);
    }

    @Override
    public Chapter getChapter(String chapterID) {
        return chapters.stream()
                .filter(c -> c.getID().equals(chapterID))
                .findFirst()
                .orElse(null);
    }

    @Override
    public String addAttachment(String chapterID, Attachment attachment) {
        Chapter chapter = chapters.stream()
                .filter(c -> c.getID().equals(chapterID))
                .findFirst()
                .orElse(null);
        chapter.addAttachment(attachment);
        return UUID.randomUUID().toString();
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
