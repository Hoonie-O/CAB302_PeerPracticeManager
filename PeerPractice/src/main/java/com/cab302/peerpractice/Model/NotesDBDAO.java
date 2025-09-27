package com.cab302.peerpractice.Model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Database implementation of INotesDAO for persistent storage of notes and chapters.
 */
public class NotesDBDAO implements INotesDAO {
    private final Connection connection;

    public NotesDBDAO() throws SQLException {
        this.connection = SQLiteConnection.getInstance();
        createTables();
    }

    private void createTables() throws SQLException {
        try (Statement st = connection.createStatement()) {
            // Create notes table
            st.execute("CREATE TABLE IF NOT EXISTS notes (" +
                    "note_id TEXT PRIMARY KEY, " +
                    "name TEXT NOT NULL, " +
                    "group_id INTEGER NOT NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            // Create chapters table
            st.execute("CREATE TABLE IF NOT EXISTS chapters (" +
                    "chapter_id TEXT PRIMARY KEY, " +
                    "note_id TEXT NOT NULL, " +
                    "name TEXT NOT NULL, " +
                    "content TEXT DEFAULT '', " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (note_id) REFERENCES notes(note_id) ON DELETE CASCADE" +
                    ")");

            // Create attachments table (for future use)
            st.execute("CREATE TABLE IF NOT EXISTS attachments (" +
                    "attachment_id TEXT PRIMARY KEY, " +
                    "chapter_id TEXT NOT NULL, " +
                    "filename TEXT NOT NULL, " +
                    "file_path TEXT NOT NULL, " +
                    "file_size INTEGER, " +
                    "mime_type TEXT, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (chapter_id) REFERENCES chapters(chapter_id) ON DELETE CASCADE" +
                    ")");
        }
    }

    @Override
    public String addNote(Note note) {
        String noteId = UUID.randomUUID().toString();
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO notes (note_id, name, group_id) VALUES (?, ?, ?)")) {
            ps.setString(1, noteId);
            ps.setString(2, note.getName());
            ps.setInt(3, note.getGroup());
            ps.executeUpdate();
            note.setID(noteId);
            return noteId;
        } catch (SQLException e) {
            System.err.println("Error adding note: " + e.getMessage());
            throw new RuntimeException("Failed to add note", e);
        }
    }

