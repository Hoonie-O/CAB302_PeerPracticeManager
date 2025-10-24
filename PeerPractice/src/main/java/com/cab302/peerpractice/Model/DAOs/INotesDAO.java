package com.cab302.peerpractice.Model.DAOs;

import com.cab302.peerpractice.Model.Entities.Attachment;
import com.cab302.peerpractice.Model.Entities.Chapter;
import com.cab302.peerpractice.Model.Entities.Note;

import java.util.List;

/**
 * <hr>
 * Data Access Object interface for managing collaborative notes and study materials.
 *
 * <p>This interface defines the contract for notes management data operations,
 * providing methods to create, organize, and manage structured notes with
 * chapters and attachments for collaborative learning.
 *
 * <p> Key features include:
 * <ul>
 *   <li>Hierarchical note organization with chapters</li>
 *   <li>Attachment management for rich content</li>
 *   <li>Group-based note sharing and collaboration</li>
 *   <li>Structured content management for study materials</li>
 * </ul>
 *
 * @see Note
 * @see Chapter
 * @see Attachment
 */
public interface INotesDAO {

    // === Notes Operations ===

    /**
     * <hr>
     * Adds a new note to the system.
     *
     * <p>Creates a new note entity and persists it to the database,
     * returning the generated note identifier for future reference
     * and chapter/attachment associations.
     *
     * @param note the Note object to be added
     * @return the unique identifier of the newly created note
     */
    String addNote(Note note);

    /**
     * <hr>
     * Changes the name of an existing note.
     *
     * <p>Updates the display name of a specific note identified by
     * its unique identifier, allowing users to reorganize and
     * rename their study materials.
     *
     * @param noteID the unique identifier of the note to rename
     * @param name the new name to assign to the note
     */
    void changeName(String noteID, String name);

    /**
     * <hr>
     * Deletes a note and all associated content.
     *
     * <p>Removes a note entity from the database including all
     * associated chapters and attachments through cascading
     * delete operations.
     *
     * @param noteID the unique identifier of the note to delete
     */
    void deleteNote(String noteID);

    /**
     * <hr>
     * Retrieves all notes for a specific group.
     *
     * <p>Fetches the complete collection of notes shared within
     * a particular group context, enabling collaborative note
     * sharing and group study sessions.
     *
     * @param groupID the unique identifier of the group
     * @return a list of Note objects belonging to the specified group
     */
    List<Note> getNotes(int groupID);

    /**
     * <hr>
     * Retrieves a specific note by its unique identifier.
     *
     * <p>Fetches a single note entity with all its associated
     * chapters and metadata using the note's primary key.
     *
     * @param noteID the unique identifier of the note to retrieve
     * @return the Note object if found, null otherwise
     */
    Note getNote(String noteID);

    /**
     * <hr>
     * Retrieves all notes from the system.
     *
     * <p>Fetches every note entity stored in the database across
     * all groups and users, providing comprehensive system-wide
     * note access for administrative purposes.
     *
     * @return a list of all Note objects in the system
     */
    List<Note> getAllNotes();

    // === Chapters Operations ===

    /**
     * <hr>
     * Adds a new chapter to an existing note.
     *
     * <p>Creates a chapter entity and associates it with a specific
     * note, enabling hierarchical organization of note content
     * into structured sections.
     *
     * @param noteID the unique identifier of the parent note
     * @param chapter the Chapter object to be added
     * @return the unique identifier of the newly created chapter
     */
    String addChapter(String noteID, Chapter chapter);

    /**
     * <hr>
     * Updates a specific attribute of a chapter.
     *
     * <p>Modifies a single column/value pair for a chapter entity,
     * allowing targeted updates to chapter content, title, or
     * other attributes without replacing the entire entity.
     *
     * @param chapterID the unique identifier of the chapter to update
     * @param column the specific column/attribute to modify
     * @param value the new value to assign to the specified column
     */
    void updateChapter(String chapterID, String column, String value);

    /**
     * <hr>
     * Deletes a chapter from a note.
     *
     * <p>Removes a chapter entity from the database including
     * all associated attachments, maintaining referential
     * integrity within the note structure.
     *
     * @param chapterID the unique identifier of the chapter to delete
     */
    void deleteChapter(String chapterID);

    /**
     * <hr>
     * Retrieves all chapters for a specific note.
     *
     * <p>Fetches the complete chapter hierarchy for a particular
     * note, providing access to the structured content
     * organization within the note.
     *
     * @param noteID the unique identifier of the parent note
     * @return a list of Chapter objects belonging to the specified note
     */
    List<Chapter> getChapters(String noteID);

    /**
     * <hr>
     * Retrieves a specific chapter by its unique identifier.
     *
     * <p>Fetches a single chapter entity with all its content
     * and metadata using the chapter's primary key.
     *
     * @param chapterID the unique identifier of the chapter to retrieve
     * @return the Chapter object if found, null otherwise
     */
    Chapter getChapter(String chapterID);

    // === Attachments Operations ===

    /**
     * <hr>
     * Adds a new attachment to a chapter.
     *
     * <p>Creates an attachment entity and associates it with a
     * specific chapter, enabling rich media and file attachments
     * to enhance note content.
     *
     * @param chapterID the unique identifier of the parent chapter
     * @param attachment the Attachment object to be added
     * @return the unique identifier of the newly created attachment
     */
    String addAttachment(String chapterID, Attachment attachment);

    /**
     * <hr>
     * Updates an existing attachment.
     *
     * <p>Modifies an attachment entity's attributes, typically
     * used for metadata updates, version control, or content
     * modifications of attached files.
     *
     * @param attachmentID the unique identifier of the attachment to update
     */
    void updateAttachment(String attachmentID);

    /**
     * <hr>
     * Deletes an attachment from a chapter.
     *
     * <p>Removes an attachment entity from the database,
     * detaching the file or media from its parent chapter
     * while maintaining chapter integrity.
     *
     * @param attachmentID the unique identifier of the attachment to delete
     */
    void deleteAttachment(String attachmentID);

    /**
     * <hr>
     * Retrieves all attachments for a specific chapter.
     *
     * <p>Fetches the complete collection of attachments associated
     * with a particular chapter, providing access to all
     * supplementary files and media.
     *
     * @param chapterID the unique identifier of the parent chapter
     */
    void getAttachments(String chapterID);

    /**
     * <hr>
     * Retrieves a specific attachment by its unique identifier.
     *
     * <p>Fetches a single attachment entity with all its metadata
     * and file information using the attachment's primary key.
     *
     * @param attachmentID the unique identifier of the attachment to retrieve
     */
    void getAttachment(String attachmentID);
}