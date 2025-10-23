package com.cab302.peerpractice.Model.Entities;

import java.time.LocalDateTime;

/**
 * Represents a file shared within a group.
 * Contains metadata about the file including its location, size, and uploader information.
 */
public class GroupFile {
    private final String fileId;
    private final int groupId;
    private final String uploaderId;
    private final String filename;
    private final String filepath;
    private final long fileSize;
    private final String mimeType;
    private final LocalDateTime uploadedAt;
    private final String description;

    public GroupFile(String fileId, int groupId, String uploaderId, String filename,
                     String filepath, long fileSize, String mimeType,
                     LocalDateTime uploadedAt, String description) {
        this.fileId = fileId;
        this.groupId = groupId;
        this.uploaderId = uploaderId;
        this.filename = filename;
        this.filepath = filepath;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
        this.uploadedAt = uploadedAt;
        this.description = description;
    }

    public String getFileId() {
        return fileId;
    }

    public int getGroupId() {
        return groupId;
    }

    public String getUploaderId() {
        return uploaderId;
    }

    public String getFilename() {
        return filename;
    }

    public String getFilepath() {
        return filepath;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getMimeType() {
        return mimeType;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Returns a human-readable file size string.
     * @return formatted file size (e.g., "1.5 MB", "320 KB")
     */
    public String getFormattedFileSize() {
        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.1f KB", fileSize / 1024.0);
        } else if (fileSize < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", fileSize / (1024.0 * 1024.0 * 1024.0));
        }
    }

    @Override
    public String toString() {
        return "GroupFile{" +
                "fileId='" + fileId + '\'' +
                ", groupId=" + groupId +
                ", uploaderId='" + uploaderId + '\'' +
                ", filename='" + filename + '\'' +
                ", filepath='" + filepath + '\'' +
                ", fileSize=" + fileSize +
                ", mimeType='" + mimeType + '\'' +
                ", uploadedAt=" + uploadedAt +
                ", description='" + description + '\'' +
                '}';
    }
}
