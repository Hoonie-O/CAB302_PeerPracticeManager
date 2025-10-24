package com.cab302.peerpractice.Model.DAOs;

import com.cab302.peerpractice.Model.Entities.GroupFile;
import java.util.List;

/**
 * <hr>
 * Data Access Object interface for managing group file sharing operations.
 *
 * <p>This interface defines the contract for group file management data operations,
 * providing methods to store, retrieve, and manage files shared within groups
 * in the peer practice system.
 *
 * <p> Key features include:
 * <ul>
 *   <li>File metadata storage and retrieval</li>
 *   <li>Group-specific file organization</li>
 *   <li>Uploader tracking and file attribution</li>
 *   <li>Bulk file management operations</li>
 * </ul>
 *
 * @see GroupFile
 * @see GroupFileDAO
 */
public interface IGroupFileDAO {
    /**
     * <hr>
     * Adds a new file record to the database.
     *
     * <p>Persists file metadata including file identification, group association,
     * uploader information, and file characteristics for organized file management
     * and retrieval.
     *
     * @param file the GroupFile object containing file metadata to add
     * @return true if the file was successfully added, false otherwise
     */
    boolean addFile(GroupFile file);

    /**
     * <hr>
     * Deletes a file record from the database.
     *
     * <p>Removes a specific file metadata record using its unique identifier,
     * allowing for precise file management and cleanup operations.
     *
     * @param fileId the unique identifier of the file to delete
     * @return true if the file was successfully deleted, false otherwise
     */
    boolean deleteFile(String fileId);

    /**
     * <hr>
     * Retrieves a file by its unique identifier.
     *
     * <p>Fetches a specific file metadata record using its primary key,
     * returning the complete file entity with all associated attributes.
     *
     * @param fileId the unique identifier of the file to retrieve
     * @return the GroupFile object if found, null otherwise
     */
    GroupFile getFileById(String fileId);

    /**
     * <hr>
     * Retrieves all files for a specific group.
     *
     * <p>Fetches the complete file list for a particular group, providing
     * access to all files shared within that group context for collaborative
     * file sharing and resource management.
     *
     * @param groupId the unique identifier of the group
     * @return a list of GroupFile objects associated with the specified group
     */
    List<GroupFile> getFilesForGroup(int groupId);

    /**
     * <hr>
     * Deletes all files for a specific group.
     *
     * <p>Removes the entire file history for a particular group, typically
     * used when a group is dissolved or requires complete file cleanup
     * and data removal.
     *
     * @param groupId the unique identifier of the group
     * @return true if the operation completed successfully, false otherwise
     */
    boolean deleteFilesForGroup(int groupId);

    /**
     * <hr>
     * Gets all files uploaded by a specific user.
     *
     * <p>Fetches the complete file upload history for a particular user
     * across all groups, providing a comprehensive view of the user's
     * file sharing activity and contributions.
     *
     * @param userId the username of the uploader to fetch files for
     * @return a list of GroupFile objects uploaded by the specified user
     */
    List<GroupFile> getFilesByUploader(String userId);
}