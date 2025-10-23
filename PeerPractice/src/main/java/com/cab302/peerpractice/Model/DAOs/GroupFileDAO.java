package com.cab302.peerpractice.Model.DAOs;

import com.cab302.peerpractice.Model.Entities.GroupFile;
import com.cab302.peerpractice.Model.Utils.SQLiteConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * SQLite implementation of IGroupFileDAO.
 */
public class GroupFileDAO implements IGroupFileDAO {

    private final Connection connection;

    public GroupFileDAO() throws SQLException {
        this.connection = SQLiteConnection.getInstance();
        createTable();
    }

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
