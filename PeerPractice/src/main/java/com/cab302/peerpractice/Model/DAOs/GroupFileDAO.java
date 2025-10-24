package com.cab302.peerpractice.Model.DAOs;

import com.cab302.peerpractice.Model.Entities.GroupFile;
import com.cab302.peerpractice.Model.Utils.SQLiteConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * <hr>
 * SQLite implementation of group file Data Access Object.
 *
 * <p>This class provides concrete SQLite database operations for managing
 * file uploads and attachments within groups, handling persistence and
 * retrieval of files shared in group contexts.
 *
 * <p> Key features include:
 * <ul>
 *   <li>File metadata storage and management</li>
 *   <li>Group-specific file organization and retrieval</li>
 *   <li>Uploader tracking and file attribution</li>
 *   <li>File deletion and cleanup operations</li>
 * </ul>
 *
 * @see GroupFile
 * @see IGroupFileDAO
 * @see SQLiteConnection
 */
public class GroupFileDAO implements IGroupFileDAO {

    /** <hr> Database connection instance for SQLite operations. */
    private final Connection connection;

    /**
     * <hr>
     * Constructs a new GroupFileDAO with database connection.
     *
     * <p>Initializes the SQLite connection and ensures the required
     * database table exists by calling createTable() during construction.
     *
     * @throws SQLException if database connection or table creation fails
     */
    public GroupFileDAO() throws SQLException {
        this.connection = SQLiteConnection.getInstance();
        createTable();
    }

    /**
     * <hr>
     * Creates the group_files table if it doesn't exist.
     *
     * <p>Defines the database schema for storing group file metadata with
     * appropriate foreign key constraints, file attributes, and indexing
     * for efficient file retrieval and management.
     *
     * @throws SQLException if table creation fails
     */
    private void createTable() throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS group_files (" +
                    "file_id TEXT PRIMARY KEY, " +
                    "group_id INTEGER NOT NULL, " +
                    "uploader_id TEXT NOT NULL, " +
                    "filename TEXT NOT NULL, " +
                    "filepath TEXT NOT NULL, " +
                    "file_size INTEGER NOT NULL, " +
                    "mime_type TEXT, " +
                    "uploaded_at TEXT NOT NULL, " +
                    "description TEXT, " +
                    "FOREIGN KEY(uploader_id) REFERENCES users(username) ON DELETE CASCADE, " +
                    "FOREIGN KEY(group_id) REFERENCES groups(group_id) ON DELETE CASCADE" +
                    ")");
        }
    }

    /**
     * <hr>
     * Maps a database ResultSet row to a GroupFile object.
     *
     * <p>Converts SQL result set data into a structured GroupFile entity
     * with proper type conversions for timestamp, file size, and other
     * file attributes.
     *
     * @param rs the ResultSet containing database row data
     * @return a populated GroupFile object
     * @throws SQLException if data extraction fails
     */
    private GroupFile mapRow(ResultSet rs) throws SQLException {
        return new GroupFile(
                rs.getString("file_id"),
                rs.getInt("group_id"),
                rs.getString("uploader_id"),
                rs.getString("filename"),
                rs.getString("filepath"),
                rs.getLong("file_size"),
                rs.getString("mime_type"),
                LocalDateTime.parse(rs.getString("uploaded_at")),
                rs.getString("description")
        );
    }

    /**
     * <hr>
     * Adds a new group file record to the database.
     *
     * <p>Persists file metadata to the SQLite database including file
     * identification, group association, uploader information, and
     * file characteristics for organized file management.
     *
     * @param file the GroupFile object to be stored
     * @return true if the file record was successfully added, false otherwise
     */
    @Override
    public boolean addFile(GroupFile file) {
        String sql = "INSERT INTO group_files (file_id, group_id, uploader_id, filename, filepath, file_size, mime_type, uploaded_at, description) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, file.getFileId());
            ps.setInt(2, file.getGroupId());
            ps.setString(3, file.getUploaderId());
            ps.setString(4, file.getFilename());
            ps.setString(5, file.getFilepath());
            ps.setLong(6, file.getFileSize());
            ps.setString(7, file.getMimeType());
            ps.setString(8, file.getUploadedAt().toString());
            ps.setString(9, file.getDescription());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding group file: " + e.getMessage());
            return false;
        }
    }

    /**
     * <hr>
     * Deletes a specific group file record by its unique identifier.
     *
     * <p>Removes a single file metadata record from the database using its
     * unique file ID, allowing for precise file management and cleanup.
     *
     * @param fileId the unique identifier of the file to delete
     * @return true if the file record was successfully deleted, false otherwise
     */
    @Override
    public boolean deleteFile(String fileId) {
        String sql = "DELETE FROM group_files WHERE file_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, fileId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting group file: " + e.getMessage());
            return false;
        }
    }

    /**
     * <hr>
     * Retrieves a specific group file record by its unique identifier.
     *
     * <p>Fetches a single file metadata record from the database using its
     * unique file ID, returning the complete file entity with all
     * associated file information.
     *
     * @param fileId the unique identifier of the file to retrieve
     * @return the GroupFile object if found, null otherwise
     */
    @Override
    public GroupFile getFileById(String fileId) {
        String sql = "SELECT * FROM group_files WHERE file_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, fileId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching group file: " + e.getMessage());
        }
        return null;
    }

    /**
     * <hr>
     * Retrieves all files associated with a specific group.
     *
     * <p>Fetches the complete file list for a particular group, ordered
     * by upload timestamp in descending order to show most recent
     * files first for efficient file browsing.
     *
     * @param groupId the unique identifier of the group
     * @return a list of GroupFile objects for the specified group
     */
    @Override
    public List<GroupFile> getFilesForGroup(int groupId) {
        List<GroupFile> list = new ArrayList<>();
        String sql = "SELECT * FROM group_files WHERE group_id = ? ORDER BY uploaded_at DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching files for group: " + e.getMessage());
        }
        return list;
    }

    /**
     * <hr>
     * Deletes all files associated with a specific group.
     *
     * <p>Removes the entire file history for a particular group,
     * typically used when a group is deleted or requires complete
     * file cleanup and data removal.
     *
     * @param groupId the unique identifier of the group
     * @return true if the operation completed successfully, false otherwise
     */
    @Override
    public boolean deleteFilesForGroup(int groupId) {
        String sql = "DELETE FROM group_files WHERE group_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            return ps.executeUpdate() >= 0;  // Can be 0 if no files exist
        } catch (SQLException e) {
            System.err.println("Error deleting files for group: " + e.getMessage());
            return false;
        }
    }

    /**
     * <hr>
     * Retrieves all files uploaded by a specific user.
     *
     * <p>Fetches the complete file upload history for a particular user
     * across all groups, ordered by upload timestamp in descending order
     * to show most recent uploads first.
     *
     * @param userId the username of the uploader to fetch files for
     * @return a list of GroupFile objects uploaded by the specified user
     */
    @Override
    public List<GroupFile> getFilesByUploader(String userId) {
        List<GroupFile> list = new ArrayList<>();
        String sql = "SELECT * FROM group_files WHERE uploader_id = ? ORDER BY uploaded_at DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching files by uploader: " + e.getMessage());
        }
        return list;
    }
}