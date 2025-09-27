package com.cab302.peerpractice.Model.entities;

/**
 * Model class for file attachments in chapters (for future use)
 */
public class Attachment {
    private String id;
    private String chapterId;
    private String filename;
    private String filePath;
    private long fileSize;
    private String mimeType;

    public Attachment(String filename, String filePath, long fileSize, String mimeType) {
        this.filename = filename;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
    }

    // Getters
    public String getId() { return id; }
    public String getChapterId() { return chapterId; }
    public String getFilename() { return filename; }
    public String getFilePath() { return filePath; }
    public long getFileSize() { return fileSize; }
    public String getMimeType() { return mimeType; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setChapterId(String chapterId) { this.chapterId = chapterId; }
    public void setFilename(String filename) { this.filename = filename; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
}
