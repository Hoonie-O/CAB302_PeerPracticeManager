package com.cab302.peerpractice.Model.DAOs;

import com.cab302.peerpractice.Model.Entities.Attachment;
import com.cab302.peerpractice.Model.Entities.Chapter;
import com.cab302.peerpractice.Model.Entities.Note;
import com.cab302.peerpractice.Model.Utils.SQLiteConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * <hr>
 * Database implementation of INotesDAO for persistent storage of notes, chapters, and attachments.
 *
 * <p>This implementation provides SQLite-based persistent storage for notes, chapters,
 * and attachments with full CRUD operations and relational integrity.
 *
 * <p> Key features include:
 * <ul>
 *   <li>SQLite database with foreign key constraints</li>
 *   <li>UUID-based primary keys for all entities</li>
 *   <li>Automatic table creation on initialization</li>
 *   <li>Cascade delete for related chapters and attachments</li>
 *   <li>Timestamp tracking for creation and updates</li>
 * </ul>
 *
 * @see INotesDAO
 * @see Note
 * @see Chapter
 * @see Attachment
 */
public class NotesDAO implements INotesDAO {

    /** <hr> SQLite database connection instance. */
    private final Connection connection;

    /**
     * <hr>
     * Constructs a new NotesDAO and initializes database tables.
     *
     * @throws SQLException if database connection or table creation fails
     */
    public NotesDAO() throws SQLException {
        this.connection = SQLiteConnection.getInstance();
        createTables();
    }

    // -------------------- TABLE CREATION --------------------

    /**
     * <hr>
     * Creates the necessary database tables if they don't exist.
     *
     * <p>Creates tables for notes, chapters, and attachments with appropriate
     * foreign key constraints and cascade delete behavior.
     *
     * @throws SQLException if table creation fails
     */
    private void createTables() throws SQLException {
        try (Statement st = connection.createStatement()) {
            // Notes table
            st.execute("CREATE TABLE IF NOT EXISTS notes (" +
                    "note_id TEXT PRIMARY KEY, " +
                    "name TEXT NOT NULL, " +
                    "group_id INTEGER NOT NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            // Chapters table
            st.execute("CREATE TABLE IF NOT EXISTS chapters (" +
                    "chapter_id TEXT PRIMARY KEY, " +
                    "note_id TEXT NOT NULL, " +
                    "name TEXT NOT NULL, " +
                    "content TEXT DEFAULT '', " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY(note_id) REFERENCES notes(note_id) ON DELETE CASCADE" +
                    ")");

            // Attachments table
            st.execute("CREATE TABLE IF NOT EXISTS attachments (" +
                    "attachment_id TEXT PRIMARY KEY, " +
                    "chapter_id TEXT NOT NULL, " +
                    "filename TEXT NOT NULL, " +
                    "file_path TEXT NOT NULL, " +
                    "file_size INTEGER, " +
                    "mime_type TEXT, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY(chapter_id) REFERENCES chapters(chapter_id) ON DELETE CASCADE" +
                    ")");
        }
    }

    // -------------------- CREATE --------------------

    /**
     * <hr>
     * Adds a new note to the database.
     *
     * <p>Generates a UUID for the note and stores it with the provided information.
     *
     * @param note the note to add
     * @return the generated note ID
     * @throws RuntimeException if database operation fails
     */
    @Override
    public String addNote(Note note) {
        String noteId = UUID.randomUUID().toString();
        String sql = "INSERT INTO notes (note_id, name, group_id) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, noteId);
            ps.setString(2, note.getName());
            ps.setInt(3, note.getGroup());
            ps.executeUpdate();
            note.setID(noteId);
            return noteId;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add note", e);
        }
    }

