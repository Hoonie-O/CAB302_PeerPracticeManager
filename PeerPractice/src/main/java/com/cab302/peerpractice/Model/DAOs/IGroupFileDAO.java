package com.cab302.peerpractice.Model.DAOs;

import com.cab302.peerpractice.Model.Entities.GroupFile;
import java.util.List;

/**
 * DAO interface for managing group file sharing.
 */
public interface IGroupFileDAO {
    /**
     * Adds a new file record to the database.
     * @param file the GroupFile to add
     * @return true if successful, false otherwise
     */
    boolean addFile(GroupFile file);

    /**
     * Deletes a file record from the database.
     * @param fileId the ID of the file to delete
     * @return true if successful, false otherwise
     */
    boolean deleteFile(String fileId);

    /**
     * Retrieves a file by its ID.
     * @param fileId the file ID
     * @return the GroupFile, or null if not found
     */
    GroupFile getFileById(String fileId);

    /**
     * Retrieves all files for a specific group.
     * @param groupId the group ID
     * @return list of GroupFile objects
     */
    List<GroupFile> getFilesForGroup(int groupId);

    /**
     * Deletes all files for a specific group.
     * @param groupId the group ID
     * @return true if successful, false otherwise
     */
    boolean deleteFilesForGroup(int groupId);

    /**
     * Gets all files uploaded by a specific user.
     * @param userId the user ID
     * @return list of GroupFile objects
     */
    List<GroupFile> getFilesByUploader(String userId);
}
