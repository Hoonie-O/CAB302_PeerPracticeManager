package com.cab302.peerpractice.Model.DAOs;

import com.cab302.peerpractice.Model.Entities.Attachment;
import com.cab302.peerpractice.Model.Entities.Chapter;
import com.cab302.peerpractice.Model.Entities.Note;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mock (in-memory) implementation of INotesDAO for unit testing without a database.
 */
public class MockNotesDAO implements INotesDAO {

    private final Map<String, Note> notes = new ConcurrentHashMap<>();
    private final Map<String, Chapter> chapters = new ConcurrentHashMap<>();
    private final Map<String, Attachment> attachments = new ConcurrentHashMap<>();

    // -------------------- CREATE --------------------
    @Override
    public String addNote(Note note) {
        String noteId = UUID.randomUUID().toString();
        note.setID(noteId);
        notes.put(noteId, note);
        return noteId;
    }

    @Override
    public String addChapter(String noteID, Chapter chapter) {
        if (!notes.containsKey(noteID)) {
            throw new IllegalArgumentException("Note does not exist: " + noteID);
        }
        String chapterId = UUID.randomUUID().toString();
        chapter.setID(chapterId);
        chapters.put(chapterId, chapter);
        notes.get(noteID).addChapter(chapterId);
        return chapterId;
    }

    @Override
    public String addAttachment(String chapterID, Attachment attachment) {
        if (!chapters.containsKey(chapterID)) {
            throw new IllegalArgumentException("Chapter does not exist: " + chapterID);
        }
        String attachmentId = UUID.randomUUID().toString();
        attachments.put(attachmentId, attachment);
        return attachmentId;
    }

    // -------------------- READ --------------------
    @Override
    public List<Note> getNotes(int groupID) {
        List<Note> result = new ArrayList<>();
        for (Note note : notes.values()) {
            if (note.getGroup() == groupID) {
                result.add(note);
            }
        }
        return result;
    }

    @Override
    public Note getNote(String noteID) {
        return notes.get(noteID);
    }

    @Override
    public List<Note> getAllNotes() {
        return new ArrayList<>(notes.values());
    }

    @Override
    public List<Chapter> getChapters(String noteID) {
        Note note = notes.get(noteID);
        if (note == null) return Collections.emptyList();

        List<Chapter> result = new ArrayList<>();
        for (String chapId : note.getChapters()) {
            Chapter chapter = chapters.get(chapId);
            if (chapter != null) result.add(chapter);
        }
        return result;
    }

    @Override
    public Chapter getChapter(String chapterID) {
        return chapters.get(chapterID);
    }

    // -------------------- UPDATE --------------------
    @Override
    public void changeName(String noteID, String name) {
        Note note = notes.get(noteID);
        if (note != null) note.setName(name);
    }

    @Override
    public void updateChapter(String chapterID, String column, String value) {
        Chapter chapter = chapters.get(chapterID);
        if (chapter == null) return;

        switch (column) {
            case "name": chapter.setName(value); break;
            case "content": chapter.setContent(value); break;
            default: throw new IllegalArgumentException("Invalid column: " + column);
        }
    }

    @Override
    public void updateAttachment(String attachmentID) {
        // left empty as in the SQL version
    }

    // -------------------- DELETE --------------------
    @Override
    public void deleteNote(String noteID) {
        Note note = notes.remove(noteID);
        if (note != null) {
            for (String chapId : note.getChapters()) {
                chapters.remove(chapId);
                // remove attachments linked to this chapter
                attachments.entrySet().removeIf(e -> e.getValue().getChapterId().equals(chapId));
            }
        }
    }

    @Override
    public void deleteChapter(String chapterID) {
        chapters.remove(chapterID);
        attachments.entrySet().removeIf(e -> e.getValue().getChapterId().equals(chapterID));
        // also remove from parent note's chapter list
        notes.values().forEach(n -> n.getChapters().remove(chapterID));
    }

    @Override
    public void deleteAttachment(String attachmentID) {
        attachments.remove(attachmentID);
    }

    @Override
    public void getAttachments(String chapterID) {
        // no-op in mock for now
    }

    @Override
    public void getAttachment(String attachmentID) {
        // no-op in mock for now
    }
}
