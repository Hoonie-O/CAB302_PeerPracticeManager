package com.cab302.peerpractice.Model.daos;

import com.cab302.peerpractice.Model.entities.Attachment;
import com.cab302.peerpractice.Model.entities.Chapter;
import com.cab302.peerpractice.Model.entities.Note;
import com.cab302.peerpractice.Model.utils.SQLiteConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Database implementation of INotesDAO for persistent storage of notes, chapters, and attachments.
 */
public class NotesDAO implements INotesDAO {

    private final Connection connection;

    public NotesDAO() throws SQLException {
        this.connection = SQLiteConnection.getInstance();
        createTables();
    }

    // -------------------- TABLE CREATION --------------------
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

    @Override
    public Chapter getChapter(String chapterID) {
        String sql = "SELECT * FROM chapters WHERE chapter_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, chapterID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapChapter(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch chapter", e);
        }
        return null;
    }

    // -------------------- UPDATE --------------------
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

    @Override
    public void updateAttachment(String attachmentID) {
        // Placeholder for future implementation
    }

    // -------------------- DELETE --------------------
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

    @Override
    public void getAttachments(String chapterID) {
        // Placeholder for future implementation
    }

    @Override
    public void getAttachment(String attachmentID) {
        // Placeholder for future implementation
    }

    // -------------------- HELPERS --------------------
    private Note mapNote(ResultSet rs) throws SQLException {
        Note note = new Note(rs.getString("name"), rs.getInt("group_id"));
        note.setID(rs.getString("note_id"));
        // Load chapters
        getChapterIds(rs.getString("note_id")).forEach(note::addChapter);
        return note;
    }

    private Chapter mapChapter(ResultSet rs) throws SQLException {
        Chapter chapter = new Chapter(rs.getString("name"), rs.getString("note_id"));
        chapter.setID(rs.getString("chapter_id"));
        chapter.setContent(rs.getString("content"));
        return chapter;
    }

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