    /**
     * <hr>
     * Adds a new chapter to a specific note.
     *
     * <p>Generates a UUID for the chapter and associates it with the parent note.
     *
     * @param noteID the ID of the parent note
     * @param chapter the chapter to add
     * @return the generated chapter ID
     * @throws RuntimeException if database operation fails
     */
    @Override
    public String addChapter(String noteID, Chapter chapter) {
        String chapterId = UUID.randomUUID().toString();
        String sql = "INSERT INTO chapters (chapter_id, note_id, name, content) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, chapterId);
            ps.setString(2, noteID);
            ps.setString(3, chapter.getName());
            ps.setString(4, chapter.getContent());
            ps.executeUpdate();
            chapter.setID(chapterId);
            return chapterId;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add chapter", e);
        }
    }

    /**
     * <hr>
     * Adds a new attachment to a specific chapter.
     *
     * <p>Generates a UUID for the attachment and stores file metadata.
     *
     * @param chapterID the ID of the parent chapter
     * @param attachment the attachment to add
     * @return the generated attachment ID
     * @throws RuntimeException if database operation fails
     */
    @Override
    public String addAttachment(String chapterID, Attachment attachment) {
        String attachmentId = UUID.randomUUID().toString();
        String sql = "INSERT INTO attachments (attachment_id, chapter_id, filename, file_path, file_size, mime_type) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, attachmentId);
            ps.setString(2, chapterID);
            ps.setString(3, attachment.getFilename());
            ps.setString(4, attachment.getFilePath());
            ps.setLong(5, attachment.getFileSize());
            ps.setString(6, attachment.getMimeType());
            ps.executeUpdate();
            return attachmentId;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add attachment", e);
        }
    }

    // -------------------- READ --------------------

    /**
     * <hr>
     * Retrieves all notes for a specific group.
     *
     * @param groupID the ID of the group
     * @return a list of notes belonging to the specified group, ordered by creation date
     * @throws RuntimeException if database operation fails
     */
    @Override
    public List<Note> getNotes(int groupID) {
        List<Note> notes = new ArrayList<>();
        String sql = "SELECT * FROM notes WHERE group_id = ? ORDER BY created_at DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, groupID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Note note = mapNote(rs);
                    notes.add(note);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch notes", e);
        }
        return notes;
    }

    /**
     * <hr>
     * Retrieves a specific note by its ID.
     *
     * @param noteID the ID of the note to retrieve
     * @return the note with the specified ID, or null if not found
     * @throws RuntimeException if database operation fails
     */
    @Override
    public Note getNote(String noteID) {
        String sql = "SELECT * FROM notes WHERE note_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, noteID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapNote(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch note", e);
        }
        return null;
    }

    /**
     * <hr>
     * Retrieves all notes from the database.
     *
     * @return a list of all notes, ordered by creation date
     * @throws RuntimeException if database operation fails
     */
    @Override
    public List<Note> getAllNotes() {
        List<Note> notes = new ArrayList<>();
        String sql = "SELECT * FROM notes ORDER BY created_at DESC";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                notes.add(mapNote(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch all notes", e);
        }
        return notes;
    }

    /**
     * <hr>
     * Retrieves all chapters for a specific note.
     *
     * @param noteID the ID of the parent note
     * @return a list of chapters belonging to the specified note, ordered by creation date
     * @throws RuntimeException if database operation fails
     */
    @Override
    public List<Chapter> getChapters(String noteID) {
        List<Chapter> chapters = new ArrayList<>();
        String sql = "SELECT * FROM chapters WHERE note_id = ? ORDER BY created_at ASC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, noteID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) chapters.add(mapChapter(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch chapters", e);
        }
        return chapters;
    }

    /**
     * <hr>
     * Retrieves a specific chapter by its ID.
     *
     * @param chapterID the ID of the chapter to retrieve
     * @return the chapter with the specified ID, or null if not found
     * @throws IllegalArgumentException if chapterID is null or empty
     * @throws RuntimeException if database operation fails
     */
    @Override
    public Chapter getChapter(String chapterID) {
        if (chapterID == null || chapterID.trim().isEmpty()) {
            throw new IllegalArgumentException("Chapter ID cannot be null or empty");
        }

        String sql = "SELECT * FROM chapters WHERE chapter_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, chapterID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapChapter(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch chapter", e);
        }

        // chapterID valid, but not found in DB
        return null;
    }

    // -------------------- UPDATE --------------------

    /**
     * <hr>
     * Changes the name of a specific note.
     *
     * @param noteID the ID of the note to update
     * @param name the new name for the note
     * @throws RuntimeException if database operation fails
     */
    @Override
    public void changeName(String noteID, String name) {
        String sql = "UPDATE notes SET name = ?, updated_at = CURRENT_TIMESTAMP WHERE note_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, noteID);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update note name", e);
        }
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
     * @throws RuntimeException if database operation fails
     */
    @Override
    public void updateChapter(String chapterID, String column, String value) {
        String sql;
        if ("name".equals(column)) {
            sql = "UPDATE chapters SET name = ?, updated_at = CURRENT_TIMESTAMP WHERE chapter_id = ?";
        } else if ("content".equals(column)) {
            sql = "UPDATE chapters SET content = ?, updated_at = CURRENT_TIMESTAMP WHERE chapter_id = ?";
        } else throw new IllegalArgumentException("Invalid column: " + column);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, value);
            ps.setString(2, chapterID);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update chapter", e);
        }
    }

    /**
     * <hr>
     * Updates an attachment (placeholder implementation).
     *
     * <p>This method is reserved for future implementation.
     *
     * @param attachmentID the ID of the attachment to update
     */
    @Override
    public void updateAttachment(String attachmentID) {
        // Placeholder for future implementation
    }

    // -------------------- DELETE --------------------

    /**
     * <hr>
     * Deletes a note and all its associated chapters and attachments.
     *
     * <p>Uses cascade delete to automatically remove related chapters and attachments.
     *
     * @param noteID the ID of the note to delete
     * @throws RuntimeException if database operation fails
     */
    @Override
    public void deleteNote(String noteID) {
        String sql = "DELETE FROM notes WHERE note_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, noteID);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete note", e);
        }
    }

    /**
     * <hr>
     * Deletes a chapter and all its associated attachments.
     *
     * <p>Uses cascade delete to automatically remove related attachments.
     *
     * @param chapterID the ID of the chapter to delete
     * @throws RuntimeException if database operation fails
     */
    @Override
    public void deleteChapter(String chapterID) {
        String sql = "DELETE FROM chapters WHERE chapter_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, chapterID);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete chapter", e);
        }
    }

    /**
     * <hr>
     * Deletes a specific attachment.
     *
     * @param attachmentID the ID of the attachment to delete
     * @throws RuntimeException if database operation fails
     */
    @Override
    public void deleteAttachment(String attachmentID) {
        String sql = "DELETE FROM attachments WHERE attachment_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, attachmentID);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete attachment", e);
        }
    }

    /**
     * <hr>
     * Retrieves attachments for a chapter (placeholder implementation).
     *
     * <p>This method is reserved for future implementation.
     *
     * @param chapterID the ID of the chapter
     */
    @Override
    public void getAttachments(String chapterID) {
        // Placeholder for future implementation
    }

    /**
     * <hr>
     * Retrieves a specific attachment (placeholder implementation).
     *
     * <p>This method is reserved for future implementation.
     *
     * @param attachmentID the ID of the attachment to retrieve
     */
    @Override
    public void getAttachment(String attachmentID) {
        // Placeholder for future implementation
    }

    // -------------------- HELPERS --------------------

    /**
     * <hr>
     * Maps a database ResultSet row to a Note object.
     *
     * @param rs the ResultSet containing note data
     * @return a Note object populated with data from the ResultSet
     * @throws SQLException if database access error occurs
     */
    private Note mapNote(ResultSet rs) throws SQLException {
        Note note = new Note(rs.getString("name"), rs.getInt("group_id"));
        note.setID(rs.getString("note_id"));
        // Load chapters
        getChapterIds(rs.getString("note_id")).forEach(note::addChapter);
        return note;
    }

    /**
     * <hr>
     * Maps a database ResultSet row to a Chapter object.
     *
     * @param rs the ResultSet containing chapter data
     * @return a Chapter object populated with data from the ResultSet
     * @throws SQLException if database access error occurs
     */
    private Chapter mapChapter(ResultSet rs) throws SQLException {
        Chapter chapter = new Chapter(rs.getString("name"), rs.getString("note_id"));
        chapter.setID(rs.getString("chapter_id"));
        chapter.setContent(rs.getString("content"));
        return chapter;
    }

    /**
     * <hr>
     * Retrieves all chapter IDs for a specific note.
     *
     * @param noteId the ID of the note
     * @return a list of chapter IDs belonging to the specified note
     * @throws RuntimeException if database operation fails
     */
    private List<String> getChapterIds(String noteId) {
        List<String> chapterIds = new ArrayList<>();
        String sql = "SELECT chapter_id FROM chapters WHERE note_id = ? ORDER BY created_at ASC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, noteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) chapterIds.add(rs.getString("chapter_id"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch chapter IDs", e);
        }
        return chapterIds;
    }
}