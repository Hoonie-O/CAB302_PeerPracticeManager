package com.cab302.peerpractice;

import com.cab302.peerpractice.Model.DAOs.*;
import com.cab302.peerpractice.Model.Entities.Chapter;
import com.cab302.peerpractice.Model.Entities.Group;
import com.cab302.peerpractice.Model.Entities.Note;
import com.cab302.peerpractice.Model.Managers.NotesManager;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class NotesManagerTest {

    private NotesManager notesManager;
    private INotesDAO notesDAO;
    private IGroupDAO groupDAO;
    private IUserDAO userDAO;
    private int groupID;
    private Group testGroup;

    @BeforeEach
    public void setUp() throws SQLException {
        // Use mocks instead of real DAOs
        userDAO = new MockUserDAO();
        groupDAO = new MockGroupDAO(userDAO);
        notesDAO = new MockNotesDAO();
        notesManager = new NotesManager(notesDAO, groupDAO);

        // Create a fresh test group
        testGroup = new Group("group", "group description", false,
                UUID.randomUUID().toString(), LocalDateTime.now());
        groupID = groupDAO.addGroup(testGroup);
    }

    @Test
    public void TestCreateNoteNormal() {
        String noteID = assertDoesNotThrow(() -> notesManager.addNote("note", groupID));
        assertNotNull(noteID);

        Note note = notesDAO.getNote(noteID);
        assertNotNull(note);
        assertEquals("note", note.getName());
        assertEquals(groupID, note.getGroup());
        assertEquals(noteID, note.getID());

        List<Note> allNotes = notesDAO.getAllNotes();
        assertNotNull(allNotes);
        assertFalse(allNotes.isEmpty());
        assertTrue(allNotes.contains(note));

        List<Note> notes = notesDAO.getNotes(groupID);
        assertNotNull(notes);
        assertFalse(notes.isEmpty());
        assertTrue(notes.contains(note));
    }

    @Test
    public void TestCreateNoteNullName() {
        assertThrows(IllegalArgumentException.class, () -> notesManager.addNote(null, groupID));
    }

    @Test
    public void TestCreateNoteEmptyName() {
        assertThrows(IllegalArgumentException.class, () -> notesManager.addNote("", groupID));
    }

    @Test
    public void TestCreateZeroGroupID() {
        assertThrows(IllegalArgumentException.class, () -> notesManager.addNote("note", 0));
    }

    @Test
    public void TestCreateNegativeGroupID() {
        assertThrows(IllegalArgumentException.class, () -> notesManager.addNote("note", -1));
    }

    @Test
    public void TestCreateNameLargerThan20() {
        assertThrows(IllegalArgumentException.class, () ->
                notesManager.addNote("somethingsomethingsomethingsomethingsomethingsomething", groupID));
    }

    @Test
    public void TestCreateNonExistentGroup() {
        assertThrows(IllegalArgumentException.class, () -> notesManager.addNote("something", 99999));
    }

    @Test
    public void TestChangeNameNormal() {
        String noteID = notesManager.addNote("something", groupID);
        notesManager.changeName(noteID, "name");
        Note note = notesDAO.getNote(noteID);
        assertEquals("name", note.getName());
    }

    @Test
    public void TestChangeNullName() {
        String noteID = notesManager.addNote("something", groupID);
        assertThrows(IllegalArgumentException.class, () -> notesManager.changeName(noteID, null));
    }

    @Test
    public void TestChangeNameEmpty() {
        String noteID = notesManager.addNote("something", groupID);
        assertThrows(IllegalArgumentException.class, () -> notesManager.changeName(noteID, ""));
    }

    @Test
    public void TestChangeNameNonExistentNote() {
        assertThrows(IllegalArgumentException.class, () -> notesManager.changeName("fakeid", "something"));
    }

    @Test
    public void TestDeleteNote() {
        String noteID = notesManager.addNote("something", groupID);
        notesManager.deleteNote(noteID);
        assertNull(notesDAO.getNote(noteID));
        assertTrue(notesDAO.getNotes(groupID).isEmpty());
        assertTrue(notesDAO.getAllNotes().isEmpty() || notesDAO.getAllNotes().stream().noneMatch(n -> n.getID().equals(noteID)));
    }

    @Test
    public void TestDeleteNonExistentNote() {
        assertThrows(IllegalArgumentException.class, () -> notesManager.deleteNote("fakeid"));
    }

    @Test
    public void TestDeleteEmptyID() {
        assertThrows(IllegalArgumentException.class, () -> notesManager.deleteNote(null));
    }

    @Test
    public void TestGetNotesNormal() {
        String noteID1 = notesManager.addNote("something", groupID);
        String noteID2 = notesManager.addNote("Somethingelse", groupID);
        Note note1 = notesDAO.getNote(noteID1);
        Note note2 = notesDAO.getNote(noteID2);

        List<Note> notes = notesManager.getNotes(groupID);
        assertNotNull(notes);
        assertTrue(notes.contains(note1));
        assertTrue(notes.contains(note2));
    }

    @Test
    public void TestGetNotesEmpty() {
        assertTrue(notesManager.getNotes(groupID).isEmpty());
    }

    @Test
    public void TestAddChapterNormal() {
        String noteID = notesManager.addNote("something", groupID);
        String chapterID = notesManager.addChapter(noteID, "chapter");
        Chapter chapter = notesDAO.getChapter(chapterID);
        assertNotNull(chapter);

        List<Chapter> chapters = notesDAO.getChapters(noteID);
        assertNotNull(chapters);
        assertTrue(chapters.contains(chapter));
    }

    @Test
    public void TestAddChapterNullNote() {
        assertThrows(IllegalArgumentException.class, () -> notesManager.addChapter(null, "chapter"));
    }

    @Test
    public void TestAddChapterEmptyNote() {
        assertThrows(IllegalArgumentException.class, () -> notesManager.addChapter("", "chapter"));
    }

    @Test
    public void TestAddChapterNonExistentNote() {
        assertThrows(IllegalArgumentException.class, () -> notesManager.addChapter("fakeid", "chapter"));
    }

    @Test
    public void TestAddChapterNullName() {
        String noteID = notesManager.addNote("something", groupID);
        assertThrows(IllegalArgumentException.class, () -> notesManager.addChapter(noteID, null));
    }

    @Test
    public void TestAddChapterEmpty() {
        String noteID = notesManager.addNote("something", groupID);
        assertThrows(IllegalArgumentException.class, () -> notesManager.addChapter(noteID, ""));
    }

    @Test
    public void TestChangeChapterNameNormal() {
        String noteID = notesManager.addNote("something", groupID);
        String chapterID = notesManager.addChapter(noteID, "chapter");
        notesManager.changeChapterName(chapterID, "name");
        Chapter chapter = notesDAO.getChapter(chapterID);
        assertEquals("name", chapter.getName());
    }

    @Test
    public void TestChangeChapterNameNull() {
        String noteID = notesManager.addNote("something", groupID);
        String chapterID = notesManager.addChapter(noteID, "chapter");
        assertThrows(IllegalArgumentException.class, () -> notesManager.changeChapterName(chapterID, null));
    }

    @Test
    public void TestChangeChapterNameEmpty() {
        String noteID = notesManager.addNote("something", groupID);
        String chapterID = notesManager.addChapter(noteID, "chapter");
        assertThrows(IllegalArgumentException.class, () -> notesManager.changeChapterName(chapterID, ""));
    }

    @Test
    public void TestChangeChapterNameNonExistentChapter() {
        assertThrows(IllegalArgumentException.class, () -> notesManager.changeChapterName("fakeid", "something"));
    }

    @Test
    public void TestChangeChapterNameNullChapterID() {
        assertThrows(IllegalArgumentException.class, () -> notesManager.changeChapterName(null, "something"));
    }

    @Test
    public void TestChangeContentNormal() {
        String noteID = notesManager.addNote("something", groupID);
        String chapterID = notesManager.addChapter(noteID, "chapter");
        notesManager.changeContent(chapterID, "Content");
        Chapter chapter = notesDAO.getChapter(chapterID);
        assertEquals("Content", chapter.getContent());
    }

    @Test
    public void TestChangeContentInvalids() {
        String noteID = notesManager.addNote("something", groupID);
        String chapterID = notesManager.addChapter(noteID, "chapter");
        assertThrows(IllegalArgumentException.class, () -> notesManager.changeContent(chapterID, null));
        assertThrows(IllegalArgumentException.class, () -> notesManager.changeContent(chapterID, ""));
    }
}
