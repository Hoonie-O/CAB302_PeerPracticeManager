import com.cab302.peerpractice.Model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class NotesManagerTest {

    NotesManager notesManager;
    User user;
    Group group;
    INotesDAO notesDAO = new MockNotesDao();
    IGroupDAO groupDAO = new MockGroupDAO();
    int groupID;

    @BeforeEach
    public void setUp(){
        notesManager = new NotesManager(notesDAO,groupDAO);
        Group group = new Group("group","group",false, UUID.randomUUID().toString(),LocalDateTime.now());
        groupID = groupDAO.addGroup(group);
    }

    @Test
    public void TestCreateNoteNormal(){
        String noteID = assertDoesNotThrow(() -> notesManager.addNote("note", groupID));
        assertNotNull(noteID);
        Note note = notesDAO.getNote(noteID);
        assertNotNull(note);
        assertEquals("note",note.getName());
        assertEquals(groupID,note.getGroup());
        assertEquals(noteID, note.getID());
        List<Note> allNotes = notesDAO.getAllNotes();
        assertNotNull(allNotes);
        assertFalse(allNotes.isEmpty());
        assertEquals(note,allNotes.getFirst());
        List<Note> notes = notesDAO.getNotes(groupID);
        assertNotNull(notes);
        assertFalse(notes.isEmpty());
        assertEquals(note,notes.getFirst());

    }

    @Test
    public void TestCreateNoteNullName(){
        assertThrows(IllegalArgumentException.class , ()->notesManager.addNote(null,groupID ));
    }

    @Test
    public void TestCreateNoteEmptyName(){
        assertThrows(IllegalArgumentException.class, ()->notesManager.addNote("",groupID));
    }

    @Test
    public void TestCreateZeroGroupID(){
        assertThrows(IllegalArgumentException.class, () -> notesManager.addNote("note",0));
    }

    @Test
    public void TestCreateNegativeGroupID(){
        assertThrows(IllegalArgumentException.class , () -> notesManager.addNote("note",-1));
    }

    @Test
    public void TestCreateNameLargerThan20(){
        assertThrows(IllegalArgumentException.class , () -> notesManager.addNote("somethingsomethingsomethingsomethingsomethingsomething",groupID));
    }

    @Test
    public void TestCreateNonExistentGroup(){
        assertThrows(IllegalArgumentException.class , () -> notesManager.addNote("something",2));
    }

    @Test
    public void TestChangeNameNormal(){
        String noteID = notesManager.addNote("something",groupID);
        notesManager.changeName(noteID,"name");
        Note note = notesDAO.getNote(noteID);
        assertEquals("name",note.getName());
    }

    @Test
    public void TestChangeNullName(){
        String noteID = notesManager.addNote("something",groupID);
        assertThrows(IllegalArgumentException.class, () -> notesManager.changeName(noteID,null));
    }

    @Test
    public void TestChangeNameEmpty(){
        String noteID = notesManager.addNote("something",groupID);
        assertThrows(IllegalArgumentException.class , () -> notesManager.changeName(noteID,""));
    }

    @Test
    public void TestChangeNameNonExistentNote(){
        assertThrows(IllegalArgumentException.class, () -> notesManager.changeName("asfsaf","something"));
    }

    @Test
    public void TestDeleteNote(){
        String noteID = notesManager.addNote("something",groupID);
        notesManager.deleteNote(noteID);
        assertNull(notesDAO.getNote(noteID));
        assertTrue(notesDAO.getNotes(groupID).isEmpty());
        assertTrue(notesDAO.getAllNotes().isEmpty());
    }

    @Test
    public void TestDeleteNonExistentNote(){
        assertThrows(IllegalArgumentException.class, () -> notesManager.deleteNote("asfsa"));
    }

    @Test
    public void TestDeleteEmptyID(){
        assertThrows(IllegalArgumentException.class, () -> notesManager.deleteNote(null));
    }

    @Test
    public void TestGetNotesNormal(){
        String noteID1 = notesManager.addNote("something",groupID);
        String noteID2 = notesManager.addNote("Somethingelse",groupID);
        Note note1 = notesDAO.getNote(noteID1);
        Note note2 = notesDAO.getNote(noteID2);
        List<Note> notes = notesManager.getNotes(groupID);
        assertNotNull(notes);
        assertFalse(notes.isEmpty());
        assertTrue(notes.contains(note1));
        assertTrue(notes.contains(note2));
    }

    @Test
    public void TestGetNotesEmpty(){
        assertTrue(notesManager.getNotes(groupID).isEmpty());
    }




    @Test
    public void TestAddChapterNormal(){
        String noteID = assertDoesNotThrow(() -> notesManager.addNote("something",groupID));
        String chapterID = notesManager.addChapter(noteID,"chapter");
        Chapter chapter = notesDAO.getChapter(chapterID);
        List<Chapter> chapters = notesDAO.getChapters(noteID);
        assertNotNull(chapters);
        assertFalse(chapters.isEmpty());
        assertEquals(chapter,chapters.getFirst());
    }

    @Test
    public void TestAddChapterNullNote(){
        String noteID = notesManager.addNote("something",groupID);
        assertThrows(IllegalArgumentException.class , () -> notesManager.addChapter(null,"chapter"));
    }

    @Test
    public void TestAddChapterEmptyNote(){
        String noteID = notesManager.addNote("something",groupID);
        assertThrows(IllegalArgumentException.class , () -> notesManager.addChapter("","chapter"));
    }

    @Test
    public void TestAddChapterNonExistentNote(){
        assertThrows(IllegalArgumentException.class, () -> notesManager.addChapter("asf","chapter"));
    }

    @Test
    public void TestAddChapterNullName(){
        String noteID = notesManager.addNote("something",groupID);
        assertThrows(IllegalArgumentException.class, () -> notesManager.addChapter(noteID,null));
    }

    @Test
    public void TestAddChapterEmpty(){
        String noteID = notesManager.addNote("something",groupID);
        assertThrows(IllegalArgumentException.class, () -> notesManager.addChapter(noteID,""));
    }


    @Test
    public void TestChangeChapterNameNormal(){
        String noteID = notesManager.addNote("something",groupID);
        String chapterID = notesManager.addChapter(noteID,"chapter");
        assertDoesNotThrow(() -> notesManager.changeChapterName(chapterID,"name"));
        Chapter chapter = notesDAO.getChapter(chapterID);
        assertEquals("name",chapter.getName());
    }

    @Test
    public void TestChangeChapterNameNull(){
        String noteID = notesManager.addNote("something",groupID);
        String chapterID = notesManager.addChapter(noteID,"chapter");
        assertThrows(IllegalArgumentException.class, () -> notesManager.changeChapterName(chapterID,null));
    }

    @Test
    public void TestChangeChapterNameEmpty(){
        String noteID = notesManager.addNote("something",groupID);
        String chapterID = notesManager.addChapter(noteID,"chapter");
        assertThrows(IllegalArgumentException.class, () -> notesManager.changeChapterName(chapterID,""));
    }

    @Test
    public void TestChangeChapterNameNonExistentChapter(){
        assertThrows(IllegalArgumentException.class, () -> notesManager.changeChapterName("asf","something"));
    }

    @Test
    public void TestChangeChapterNameNullChapterID(){
        assertThrows(IllegalArgumentException.class, () -> notesManager.changeChapterName(null,"something"));
    }

    @Test
    public void TestChangeContentNormal(){
        String noteID = notesManager.addNote("something",groupID);
        String chapterID = notesManager.addChapter(noteID,"chapter");
        assertDoesNotThrow(() -> notesManager.changeContent(chapterID,"Content"));
        Chapter chapter = notesDAO.getChapter(chapterID);
        assertEquals("Content",chapter.getContent());
    }

    @Test
    public void TestChangeContentInvalids(){
        String noteID = notesManager.addNote("something",groupID);
        String chapterID = notesManager.addChapter(noteID,"chapter");
        assertThrows(IllegalArgumentException.class, () -> notesManager.changeContent(chapterID,null));
        assertThrows(IllegalArgumentException.class, () -> notesManager.changeContent(chapterID, ""));
    }


}
