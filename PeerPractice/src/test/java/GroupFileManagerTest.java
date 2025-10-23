import com.cab302.peerpractice.Model.Entities.GroupFile;
import com.cab302.peerpractice.Model.Managers.GroupFileManager;
import com.cab302.peerpractice.Model.DAOs.IGroupFileDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for GroupFileManager.
 * Tests file upload, download, deletion, and retrieval operations.
 */
class GroupFileManagerTest {

    private GroupFileManager manager;
    private MockGroupFileDAO dao;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        dao = new MockGroupFileDAO();
        manager = new GroupFileManager(dao);
    }

    @Test
    void uploadFile_createsFileRecord() throws IOException {
        // Create a temporary test file
        File testFile = tempDir.resolve("test.txt").toFile();
        Files.writeString(testFile.toPath(), "Test content");

        // Upload the file
        GroupFile uploaded = manager.uploadFile(1, "alice", testFile, "Test file");

        // Verify the file was added to DAO
        assertNotNull(uploaded);
        assertEquals(1, uploaded.getGroupId());
        assertEquals("alice", uploaded.getUploaderId());
        assertEquals("test.txt", uploaded.getFilename());
        assertNotNull(dao.getFileById(uploaded.getFileId()));
    }

    @Test
    void uploadFile_invalidGroupId_throws() {
        File testFile = tempDir.resolve("test.txt").toFile();
        assertThrows(IllegalArgumentException.class,
                () -> manager.uploadFile(-1, "alice", testFile, "Test"));
    }

    @Test
    void uploadFile_nullUploader_throws() throws IOException {
        File testFile = tempDir.resolve("test.txt").toFile();
        Files.writeString(testFile.toPath(), "Test content");

        assertThrows(IllegalArgumentException.class,
                () -> manager.uploadFile(1, null, testFile, "Test"));
    }

    @Test
    void uploadFile_nonExistentFile_throws() {
        File nonExistent = new File("/nonexistent/file.txt");
        assertThrows(IllegalArgumentException.class,
                () -> manager.uploadFile(1, "alice", nonExistent, "Test"));
    }

    @Test
    void getFilesForGroup_returnsCorrectFiles() {
        // Add files for different groups
        dao.addFile(createTestFile("1", 1, "alice", "file1.txt"));
        dao.addFile(createTestFile("2", 1, "bob", "file2.txt"));
        dao.addFile(createTestFile("3", 2, "carol", "file3.txt"));

        // Get files for group 1
        List<GroupFile> group1Files = manager.getFilesForGroup(1);

        assertEquals(2, group1Files.size());
        assertTrue(group1Files.stream().anyMatch(f -> f.getFilename().equals("file1.txt")));
        assertTrue(group1Files.stream().anyMatch(f -> f.getFilename().equals("file2.txt")));
    }

    @Test
    void getFileById_returnsCorrectFile() {
        GroupFile file = createTestFile("123", 1, "alice", "test.pdf");
        dao.addFile(file);

        GroupFile retrieved = manager.getFileById("123");

        assertNotNull(retrieved);
        assertEquals("test.pdf", retrieved.getFilename());
        assertEquals("alice", retrieved.getUploaderId());
    }

    @Test
    void deleteFile_removesFile() {
        GroupFile file = createTestFile("456", 1, "bob", "delete-me.txt");
        dao.addFile(file);

        assertTrue(manager.deleteFile("456"));
        assertNull(dao.getFileById("456"));
    }

    @Test
    void deleteFile_invalidId_returnsFalse() {
        assertFalse(manager.deleteFile("nonexistent"));
    }

    @Test
    void deleteFilesForGroup_removesAllGroupFiles() {
        dao.addFile(createTestFile("1", 1, "alice", "file1.txt"));
        dao.addFile(createTestFile("2", 1, "bob", "file2.txt"));
        dao.addFile(createTestFile("3", 2, "carol", "file3.txt"));

        assertTrue(manager.deleteFilesForGroup(1));

        // Group 1 files should be deleted
        assertTrue(manager.getFilesForGroup(1).isEmpty());

        // Group 2 files should remain
        assertEquals(1, manager.getFilesForGroup(2).size());
    }

    @Test
    void getFilesByUploader_returnsUserFiles() {
        dao.addFile(createTestFile("1", 1, "alice", "alice-file1.txt"));
        dao.addFile(createTestFile("2", 2, "alice", "alice-file2.txt"));
        dao.addFile(createTestFile("3", 1, "bob", "bob-file.txt"));

        List<GroupFile> aliceFiles = manager.getFilesByUploader("alice");

        assertEquals(2, aliceFiles.size());
        assertTrue(aliceFiles.stream().allMatch(f -> f.getUploaderId().equals("alice")));
    }

    /**
     * Helper method to create a test GroupFile.
     */
    private GroupFile createTestFile(String id, int groupId, String uploaderId, String filename) {
        return new GroupFile(
                id,
                groupId,
                uploaderId,
                filename,
                "/test/path/" + filename,
                1024L,
                "text/plain",
                LocalDateTime.now(),
                "Test file"
        );
    }

    /**
     * Mock implementation of IGroupFileDAO for testing.
     */
    private static class MockGroupFileDAO implements IGroupFileDAO {
        private final Map<String, GroupFile> files = new HashMap<>();

        @Override
        public boolean addFile(GroupFile file) {
            files.put(file.getFileId(), file);
            return true;
        }

        @Override
        public boolean deleteFile(String fileId) {
            return files.remove(fileId) != null;
        }

        @Override
        public GroupFile getFileById(String fileId) {
            return files.get(fileId);
        }

        @Override
        public List<GroupFile> getFilesForGroup(int groupId) {
            return files.values().stream()
                    .filter(f -> f.getGroupId() == groupId)
                    .collect(Collectors.toList());
        }

        @Override
        public boolean deleteFilesForGroup(int groupId) {
            List<String> toDelete = files.values().stream()
                    .filter(f -> f.getGroupId() == groupId)
                    .map(GroupFile::getFileId)
                    .collect(Collectors.toList());

            toDelete.forEach(files::remove);
            return true;
        }

        @Override
        public List<GroupFile> getFilesByUploader(String userId) {
            return files.values().stream()
                    .filter(f -> f.getUploaderId().equals(userId))
                    .collect(Collectors.toList());
        }
    }
}