    @Override
    public void changeName(String noteID, String name) {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE notes SET name = ?, updated_at = CURRENT_TIMESTAMP WHERE note_id = ?")) {
            ps.setString(1, name);
            ps.setString(2, noteID);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating note name: " + e.getMessage());
            throw new RuntimeException("Failed to update note name", e);
        }
    }

    @Override
    public void deleteNote(String noteID) {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM notes WHERE note_id = ?")) {
            ps.setString(1, noteID);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting note: " + e.getMessage());
            throw new RuntimeException("Failed to delete note", e);
        }
    }

    @Override
    public List<Note> getNotes(int groupID) {
        List<Note> notes = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM notes WHERE group_id = ? ORDER BY created_at DESC")) {
            ps.setInt(1, groupID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Note note = new Note(rs.getString("name"), rs.getInt("group_id"));
                    note.setID(rs.getString("note_id"));

                    // Load chapter IDs for this note
                    List<String> chapterIds = getChapterIds(rs.getString("note_id"));
                    for (String chapterId : chapterIds) {
                        note.addChapter(chapterId);
                    }

                    notes.add(note);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting notes: " + e.getMessage());
        }
        return notes;
    }

    @Override
    public Note getNote(String noteID) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM notes WHERE note_id = ?")) {
            ps.setString(1, noteID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Note note = new Note(rs.getString("name"), rs.getInt("group_id"));
                    note.setID(rs.getString("note_id"));

                    // Load chapter IDs for this note
                    List<String> chapterIds = getChapterIds(noteID);
                    for (String chapterId : chapterIds) {
                        note.addChapter(chapterId);
                    }

                    return note;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting note: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Note> getAllNotes() {
        List<Note> notes = new ArrayList<>();
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM notes ORDER BY created_at DESC")) {
            while (rs.next()) {
                Note note = new Note(rs.getString("name"), rs.getInt("group_id"));
                note.setID(rs.getString("note_id"));

                // Load chapter IDs for this note
                List<String> chapterIds = getChapterIds(rs.getString("note_id"));
                for (String chapterId : chapterIds) {
                    note.addChapter(chapterId);
                }

                notes.add(note);
            }
        } catch (SQLException e) {
            System.err.println("Error getting all notes: " + e.getMessage());
        }
        return notes;
    }

    private List<String> getChapterIds(String noteId) {
        List<String> chapterIds = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT chapter_id FROM chapters WHERE note_id = ? ORDER BY created_at ASC")) {
            ps.setString(1, noteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    chapterIds.add(rs.getString("chapter_id"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting chapter IDs: " + e.getMessage());
        }
        return chapterIds;
    }

    @Override
    public String addChapter(String noteID, Chapter chapter) {
        String chapterId = UUID.randomUUID().toString();
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO chapters (chapter_id, note_id, name, content) VALUES (?, ?, ?, ?)")) {
            ps.setString(1, chapterId);
            ps.setString(2, noteID);
            ps.setString(3, chapter.getName());
            ps.setString(4, chapter.getContent());
            ps.executeUpdate();
            chapter.setID(chapterId);
            return chapterId;
        } catch (SQLException e) {
            System.err.println("Error adding chapter: " + e.getMessage());
            throw new RuntimeException("Failed to add chapter", e);
        }
    }

    @Override
    public void updateChapter(String chapterID, String column, String value) {
        String sql;
        if ("name".equals(column)) {
            sql = "UPDATE chapters SET name = ?, updated_at = CURRENT_TIMESTAMP WHERE chapter_id = ?";
        } else if ("content".equals(column)) {
            sql = "UPDATE chapters SET content = ?, updated_at = CURRENT_TIMESTAMP WHERE chapter_id = ?";
        } else {
            throw new IllegalArgumentException("Invalid column: " + column);
        }

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, value);
            ps.setString(2, chapterID);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating chapter: " + e.getMessage());
            throw new RuntimeException("Failed to update chapter", e);
        }
    }

    @Override
    public void deleteChapter(String chapterID) {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM chapters WHERE chapter_id = ?")) {
            ps.setString(1, chapterID);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting chapter: " + e.getMessage());
            throw new RuntimeException("Failed to delete chapter", e);
        }
    }

    @Override
    public List<Chapter> getChapters(String noteID) {
        List<Chapter> chapters = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM chapters WHERE note_id = ? ORDER BY created_at ASC")) {
            ps.setString(1, noteID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Chapter chapter = new Chapter(rs.getString("name"), rs.getString("note_id"));
                    chapter.setID(rs.getString("chapter_id"));
                    chapter.setContent(rs.getString("content"));
                    chapters.add(chapter);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting chapters: " + e.getMessage());
        }
        return chapters;
    }

    @Override
    public Chapter getChapter(String chapterID) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM chapters WHERE chapter_id = ?")) {
            ps.setString(1, chapterID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Chapter chapter = new Chapter(rs.getString("name"), rs.getString("note_id"));
                    chapter.setID(rs.getString("chapter_id"));
                    chapter.setContent(rs.getString("content"));
                    return chapter;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting chapter: " + e.getMessage());
        }
        return null;
    }

    // Attachment methods - basic implementation for future extension
    @Override
    public String addAttachment(String chapterID, Attachment attachment) {
        String attachmentId = UUID.randomUUID().toString();
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO attachments (attachment_id, chapter_id, filename, file_path, file_size, mime_type) VALUES (?, ?, ?, ?, ?, ?)")) {
            ps.setString(1, attachmentId);
            ps.setString(2, chapterID);
            ps.setString(3, attachment.getFilename());
            ps.setString(4, attachment.getFilePath());
            ps.setLong(5, attachment.getFileSize());
            ps.setString(6, attachment.getMimeType());
            ps.executeUpdate();
            return attachmentId;
        } catch (SQLException e) {
            System.err.println("Error adding attachment: " + e.getMessage());
            throw new RuntimeException("Failed to add attachment", e);
        }
    }

    @Override
    public void updateAttachment(String attachmentID) {
        // Implementation placeholder for future use
    }

    @Override
    public void deleteAttachment(String attachmentID) {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM attachments WHERE attachment_id = ?")) {
            ps.setString(1, attachmentID);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting attachment: " + e.getMessage());
        }
    }

    @Override
    public void getAttachments(String chapterID) {
        // Implementation placeholder for future use
    }

    @Override
    public void getAttachment(String attachmentID) {
        // Implementation placeholder for future use
    }
}