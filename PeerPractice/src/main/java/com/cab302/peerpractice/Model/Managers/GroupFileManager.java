package com.cab302.peerpractice.Model.Managers;

import com.cab302.peerpractice.Model.DAOs.IGroupFileDAO;
import com.cab302.peerpractice.Model.Entities.GroupFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Manager class for handling group file operations.
 * Acts as the middle layer between controllers and the DAO.
 * Handles file storage and metadata management.
 */
public class GroupFileManager {

    private final IGroupFileDAO groupFileDAO;
    private static final String FILE_STORAGE_ROOT = "group_files";

    public GroupFileManager(IGroupFileDAO groupFileDAO) {
        this.groupFileDAO = Objects.requireNonNull(groupFileDAO, "GroupFileDAO cannot be null");
        initializeFileStorage();
    }

    /**
     * Creates the file storage directory structure if it doesn't exist.
     */
    private void initializeFileStorage() {
        try {
            Path storagePath = Paths.get(FILE_STORAGE_ROOT);
            if (!Files.exists(storagePath)) {
                Files.createDirectories(storagePath);
            }
        } catch (IOException e) {
            System.err.println("Error creating file storage directory: " + e.getMessage());
        }
    }

    /**
     * Uploads a file to group storage and creates a database record.
     *
     * @param groupId the group ID
     * @param uploaderId the user ID of the uploader
     * @param sourceFile the file to upload
     * @param description optional description
     * @return the created GroupFile, or null if upload failed
     * @throws IOException if file operations fail
     */
    public GroupFile uploadFile(int groupId, String uploaderId, File sourceFile, String description) throws IOException {
        if (groupId <= 0) {
            throw new IllegalArgumentException("Invalid group ID: " + groupId);
        }
        if (uploaderId == null || uploaderId.isBlank()) {
            throw new IllegalArgumentException("Uploader ID cannot be null or blank");
        }
        if (sourceFile == null || !sourceFile.exists()) {
            throw new IllegalArgumentException("Source file does not exist");
        }

        // Generate unique file ID
        String fileId = UUID.randomUUID().toString();

        // Create group-specific directory
        Path groupDir = Paths.get(FILE_STORAGE_ROOT, "group_" + groupId);
        if (!Files.exists(groupDir)) {
            Files.createDirectories(groupDir);
        }

        // Determine file extension
        String originalFilename = sourceFile.getName();
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex);
        }

        // Create destination path with unique ID
        String storedFilename = fileId + extension;
        Path destinationPath = groupDir.resolve(storedFilename);

        // Copy file to storage
        Files.copy(sourceFile.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);

        // Determine MIME type
        String mimeType = getMimeType(sourceFile);

        // Create GroupFile entity
        GroupFile groupFile = new GroupFile(
                fileId,
                groupId,
                uploaderId,
                originalFilename,
                destinationPath.toString(),
                sourceFile.length(),
                mimeType,
                LocalDateTime.now(),
                description
        );

        // Save to database
        if (groupFileDAO.addFile(groupFile)) {
            return groupFile;
        } else {
            // Clean up file if database insert failed
            Files.deleteIfExists(destinationPath);
            return null;
        }
    }

    /**
     * Determines the MIME type of a file based on its extension.
     *
     * @param file the file
     * @return the MIME type
     */
    private String getMimeType(File file) {
        String filename = file.getName().toLowerCase();

        // Audio files
        if (filename.endsWith(".mp3")) return "audio/mpeg";
        if (filename.endsWith(".wav")) return "audio/wav";
        if (filename.endsWith(".ogg")) return "audio/ogg";
        if (filename.endsWith(".m4a")) return "audio/mp4";
        if (filename.endsWith(".flac")) return "audio/flac";

        // Document files
        if (filename.endsWith(".pdf")) return "application/pdf";
        if (filename.endsWith(".doc")) return "application/msword";
        if (filename.endsWith(".docx")) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        if (filename.endsWith(".txt")) return "text/plain";
        if (filename.endsWith(".rtf")) return "application/rtf";

        // Other common types
        if (filename.endsWith(".zip")) return "application/zip";
        if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) return "image/jpeg";
        if (filename.endsWith(".png")) return "image/png";

        return "application/octet-stream";
    }

    /**
     * Retrieves all files for a specific group.
     *
     * @param groupId the group ID
     * @return list of GroupFile objects
     */
    public List<GroupFile> getFilesForGroup(int groupId) {
        if (groupId <= 0) {
            throw new IllegalArgumentException("Invalid group ID: " + groupId);
        }
        return groupFileDAO.getFilesForGroup(groupId);
    }

    /**
     * Retrieves a file by its ID.
     *
     * @param fileId the file ID
     * @return the GroupFile, or null if not found
     */
    public GroupFile getFileById(String fileId) {
        if (fileId == null || fileId.isBlank()) {
            throw new IllegalArgumentException("File ID cannot be null or blank");
        }
        return groupFileDAO.getFileById(fileId);
    }

    /**
     * Deletes a file from both storage and database.
     *
     * @param fileId the file ID
     * @return true if successful, false otherwise
     */
    public boolean deleteFile(String fileId) {
        if (fileId == null || fileId.isBlank()) {
            throw new IllegalArgumentException("File ID cannot be null or blank");
        }

        // Get file metadata
        GroupFile file = groupFileDAO.getFileById(fileId);
        if (file == null) {
            return false;
        }

        // Delete from database first
        boolean dbDeleted = groupFileDAO.deleteFile(fileId);

        // Delete physical file
        try {
            Path filePath = Paths.get(file.getFilepath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.err.println("Error deleting physical file: " + e.getMessage());
            // Continue even if physical file deletion fails
        }

        return dbDeleted;
    }

    /**
     * Deletes all files for a specific group.
     *
     * @param groupId the group ID
     * @return true if successful, false otherwise
     */
    public boolean deleteFilesForGroup(int groupId) {
        if (groupId <= 0) {
            throw new IllegalArgumentException("Invalid group ID: " + groupId);
        }

        // Get all files first
        List<GroupFile> files = groupFileDAO.getFilesForGroup(groupId);

        // Delete from database
        boolean dbDeleted = groupFileDAO.deleteFilesForGroup(groupId);

        // Delete physical files
        for (GroupFile file : files) {
            try {
                Path filePath = Paths.get(file.getFilepath());
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                System.err.println("Error deleting physical file: " + e.getMessage());
            }
        }

        // Try to delete group directory if empty
        try {
            Path groupDir = Paths.get(FILE_STORAGE_ROOT, "group_" + groupId);
            if (Files.exists(groupDir) && Files.isDirectory(groupDir)) {
                Files.deleteIfExists(groupDir);
            }
        } catch (IOException e) {
            // Ignore if directory is not empty or cannot be deleted
        }

        return dbDeleted;
    }

    /**
     * Gets all files uploaded by a specific user.
     *
     * @param userId the user ID
     * @return list of GroupFile objects
     */
    public List<GroupFile> getFilesByUploader(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID cannot be null or blank");
        }
        return groupFileDAO.getFilesByUploader(userId);
    }

    /**
     * Gets the physical file for download.
     *
     * @param fileId the file ID
     * @return the File object, or null if not found
     */
    public File getPhysicalFile(String fileId) {
        GroupFile groupFile = groupFileDAO.getFileById(fileId);
        if (groupFile == null) {
            return null;
        }

        File file = new File(groupFile.getFilepath());
        return file.exists() ? file : null;
    }
}
