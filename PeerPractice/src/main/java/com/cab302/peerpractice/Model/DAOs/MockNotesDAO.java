package com.cab302.peerpractice.Model.DAOs;

import com.cab302.peerpractice.Model.Entities.Attachment;
import com.cab302.peerpractice.Model.Entities.Chapter;
import com.cab302.peerpractice.Model.Entities.Note;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <hr>
 * Mock (in-memory) implementation of INotesDAO for unit testing without a database.
 *
 * <p>This implementation provides in-memory storage for notes, chapters, and attachments
 * to facilitate testing without requiring a real database connection.
 *
 * <p> Key features include:
 * <ul>
 *   <li>Thread-safe concurrent data structures</li>
 *   <li>UUID-based ID generation for all entities</li>
 *   <li>Hierarchical storage of notes, chapters, and attachments</li>
 *   <li>Support for basic CRUD operations on all entity types</li>
 * </ul>
 *
 * @see INotesDAO
 * @see Note
 * @see Chapter
 * @see Attachment
 */
public class MockNotesDAO implements INotesDAO {

    /** <hr> In-memory storage for notes by note ID. */
    private final Map<String, Note> notes = new ConcurrentHashMap<>();
    /** <hr> In-memory storage for chapters by chapter ID. */
    private final Map<String, Chapter> chapters = new ConcurrentHashMap<>();
    /** <hr> In-memory storage for attachments by attachment ID. */
    private final Map<String, Attachment> attachments = new ConcurrentHashMap<>();

    // -------------------- CREATE --------------------

    /**
     * <hr>
     * Adds a new note to the in-memory storage.
     *
     * <p>Generates a unique UUID for the note and stores it.
     *
     * @param note the note to add
     * @return the generated note ID
     */
    @Override
    public String addNote(Note note) {
        String noteId = UUID.randomUUID().toString();
        note.setID(noteId);
        notes.put(noteId, note);
        return noteId;
    }

    /**
     * <hr>
     * Adds a new chapter to a specific note.
     *
     * <p>Generates a unique UUID for the chapter and associates it with the parent note.
     *
     * @param noteID the ID of the parent note
     * @param chapter the chapter to add
     * @return the generated chapter ID
     * @throws IllegalArgumentException if the parent note does not exist
     */
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

    /**
     * <hr>
     * Adds a new attachment to a specific chapter.
     *
     * <p>Generates a unique UUID for the attachment and associates it with the parent chapter.
     *
     * @param chapterID the ID of the parent chapter
     * @param attachment the attachment to add
     * @return the generated attachment ID
     * @throws IllegalArgumentException if the parent chapter does not exist
     */
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

    /**
     * <hr>
     * Retrieves all notes for a specific group.
     *
     * @param groupID the ID of the group
     * @return a list of notes belonging to the specified group
     */
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

    /**
     * <hr>
     * Retrieves a specific note by its ID.
     *
     * @param noteID the ID of the note to retrieve
     * @return the note with the specified ID, or null if not found
     */
    @Override
    public Note getNote(String noteID) {
        return notes.get(noteID);
    }

    /**
     * <hr>
     * Retrieves all notes from the in-memory storage.
     *
     * @return a list of all notes
     */
    @Override
    public List<Note> getAllNotes() {
        return new ArrayList<>(notes.values());
    }

    /**
     * <hr>
     * Retrieves all chapters for a specific note.
     *
     * @param noteID the ID of the parent note
     * @return a list of chapters belonging to the specified note
     */
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

    /**
     * <hr>
     * Retrieves a specific chapter by its ID.
     *
     * @param chapterID the ID of the chapter to retrieve
     * @return the chapter with the specified ID, or null if not found
     */
    @Override
    public Chapter getChapter(String chapterID) {
        return chapters.get(chapterID);
    }

    // -------------------- UPDATE --------------------

    /**
     * <hr>
     * Changes the name of a specific note.
     *
     * @param noteID the ID of the note to update
     * @param name the new name for the note
     */
    @Override
    public void changeName(String noteID, String name) {
        Note note = notes.get(noteID);
        if (note != null) note.setName(name);
    }

    /**
     * <hr>
     * Updates a specific field of a chapter.
     *
     * <p>Supports updating either the name or content field.
     *
     * @param chapterID the ID of the chapter to update
     * @param column the field to update ("name" or "content")
     * @param value the new value for the field
     * @throws IllegalArgumentException if an invalid column is specified
     */
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

    /**
     * <hr>
     * Updates an attachment (no-op implementation).
     *
     * <p>This method is left empty to match the SQL version's interface.
     *
     * @param attachmentID the ID of the attachment to update
     */
    @Override
    public void updateAttachment(String attachmentID) {
        // left empty as in the SQL version
    }

    // -------------------- DELETE --------------------

    /**
     * <hr>
     * Deletes a note and all its associated chapters and attachments.
     *
     * @param noteID the ID of the note to delete
     */
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

    /**
     * <hr>
     * Deletes a chapter and all its associated attachments.
     *
     * <p>Also removes the chapter from its parent note's chapter list.
     *
     * @param chapterID the ID of the chapter to delete
     */
    @Override
    public void deleteChapter(String chapterID) {
        chapters.remove(chapterID);
        attachments.entrySet().removeIf(e -> e.getValue().getChapterId().equals(chapterID));
        // also remove from parent note's chapter list
        notes.values().forEach(n -> n.getChapters().remove(chapterID));
    }

    /**
     * <hr>
     * Deletes a specific attachment.
     *
     * @param attachmentID the ID of the attachment to delete
     */
    @Override
    public void deleteAttachment(String attachmentID) {
        attachments.remove(attachmentID);
    }

    /**
     * <hr>
     * Retrieves attachments for a chapter (no-op implementation).
     *
     * <p>This method is left empty in the mock implementation.
     *
     * @param chapterID the ID of the chapter
     */
    @Override
    public void getAttachments(String chapterID) {
        // no-op in mock for now
    }

    /**
     * <hr>
     * Retrieves a specific attachment (no-op implementation).
     *
     * <p>This method is left empty in the mock implementation.
     *
     * @param attachmentID the ID of the attachment to retrieve
     */
    @Override
    public void getAttachment(String attachmentID) {
        // no-op in mock for now
    }
}